package site.nullpointer.mss.ai.service;

import site.nullpointer.mss.ai.domain.llm.FunctionCallRequest;
import site.nullpointer.mss.ai.domain.llm.FunctionCallResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
//用来调用远程方法的类，大模型输出的functioncall都在此调用
public class RemoteFunctionCallService implements FunctionCallService {
    private static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();


    @Override
    public FunctionCallResult apply(FunctionCallRequest callRequest) {
        FunctionCallResult callResult = new FunctionCallResult();
        callResult.setContent("");
        return callResult;
    }
}
