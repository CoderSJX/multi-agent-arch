package site.nullpointer.mss.ai.domain.llm;

import site.nullpointer.mss.ai.domain.identity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FunctionCallRequest {
    private String functionName;
    private String functionArguments;
    private Profile userProfile;
    private String enterpriseId;
    private Map<String, Object> extras;
}
