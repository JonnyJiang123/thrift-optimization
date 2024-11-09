package cn.mj.thrift.test;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.layered.TFramedTransport;

public class SocketFactory extends BasePooledObjectFactory<TTransport> {
    @Override
    public TTransport create() throws Exception {
        return new TSocket("127.0.0.1",9999);
    }

    @Override
    public PooledObject<TTransport> wrap(TTransport tProtocol) {
        return new DefaultPooledObject<>(tProtocol);
    }

    @Override
    public boolean validateObject(PooledObject<TTransport> p) {
        return p.getObject().isOpen();
    }

    @Override
    public void destroyObject(PooledObject<TTransport> p) throws Exception {
        p.getObject().close();
    }
}
