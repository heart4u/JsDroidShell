package com.jsdroid.shell.server;

import com.jsdroid.shell.Server;
import com.jsdroid.shell.handler.SocketHandler;

import java.net.ServerSocket;

public class SocketServer implements Runnable {
    ServerSocket serverSocket = null;

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(9800);
            for (; ; ) {
                Server.execute(new SocketHandler(serverSocket.accept()));
            }
        } catch (Exception e) {
        } finally {
            close();
            //端口占用，退出
            System.exit(1);
        }
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (Exception e) {
        }
    }
}
