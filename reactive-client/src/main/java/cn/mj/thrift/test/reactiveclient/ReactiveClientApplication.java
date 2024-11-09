package cn.mj.thrift.test.reactiveclient;

import cn.mj.thrift.test.ClientFactory;
import cn.mj.thrift.test.ClientPool;
import cn.mj.thrift.test.ProtocolFactory;
import cn.mj.thrift.test.ProtocolPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReactiveClientApplication {

    public static void main(String[] args) {
//        System.setProperty("reactor.netty.ioWorkerCount","256");
        SpringApplication.run(ReactiveClientApplication.class, args);
    }
    @Bean
    public ProtocolFactory protocolFactory() {
        return new ProtocolFactory();
    }
    @Bean
    public ProtocolPool protocolPool(ProtocolFactory protocolFactory) {
        return new ProtocolPool(protocolFactory);
    }
    @Bean
    public ClientFactory clientFactory(ProtocolPool protocolPool) throws Exception {
        return new ClientFactory(protocolPool);
    }
    @Bean
    public ClientPool clientPool(ClientFactory clientFactory) throws Exception {
        return new ClientPool(clientFactory);
    }
}
