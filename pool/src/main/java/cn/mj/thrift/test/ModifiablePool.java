package cn.mj.thrift.test;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public interface ModifiablePool<T> {
    void resetConfig(GenericObjectPoolConfig<T> config);
}
