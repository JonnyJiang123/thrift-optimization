package cn.mj.thrift.test.client.service;

import cn.mj.thrift.test.HelloResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service("nonblockService")
public class HelloServiceNonBlock implements HelloService {
    @Override
    public HelloResponse hello() {
        HelloResponse helloResponse = new HelloResponse();

        try(TNonblockingSocket socket = new TNonblockingSocket("127.0.0.1",9999)){
            TAsyncClientManager clientManager = new TAsyncClientManager();
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            cn.mj.thrift.test.HelloService.AsyncClient client = new cn.mj.thrift.test.HelloService.AsyncClient(protocolFactory,clientManager,socket);
            CompletableFuture<HelloResponse> future = new CompletableFuture<>();
            client.sayHello(getRequest(), new AsyncMethodCallback<>() {
                @Override
                public void onComplete(HelloResponse response) {
                    future.complete(response);
                }
                @Override
                public void onError(Exception exception) {
                    future.completeExceptionally(exception);
                }
            });
            return future.get();
        }catch (Exception e){
            log.error("hello failed: ", e);
            helloResponse.setMessage("error, " +e.getMessage());
        }
        return helloResponse;
    }
}
