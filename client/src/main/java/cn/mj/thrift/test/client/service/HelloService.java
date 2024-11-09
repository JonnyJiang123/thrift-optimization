package cn.mj.thrift.test.client.service;

import cn.mj.thrift.test.HelloRequest;
import cn.mj.thrift.test.HelloResponse;

public interface HelloService {
    HelloResponse hello() throws Exception;

    default HelloRequest getRequest(){
        HelloRequest helloRequest = new HelloRequest();
        helloRequest.setName("Jonny");
        helloRequest.setMessage("good night");
        return helloRequest;
    }
}
