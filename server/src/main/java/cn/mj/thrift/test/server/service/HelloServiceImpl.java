package cn.mj.thrift.test.server.service;

import cn.mj.thrift.test.HelloRequest;
import cn.mj.thrift.test.HelloResponse;
import cn.mj.thrift.test.HelloService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("helloService")
public class HelloServiceImpl implements HelloService.AsyncIface {
    @Resource
    private ObjectMapper objectMapper;
    private final Random random = new Random();

    @Override
    public void sayHello(HelloRequest req, AsyncMethodCallback<HelloResponse> resultHandler) throws TException {
        HelloResponse helloResponse = new HelloResponse();
        try{
            var json = objectMapper.writeValueAsString(req);
//            log.info("sayHello: {}", json);

            helloResponse.setAge(19);
            helloResponse.setName(req.getName());
            helloResponse.setMessage("hello, " + req.getName());
            helloResponse.setTimestamp(System.currentTimeMillis());

            int sleepTime = random.nextInt(100, 200);
//            TimeUnit.MILLISECONDS.sleep(sleepTime);
        }catch (Exception e){
            log.error("sayHello failed: ", e);
            helloResponse.setMessage("error, " +e.getMessage());
        }
        resultHandler.onComplete(helloResponse);
    }
}
