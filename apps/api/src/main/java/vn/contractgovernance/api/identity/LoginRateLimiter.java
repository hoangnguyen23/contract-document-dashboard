package vn.contractgovernance.api.identity;

import java.sql.Timestamp;
import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginRateLimiter {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    public LoginRateLimiter(JdbcTemplate jdbc, Clock clock) { this.jdbc = jdbc; this.clock = clock; }

    /** Failed login attempts commit independently of the session/login transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquire(String username, String remoteIp) {
        var now = clock.instant();
        String key = PasswordHash.digest(username + "\u0000" + remoteIp);
        Integer attempts = jdbc.queryForObject("insert into cg_identity_login_rate(key_hash,window_started_at,attempts) values (?,?,1) on conflict(key_hash) do update set attempts=case when cg_identity_login_rate.window_started_at <= ? then 1 else cg_identity_login_rate.attempts+1 end, window_started_at=case when cg_identity_login_rate.window_started_at <= ? then excluded.window_started_at else cg_identity_login_rate.window_started_at end returning attempts",
                Integer.class, key, Timestamp.from(now), Timestamp.from(now.minusSeconds(60)), Timestamp.from(now.minusSeconds(60)));
        return attempts != null && attempts <= 10;
    }
}
