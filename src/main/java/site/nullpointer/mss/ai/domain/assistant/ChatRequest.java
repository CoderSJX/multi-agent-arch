package site.nullpointer.mss.ai.domain.assistant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
//用户问题请求体
public class ChatRequest {
    //用户的问题
    private String content;
    //额外的补充信息，用map存储
    private Map<String, Object> extra;

}
