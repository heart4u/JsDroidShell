package com.jsdroid.service;

import java.lang.reflect.Proxy;

public class ProxyServiceManager {

    public static <T> T getService(String name, Class<T> serviceClass) {
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        ServiceProxy proxyFactory = new ServiceProxy(name);
        return (T) Proxy.newProxyInstance(classLoader, interfaces,
                proxyFactory);
    }

    public static void addService(String name, Object service) {
        ServerThreadPool.execute(new ProxyServer(name,service));
    }
}
