package com.jsdroid.shell;


import android.app.ActivityThread;
import android.os.Looper;

import com.jsdroid.shell.server.LocalSocketServer;
import com.jsdroid.shell.server.SocketServer;
import com.jsdroid.uiautomator.UiDevice;
import com.jsdroid.utils.LibraryUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ExecutorService threadPool;
    private static LocalSocketServer localSocketServer;
    private static SocketServer socketServer;

    public static void main(String[] args) {
        System.out.println("server start.");
        threadPool = Executors.newCachedThreadPool();
        //启动服务
        localSocketServer = new LocalSocketServer();
        socketServer = new SocketServer();
        threadPool.execute(localSocketServer);
        threadPool.execute(socketServer);
        //加载so
        LibraryUtil.loadAllSoLib();
        //初始化uiautomator
        Looper.prepare();
        try {
            ActivityThread.systemMain();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            UiDevice.getInstance().initialize();
        } catch (Throwable e) {
            e.printStackTrace();
            //不支持，或者被占用，退出
            System.exit(1);
        }
        Looper.loop();
    }

    public static void close() {
        localSocketServer.close();
        socketServer.close();
    }

    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }
}
