package cn.mj.thrift.test.client.controller;

import cn.mj.thrift.test.HelloResponse;
import cn.mj.thrift.test.client.service.HelloService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Resource(name = "blockService")
    HelloService helloService;

    @GetMapping("/hello")
    public HelloResponse hello() throws Exception {
        return helloService.hello();
    }
}
