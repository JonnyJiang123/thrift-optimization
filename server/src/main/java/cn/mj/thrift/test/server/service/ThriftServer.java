package cn.mj.thrift.test.server.service;

import cn.mj.thrift.test.HelloService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ThriftServer {
    @Resource
    HelloService.AsyncIface helloService;
    @PostConstruct
    public void init() throws TTransportException {
        log.info("ThriftServer init");
//        new Thread(()->{
//            try(TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9999)){
//                TServer server = new TNonblockingServer(new TNonblockingServer.Args(serverTransport)
//                        .processor(new HelloService.AsyncProcessor<>(helloService)));
//                server.serve();
//            }catch (Exception e){
//                log.error("init failed: ", e);
//            }
//
//        }).start();
        new Thread(()->{
            try(TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9999)){
                TServer server = new TThreadedSelectorServer(new TThreadedSelectorServer.Args(serverTransport)
                        .processor(new HelloService.AsyncProcessor<>(helloService))
                        .selectorThreads(10).workerThreads(200));
                server.serve();
            }catch (Exception e){
                log.error("init failed: ", e);
            }
            log.info("ThriftServer init done");
        }).start();

    }
}
