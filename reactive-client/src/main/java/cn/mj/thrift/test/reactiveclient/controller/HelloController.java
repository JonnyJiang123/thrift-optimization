package cn.mj.thrift.test.reactiveclient.controller;

import cn.mj.thrift.test.HelloResponse;
import cn.mj.thrift.test.reactiveclient.service.HelloService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    @Resource(name = "reactiveService")
    HelloService helloService;
    @GetMapping("/hello")
    public Mono<HelloResponse> hello(){
        return helloService.hello();
    }
}
