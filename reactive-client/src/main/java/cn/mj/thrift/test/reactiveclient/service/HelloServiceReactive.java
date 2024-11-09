package cn.mj.thrift.test.reactiveclient.service;

import cn.mj.thrift.test.ClientPool;
import cn.mj.thrift.test.HelloResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Service("reactiveService")
public class HelloServiceReactive implements HelloService{
    private final cn.mj.thrift.test.HelloService.Client client;
    private final ClientPool clientPool;

    public HelloServiceReactive(ClientPool clientPool) throws Exception {
        this.clientPool = clientPool;
        this.client = clientPool.getClient();
    }
    @Override
    public Mono<HelloResponse> hello() {
//        return Mono.fromFuture(CompletableFuture.supplyAsync(()-> {
//            try {
//                return clientPool.getClient();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }).thenApply(client->{
//            try {
//                return client.sayHello(getRequest());
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }finally {
//                try {
//                    clientPool.returnClient(client);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }));
        try(TTransport transport = new TSocket("127.0.0.1",9999)){
            TFramedTransport framedTransport = new TFramedTransport(transport);
            TProtocol protocol = new TBinaryProtocol(framedTransport);
            framedTransport.open();
            cn.mj.thrift.test.HelloService.Client client = new cn.mj.thrift.test.HelloService.Client(protocol);
            HelloResponse response = client.sayHello(getRequest());
            framedTransport.close();
            return Mono.just(response);
        }catch (Exception e){
            log.error("hello failed: ", e);
            throw new RuntimeException(e);
        }
    }
}
