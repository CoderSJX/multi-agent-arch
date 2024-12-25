package site.nullpointer.mss.ai.service;

import site.nullpointer.mss.ai.domain.llm.FunctionCallRequest;
import site.nullpointer.mss.ai.domain.llm.FunctionCallResult;
import org.springframework.stereotype.Service;

@Service
public interface FunctionCallService {
    FunctionCallResult apply(FunctionCallRequest request);
}
