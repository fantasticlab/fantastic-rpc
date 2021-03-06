package com.github.fantasticlab.rpc.core.initializer;

import com.github.fantasticlab.rpc.core.exception.FrpcZookeeperException;
import com.github.fantasticlab.rpc.core.proxy.JdkProxyFactory;
import com.github.fantasticlab.rpc.core.proxy.ProxyFactory;
import com.github.fantasticlab.rpc.core.example.HelloService;

public class ConsumerInitializer {

    private String zk;

    private String group;

    private ProxyFactory proxyFactory;

    public ConsumerInitializer(String zk, String group) throws FrpcZookeeperException, InterruptedException {
        this.zk = zk;
        this.group = group;
        this.proxyFactory = new JdkProxyFactory(this.zk, this.group);
    }

    public <T> T getService(Class<T> clazz) {
        return this.proxyFactory.getProxy(clazz);
    }

}
