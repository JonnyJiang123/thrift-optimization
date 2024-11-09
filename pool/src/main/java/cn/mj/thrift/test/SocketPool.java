package cn.mj.thrift.test;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.thrift.transport.TTransport;

public class SocketPool {
    private final GenericObjectPool<TTransport> pool;

    public SocketPool(SocketFactory socketFactory) {
        this.pool = new GenericObjectPool<>(socketFactory);
    }

    public TTransport getSocket() throws Exception {
        return pool.borrowObject();
    }

    public void returnSocket(TTransport socket) throws Exception {
        pool.returnObject(socket);
    }
}
