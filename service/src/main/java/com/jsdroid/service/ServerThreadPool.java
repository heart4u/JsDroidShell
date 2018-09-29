package com.jsdroid.service;

import java.util.concurrent.ExecutorService;

public class ServerThreadPool {
    private static ExecutorService threadPool;
    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }
}
