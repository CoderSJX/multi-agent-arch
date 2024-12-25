package site.nullpointer.mss.ai.domain.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Agent {
    private AgentOptions options;
    private String id;
    private List<Message> messageList;
}
