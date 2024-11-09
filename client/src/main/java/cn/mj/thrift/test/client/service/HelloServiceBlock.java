package cn.mj.thrift.test.client.service;

import cn.mj.thrift.test.ClientPool;
import cn.mj.thrift.test.HelloResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.springframework.stereotype.Service;
@Slf4j
@Service("blockService")
public class HelloServiceBlock implements HelloService{

    private final cn.mj.thrift.test.HelloService.Client client;
    private final ClientPool clientPool;

    public HelloServiceBlock(ClientPool clientPool) throws Exception {
        this.clientPool = clientPool;
        this.client = clientPool.getClient();
    }

    @Override
    public HelloResponse hello() throws Exception {
//        cn.mj.thrift.test.HelloService.Client client = clientPool.getClient();
//        try{
//            return client.sayHello(getRequest());
//        }catch (Exception e){
//            log.error("hello failed: ", e);
//            throw e;
//        }finally {
//            clientPool.returnClient(client);
//        }


        try(TTransport transport = new TSocket("127.0.0.1",9999)){
            TFramedTransport framedTransport = new TFramedTransport(transport);
            TProtocol protocol = new TBinaryProtocol(framedTransport);
            framedTransport.open();
            cn.mj.thrift.test.HelloService.Client client = new cn.mj.thrift.test.HelloService.Client(protocol);
            HelloResponse response = client.sayHello(getRequest());
            framedTransport.close();
            return response;
        }catch (Exception e){
            log.error("hello failed: ", e);
            throw new RuntimeException(e);
        }
    }
}
