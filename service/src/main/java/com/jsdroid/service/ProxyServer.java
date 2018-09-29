package com.jsdroid.service;

import android.net.LocalServerSocket;

import com.alibaba.fastjson.JSON;
import com.jsdroid.utils.LocalSocketUtil;

public class ProxyServer implements Runnable {
    String name;
    Object object;

    public ProxyServer(String name, Object object) {
        this.name = name;
        this.object = object;
    }

    @Override
    public void run() {
        LocalServerSocket localServerSocket = null;
        try {
            localServerSocket = new LocalServerSocket(name);
            for (; ; ) {
                LocalSocketUtil socketUtil = new LocalSocketUtil(localServerSocket.accept());
                try {
                    String line = socketUtil.readLine();
                    ProxyMethod method = JSON.parseObject(line, ProxyMethod.class);
                    Object resultObj;
                    if (method.params == null || method.params.length == 0) {
                        resultObj = object.getClass().getMethod(method.name).invoke(object);
                    } else {
                        Class[] types = new Class[method.params.length];
                        Object[] datas = new Object[method.params.length];
                        for (int i = 0; i < types.length; i++) {
                            types[i] = ClassLoader.getSystemClassLoader().loadClass(method.params[i].type);
                            try {
                                datas[i] = JSON.parseObject(method.params[i].data, types[i]);
                            } catch (Exception e) {
                            }
                        }
                        resultObj = object.getClass().getMethod(method.name, types).invoke(object, datas);
                    }
                    ProxyResult proxyResult = new ProxyResult();
                    if (resultObj != null) {
                        proxyResult.type = resultObj.getClass().getName();
                        proxyResult.data = JSON.toJSONString(resultObj);
                    }
                    socketUtil.sendLine(JSON.toJSONString(proxyResult));
                } catch (Exception e) {
                    socketUtil.close();
                }
            }
        } catch (Exception e) {
        }
    }
}
