package com.jsdroid.shell;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Looper;
import android.os.Process;

import com.alibaba.fastjson.JSON;
import com.jsdroid.shell.bean.Event;
import com.jsdroid.shell.bean.EventType;
import com.jsdroid.shell.bean.Script;
import com.jsdroid.utils.SocketUtil;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import eu.chainfire.libsuperuser.Shell;
import eu.chainfire.libsuperuser.StreamGobbler;

public class Main {
    static void startServer() {
        String app_process;
        if (new File("/system/bin/app_process32").exists()) {
            app_process = "/system/bin/app_process32";
        } else if (new File("/system/bin/app_process").exists()) {
            app_process = "/system/bin/app_process";
        } else {
            app_process = "app_process";
        }
        //开启另外的进程启动服务
        new Shell.Builder()
                .useSH()
                .addCommand("trap \"\" HUP")
                .addCommand("cd /data/local/tmp/")
                .addCommand(app_process + " -Djava.class.path=jsdroid_shell.apk / com.jsdroid.shell.Server")
                .setOnSTDOUTLineListener(new StreamGobbler.OnLineListener() {
                    @Override
                    public void onLine(String s) {
                        //退出本进程
                        System.out.println(s);

                    }
                })
                .setOnSTDERRLineListener(new StreamGobbler.OnLineListener() {
                    @Override
                    public void onLine(String s) {
                        //退出本进程
                        System.out.println(s);
                    }
                })
                .open();
    }

    public static void main(String[] args) {
        if (args != null && args.length == 1) {
            //输入运行
            try {
                SocketUtil socketUtil = new SocketUtil("127.0.0.1", 9800);
                Scanner scanner = new Scanner(System.in);
                for (; ; ) {
                    String s = scanner.nextLine();
                    if (s.equals("exit")) {
                        break;
                    }
                    Script script = new Script();
                    script.dir = "/data/local/tmp";
                    script.text = s;
                    Event event = new Event();
                    event.type = EventType.TYPE_RUN;
                    event.data = JSON.toJSONString(script);
                    String send = JSON.toJSONString(event);
                    socketUtil.sendLine(send);
                    String result = socketUtil.readLine();
                    System.out.println(result);
                }

            } catch (IOException e) {
            } finally {
                System.exit(0);
            }
        } else {
            checkStart();
            startServer();
            //测试
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            try {
                // Process.killProcess(Process.myPid());
            } catch (Throwable e) {
                //e.printStackTrace();
            }
           // System.exit(0);
        }


    }

    private static void checkStart() {
        LocalSocket localSocket = new LocalSocket();
        try {
            localSocket.connect(new LocalSocketAddress("jsdroid.shell"));
            localSocket.close();
            //已经启动该服务了，直接退出
            System.out.println("err:already start.");
            System.exit(0);
        } catch (IOException e) {

        }
    }
}
