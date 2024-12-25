package site.nullpointer.mss.ai.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "site.nullpointer.mss.ai")
@EnableJpaRepositories(basePackages =
        "site.nullpointer.mss.ai.repository")
public class JPAConfiguration {
}

