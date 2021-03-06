package com.github.fantasticlab.rpc.core.provider;

import com.github.fantasticlab.rpc.core.annotation.Frpc;
import com.github.fantasticlab.rpc.core.context.InvokeResponseContext;
import com.github.fantasticlab.rpc.core.exception.FrpcInvokeException;
import com.github.fantasticlab.rpc.core.exception.FrpcRegistryException;
import com.github.fantasticlab.rpc.core.context.InvokeRequestContext;
import com.github.fantasticlab.rpc.core.meta.Address;
import com.github.fantasticlab.rpc.core.meta.ProviderNode;
import com.github.fantasticlab.rpc.core.registry.ProviderRegistry;
import com.github.fantasticlab.rpc.core.registry.ZookeeperProviderRegistry;
import com.github.fantasticlab.rpc.core.example.HelloService;
import com.github.fantasticlab.rpc.core.util.NetUtils;
import com.github.fantasticlab.rpc.core.zookeeper.ZookeeperClient;
import com.github.fantasticlab.rpc.core.zookeeper.ZookeeperClientImpl;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistryImpl implements ServiceRegistry {

    private Integer port;

    private String group;

    private ProviderRegistry providerRegistry;

    private Map<String, ServiceNode> serviceMap = new ConcurrentHashMap<>();

    public ServiceRegistryImpl(String group,
                               Integer port,
                               ProviderRegistry providerRegistry) {
        this.group = group;
        this.port = port;
        this.providerRegistry = providerRegistry;
    }

    @Data
    @AllArgsConstructor
    private class ServiceNode {
        private Object obj;
        private Class<?> clazz;
    }

    @Override
    public void register(Class<?> clazz) throws FrpcRegistryException {

        try {
            Object service = clazz.newInstance();
            ServiceNode serviceNode = new ServiceNode(service, clazz);

            Frpc frpc = clazz.getAnnotation(Frpc.class);
            String registerValue = frpc.value();

            serviceMap.put(registerValue, serviceNode);

            Address address = new Address(NetUtils.getLocalIp(), this.port);
            ProviderNode providerNode = new ProviderNode(this.group, registerValue, address.toString());
            providerNode.setService(registerValue);
            providerNode.setGroup(this.group);

            providerNode.setAddress(address);
            long now = new Date().getTime();
            providerNode.setRegisterTime(now);
            providerNode.setRefreshTime(now);
            providerRegistry.register(providerNode);

//            Class<?>[] interfaces = clazz.getInterfaces();
//            for (Class<?> inter : interfaces) {
//                String interfaceName = inter.getName();
//                serviceMap.put(interfaceName, serviceNode);
//
//                ProviderNode providerNode = new ProviderNode();
//                providerNode.setService(interfaceName);
//                providerNode.setGroup(this.group);
//                providerNode.setAddress(NetUtils.getLocalIp() + ":" + NetUtils.getLocalUnusedPort());
//                long now = new Date().getTime();
//                providerNode.setRegisterTime(now);
//                providerNode.setRefreshTime(now);
//                providerRegistry.register(providerNode);
//            }




        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (FrpcRegistryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InvokeResponseContext invoke(InvokeRequestContext context) throws FrpcInvokeException {
        if (!serviceMap.containsKey(context.getService())) {
            throw new FrpcInvokeException("Service not exist", null);
        }

        ServiceNode serviceNode = serviceMap.get(context.getService());
        try {
            Method method = serviceNode.getClazz().getDeclaredMethod(
                    context.getMethod(), context.getArgTypes());
            Object result =  method.invoke(serviceNode.getObj(), context.getArgs());
            return new InvokeResponseContext(context, result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
