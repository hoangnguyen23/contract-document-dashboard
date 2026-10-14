package vn.contractgovernance.api.identity;

import java.sql.Timestamp;
import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SessionCleanup {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    public SessionCleanup(JdbcTemplate jdbc,Clock clock){this.jdbc=jdbc;this.clock=clock;}
    @Scheduled(fixedDelay=600_000,initialDelay=600_000)
    @Transactional
    public void discardExpiredTokens(){
        var now=clock.instant();
        jdbc.update("delete from cg_identity_session where absolute_expires_at<=? or last_seen_at<=?",Timestamp.from(now),Timestamp.from(now.minusSeconds(1800)));
        jdbc.update("delete from cg_identity_login_rate where window_started_at<=?",Timestamp.from(now.minusSeconds(3600)));
    }
}
