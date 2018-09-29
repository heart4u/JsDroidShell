package com.jsdroid.shell.handler;

import android.net.LocalSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalSocketHandler extends Handler {

    public LocalSocketHandler(final LocalSocket localSocket) {
        super(new ISocket() {
            @Override
            public void close() throws IOException {
                localSocket.close();
            }

            @Override
            public InputStream input() throws IOException {
                return localSocket.getInputStream();
            }

            @Override
            public OutputStream out() throws IOException {
                return localSocket.getOutputStream();
            }
        });
    }

}
