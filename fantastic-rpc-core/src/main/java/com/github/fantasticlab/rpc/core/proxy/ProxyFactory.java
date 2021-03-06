package com.github.fantasticlab.rpc.core.proxy;

import java.lang.reflect.InvocationHandler;

public interface ProxyFactory {

    <T> T getProxy(Class<T> clz);

}
