package site.nullpointer.mss.ai.domain.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
//智能体调度结果
public class AgentDispatchResult {
    //根据用户问题匹配到的智能体分类
    private String agentType;
}
