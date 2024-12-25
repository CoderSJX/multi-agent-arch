package site.nullpointer.mss.ai.domain.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class AgentOptions {
    //智能体的业务类别
    private String type;
    //系统提示词
    private String systemPrompt;
    //温度
    private Double temperature;
    //智能体描述，仅作说明，无实际作用
    private String description;
    //智能体注册的方法
    private List<OpenAiApi.FunctionTool> functionTools;
}
