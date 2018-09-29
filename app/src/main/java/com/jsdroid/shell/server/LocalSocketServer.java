package com.jsdroid.shell.server;

import android.net.LocalServerSocket;

import com.jsdroid.shell.Server;
import com.jsdroid.shell.handler.LocalSocketHandler;

import java.io.IOException;

public class LocalSocketServer implements Runnable {
    LocalServerSocket serverSocket = null;

    @Override
    public void run() {

        try {
            serverSocket = new LocalServerSocket("jsdroid.shell");
            for (; ; ) {
                Server.execute(new LocalSocketHandler(serverSocket.accept()));
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
