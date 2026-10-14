package vn.contractgovernance.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Clock;

@SpringBootApplication
@EnableScheduling
public class ContractGovernanceApplication {
    public static void main(String[] args) { SpringApplication.run(ContractGovernanceApplication.class, args); }
    @Bean Clock clock() { return Clock.systemUTC(); }
}
