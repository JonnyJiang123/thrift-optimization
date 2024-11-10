package cn.mj.thrift.test;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public interface ModifiablePool<T> extends NamedPool{
    void resetConfig(GenericObjectPoolConfig<T> config);
}
