package site.nullpointer.mss.ai.domain.functioncall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class FunctionCallResult {
    private boolean success;

}
