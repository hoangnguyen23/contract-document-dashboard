package vn.contractgovernance.api.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/** Operational marker after startup runners, including any explicitly enabled synthetic identity seed. */
@Component
public final class BackendStartupStatus implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(BackendStartupStatus.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOG.info("CG_BACKEND_READY");
    }
}
