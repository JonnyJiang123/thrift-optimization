package cn.mj.thrift.test;

import org.apache.commons.pool2.impl.BaseGenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


public class ClientPool extends MonitorablePool implements ModifiablePool<HelloService.Client>{
    private final GenericObjectPool<HelloService.Client> pool;
    public ClientPool(ClientFactory factory) {
        GenericObjectPoolConfig<HelloService.Client> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(2048);
        config.setMaxIdle(1024);
        this.pool = new GenericObjectPool<>(factory, config);
    }
    public HelloService.Client getClient() throws Exception {
        return pool.borrowObject();
    }
    public void returnClient(HelloService.Client client) throws Exception {
        pool.returnObject(client);
    }

    @Override
    public BaseGenericObjectPool<?> getPool() {
        return pool;
    }

    @Override
    public String getName() {
        return "cn.mj.thrift.test.ClientPool";
    }

    @Override
    public void resetConfig(GenericObjectPoolConfig<HelloService.Client> config) {
        this.pool.setConfig(config);
    }
}
