package vn.contractgovernance.api.identity;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import vn.contractgovernance.api.shared.Actor;
import vn.contractgovernance.api.shared.ApiException;
import vn.contractgovernance.api.shared.JsonSupport;

@Service
public class SessionService {
    public static final String COOKIE = "__Host-cg-session";
    public static final String ATTRIBUTE = SessionService.class.getName() + ".session";
    private static final Duration IDLE = Duration.ofMinutes(30);
    private static final Duration ABSOLUTE = Duration.ofHours(8);
    private final JdbcTemplate jdbc;
    private final UserDirectory users;
    private final JsonSupport json;
    private final Clock clock;
    private final LoginRateLimiter rateLimiter;
    private final TransactionTemplate transactions;
    private final String missingUserHash = PasswordHash.encode(PasswordHash.randomToken());

    public record Session(String tokenHash, String csrfToken, UUID userId, Long authVersion,
                          Instant createdAt, Instant lastSeenAt, Instant absoluteExpiresAt, Actor actor) {}
    public record Issued(Session session, String opaqueToken) {}

    public SessionService(JdbcTemplate jdbc, UserDirectory users, JsonSupport json, Clock clock, LoginRateLimiter rateLimiter, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc; this.users = users; this.json = json; this.clock = clock;
        this.rateLimiter = rateLimiter;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Transactional
    public Optional<Session> resolve(String token) {
        if (token == null || !token.matches("^[A-Za-z0-9_-]{43}$")) return Optional.empty();
        String hash = PasswordHash.digest(token);
        var rows = jdbc.query("select token_hash,csrf_token,user_id,auth_version,created_at,last_seen_at,absolute_expires_at from cg_identity_session where token_hash=? for update",
                (rs, row) -> new Session(rs.getString(1), rs.getString(2), rs.getObject(3, UUID.class),
                        (Long)rs.getObject(4), rs.getTimestamp(5).toInstant(), rs.getTimestamp(6).toInstant(),
                        rs.getTimestamp(7).toInstant(), null), hash);
        if (rows.isEmpty()) return Optional.empty();
        Session session = rows.getFirst();
        Instant now = clock.instant();
        if (!now.isBefore(session.absoluteExpiresAt()) || !now.isBefore(session.lastSeenAt().plus(IDLE))) {
            delete(hash); return Optional.empty();
        }
        Actor actor = null;
        if (session.userId() != null) {
            var versions = jdbc.query("select auth_version from cg_identity_credential where user_id=?",
                    (rs, row) -> rs.getLong(1), session.userId());
            if (versions.isEmpty() || !versions.getFirst().equals(session.authVersion())) {
                delete(hash); return Optional.empty();
            }
            try { actor = users.actor(session.userId()); }
            catch (ApiException ex) { delete(hash); return Optional.empty(); }
        }
        jdbc.update("update cg_identity_session set last_seen_at=? where token_hash=?", Timestamp.from(now), hash);
        return Optional.of(new Session(hash, session.csrfToken(), session.userId(), session.authVersion(),
                session.createdAt(), now, session.absoluteExpiresAt(), actor));
    }

    @Transactional
    public Issued bootstrap() { return issue(null, null); }

    public Issued login(Session anonymousOrCurrent, String username, String password, String remoteIp) {
        if (!rateLimiter.acquire(username, remoteIp))
            throw ApiException.of("RATE_LIMITED", "Đã vượt 10 lần đăng nhập trong một phút.");
        var user = users.byUsername(username);
        var credential = user.flatMap(value -> jdbc.query("select password_hash,auth_version from cg_identity_credential where user_id=?",
                (rs, row) -> new Credential(rs.getString(1), rs.getLong(2)), value.id()).stream().findFirst());
        boolean passwordMatches = PasswordHash.matches(password, credential.map(Credential::passwordHash).orElse(missingUserHash));
        if (!passwordMatches || user.isEmpty() || !user.get().body().path("active").asBoolean() || credential.isEmpty())
            throw ApiException.of("UNAUTHENTICATED", "Tên đăng nhập hoặc mật khẩu không đúng.");
        // Rate counting and expensive hashing complete before reserving the transaction connection.
        return transactions.execute(transaction -> {
            var lockedUser = users.lock(user.get().id());
            Long currentVersion = jdbc.queryForObject("select auth_version from cg_identity_credential where user_id=? for update", Long.class, lockedUser.id());
            if (!lockedUser.body().path("active").asBoolean() || currentVersion == null
                    || currentVersion.longValue() != credential.get().authVersion())
                throw ApiException.of("UNAUTHENTICATED", "Tên đăng nhập hoặc mật khẩu không đúng.");
            Actor current = users.actor(lockedUser.id());
            delete(anonymousOrCurrent.tokenHash());
            return issue(current, currentVersion);
        });
    }

    private Issued issue(Actor actor, Long authVersion) {
        String opaque = PasswordHash.randomToken();
        Instant now = clock.instant();
        var session = new Session(PasswordHash.digest(opaque), PasswordHash.randomToken(), actor == null ? null : actor.id(),
                authVersion, now, now, now.plus(ABSOLUTE), actor);
        jdbc.update("insert into cg_identity_session(token_hash,csrf_token,user_id,auth_version,created_at,last_seen_at,absolute_expires_at) values (?,?,?,?,?,?,?)",
                session.tokenHash(), session.csrfToken(), session.userId(), authVersion,
                Timestamp.from(now), Timestamp.from(now), Timestamp.from(session.absoluteExpiresAt()));
        return new Issued(session, opaque);
    }

    @Transactional
    public void logout(Session session) { delete(session.tokenHash()); }
    private void delete(String tokenHash) { jdbc.update("delete from cg_identity_session where token_hash=?", tokenHash); }

    public JsonNode representation(Session session) {
        var response = json.object();
        response.put("authenticated", session.actor() != null);
        response.put("csrfToken", session.csrfToken());
        if (session.actor() == null) { response.putNull("user"); response.putNull("expiresAt"); }
        else {
            response.set("user", users.representation(users.require(session.userId()), session.actor()));
            Instant idleExpiry = session.lastSeenAt().plus(IDLE);
            response.put("expiresAt", JsonSupport.timestamp(idleExpiry.isBefore(session.absoluteExpiresAt()) ? idleExpiry : session.absoluteExpiresAt()));
        }
        return response;
    }

    public void createCredential(UUID userId, String password) {
        jdbc.update("insert into cg_identity_credential(user_id,password_hash,auth_version,created_at) values (?,?,1,?)",
                userId, PasswordHash.encode(password), Timestamp.from(clock.instant()));
    }

    public void revokeUser(UUID userId) {
        jdbc.update("update cg_identity_credential set auth_version=auth_version+1 where user_id=?", userId);
        jdbc.update("delete from cg_identity_session where user_id=?", userId);
    }

    private record Credential(String passwordHash, long authVersion) {}
}
