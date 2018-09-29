package com.jsdroid.shell.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketHandler extends Handler {
    public SocketHandler(final Socket socket) {
        super(new ISocket() {
            @Override
            public void close() throws IOException {
                socket.close();
            }

            @Override
            public InputStream input() throws IOException {
                return socket.getInputStream();
            }

            @Override
            public OutputStream out() throws IOException {
                return socket.getOutputStream();
            }
        });
    }
}
