package cn.mj.thrift.test.client;

import cn.mj.thrift.test.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
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
