package cn.mj.thrift.test;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;

public class ProtocolFactory extends BasePooledObjectFactory<TProtocol> {
    @Override
    public TProtocol create() throws Exception {
        TTransport transport = new TSocket("127.0.0.1",9999);
        TFramedTransport framedTransport = new TFramedTransport(transport);
        TProtocol protocol = new TBinaryProtocol(framedTransport);
        framedTransport.open();
        return protocol;
    }

    @Override
    public PooledObject<TProtocol> wrap(TProtocol tProtocol) {
        return new DefaultPooledObject<>(tProtocol);
    }

    @Override
    public boolean validateObject(PooledObject<TProtocol> p) {
        return p.getObject().getTransport().isOpen();
    }

    @Override
    public void destroyObject(PooledObject<TProtocol> p) throws Exception {
        p.getObject().getTransport().close();
    }

}
