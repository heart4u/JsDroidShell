package com.jsdroid.service;

import com.alibaba.fastjson.JSON;
import com.jsdroid.utils.LocalSocketUtil;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {
    String name;

    public ServiceProxy(String name) {
        this.name = name;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ProxyMethod proxyMethod = new ProxyMethod();
        proxyMethod.name = method.getName();
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        proxyMethod.params = new ProxyParam[methodParameterTypes.length];
        for (int i = 0; i < methodParameterTypes.length; i++) {
            ProxyParam param = new ProxyParam();
            param.type = methodParameterTypes[i].getName();
            try {
                param.data = JSON.toJSONString(args[i]);
            } catch (Exception e) {
            }
            proxyMethod.params[i] = param;
        }
        String line = JSON.toJSONString(proxyMethod);
        LocalSocketUtil socketUtil = new LocalSocketUtil(name);
        try {
            //发送指令
            socketUtil.sendLine(line);
            //读取运行结果
            line = socketUtil.readLine();
            ProxyResult proxyResult = JSON.parseObject(line, ProxyResult.class);
            if (proxyResult.type != null && proxyResult.data != null) {
                Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(proxyResult.type);
                return JSON.parseObject(proxyResult.data, aClass);
            }
        } finally {
            socketUtil.close();
        }
        return null;
    }
}
