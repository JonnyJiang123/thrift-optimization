package cn.mj.thrift.test.reactiveclient.service;

import cn.mj.thrift.test.HelloRequest;
import cn.mj.thrift.test.HelloResponse;
import reactor.core.publisher.Mono;

public interface HelloService {
    Mono<HelloResponse> hello();

    default HelloRequest getRequest(){
        HelloRequest helloRequest = new HelloRequest();
        helloRequest.setName("Jonny");
        helloRequest.setMessage("good night");
        return helloRequest;
    }
}
