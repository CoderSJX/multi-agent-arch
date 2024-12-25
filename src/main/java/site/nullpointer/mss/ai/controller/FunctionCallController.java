package site.nullpointer.mss.ai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/function_call")
@Slf4j
//用来实现方法调用，应该是在另一个项目（functioncall调用中心）中，这里只是写一个controller样例。
public class FunctionCallController {
//    @PostMapping("/{functionName}")
//    public FunctionCallResult call(@RequestBody FunctionCallRequest request, @PathVariable(value = "functionName") String functionName, ServerWebExchange exchange) {
//
//
//    }
}
