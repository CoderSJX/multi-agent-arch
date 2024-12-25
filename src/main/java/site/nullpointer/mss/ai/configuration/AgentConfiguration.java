package site.nullpointer.mss.ai.configuration;

import site.nullpointer.mss.ai.domain.agent.AgentOptions;
import site.nullpointer.mss.ai.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(3)
public class AgentConfiguration {

    @Autowired
    private AgentManager agentManager;
    @Bean(name = "intentAgentOptions")
    protected AgentOptions intentAgentOptions() {
        return agentManager.getAgentByType("INTENT_DISPATCH");
    }



}
