package cn.mj.thrift.test;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.protocol.TProtocol;

public class ClientFactory extends BasePooledObjectFactory<HelloService.Client> {
    private TProtocol protocol;
    private final ProtocolPool protocolPool;
    public ClientFactory(ProtocolPool protocolPool) throws Exception {
        this.protocolPool = protocolPool;
    }

    @Override
    public HelloService.Client create() throws Exception {
        this.protocol = protocolPool.getProtocol();
        return  new cn.mj.thrift.test.HelloService.Client(this.protocol);
    }

    @Override
    public PooledObject<HelloService.Client> wrap(HelloService.Client obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public boolean validateObject(PooledObject<HelloService.Client> p) {
        return protocol.getTransport().isOpen();
    }
}
