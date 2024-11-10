package cn.mj.thrift.test;

import org.apache.commons.pool2.impl.BaseGenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.protocol.TProtocol;



public class ProtocolPool extends MonitorablePool implements ModifiablePool<TProtocol>{
    private final GenericObjectPool<TProtocol> pool;

    public ProtocolPool(ProtocolFactory factory) {
        GenericObjectPoolConfig<TProtocol> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(3096);
        config.setMaxIdle(2048);
        this.pool = new GenericObjectPool<>(factory,config);
    }

    public TProtocol getProtocol() throws Exception {
        return pool.borrowObject();
    }

    public void returnProtocol(TProtocol protocol) throws Exception {
        pool.returnObject(protocol);
    }

    @Override
    public BaseGenericObjectPool<?> getPool() {
        return pool;
    }

    @Override
    public String getName() {
        return "cn.mj.thrift.test.ProtocolPool";
    }

    @Override
    public void resetConfig(GenericObjectPoolConfig<TProtocol> config) {
        this.pool.setConfig(config);
    }
}
