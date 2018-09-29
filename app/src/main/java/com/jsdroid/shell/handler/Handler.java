package com.jsdroid.shell.handler;

import android.graphics.Bitmap;
import android.os.Process;

import com.alibaba.fastjson.JSON;
import com.jsdroid.shell.Main;
import com.jsdroid.shell.Server;
import com.jsdroid.shell.bean.CaptureInfo;
import com.jsdroid.shell.bean.CaptureOption;
import com.jsdroid.shell.bean.Event;
import com.jsdroid.shell.bean.EventType;
import com.jsdroid.shell.bean.Script;
import com.jsdroid.shell.script.JsDroidScript;
import com.jsdroid.shell.script.JsDroidScriptFactory;
import com.jsdroid.uiautomator.UiDevice;
import com.jsdroid.utils.BitmapUtil;
import com.jsdroid.utils.ByteUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Handler implements Runnable {
    public interface ISocket {
        public void close() throws IOException;

        public InputStream input() throws IOException;

        public OutputStream out() throws IOException;
    }

    ISocket socket;

    public Handler(ISocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //处理消息
        try {
            InputStreamReader reader = new InputStreamReader(socket.input(), "utf-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            for (; ; ) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
                Event event = JSON.parseObject(line, Event.class);
                doEvent(event);
            }
        } catch (Exception e) {

        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    public void sendEvent(final Event event) {
        Server.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String line = JSON.toJSONString(event);
                    sendLine(line);
                } catch (Exception e) {
                }
            }
        });

    }

    public synchronized void sendLine(String line) throws IOException {
        socket.out().write((line + "\n").getBytes("utf-8"));
        socket.out().flush();
    }

    private void doEvent(Event event) throws Exception {
        switch (event.type) {
            case TYPE_RUN:
                runScript(event);
                break;
            case TYPE_EXIT:
                exit();
                break;
            case TYPE_CAPTURE:
                capture(event);
                break;
            case TYPE_STOP_SCRIPT:
                //只能通过重启服务来停止脚本
                restart();
                break;
        }
    }

    /**
     * 截图
     *
     * @param event
     */
    private void capture(Event event) {
        CaptureOption captureOption = JSON.parseObject(event.data, CaptureOption.class);
        //获取节点
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            UiDevice.getInstance().dumpWindowHierarchy(outputStream);
        } catch (Exception e) {
        }
        try {
            event.data = outputStream.toString("utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        //发送截图结果
        ZipOutputStream zipOut = null;
        try {
            CaptureInfo captureInfo = new CaptureInfo();
            // 截图
            int rotation = UiDevice.getInstance().getRotation();
            int screenWidth = UiDevice.getInstance().getDisplayWidth();
            int screenHeight = UiDevice.getInstance().getDisplayHeight();
            int width;
            int height;
            if (captureOption.scale == 1) {
                width = screenWidth;
                height = screenHeight;
            } else {
                width = (int) (captureOption.scale * screenWidth + 0.5f);
                height = (int) (captureOption.scale * screenHeight + 0.5f);
            }
            if (width == 0 || height == 0) {
                width = screenWidth;
                height = screenHeight;
            }
            captureInfo.rotation = rotation;
            captureInfo.screenWidth = screenWidth;
            captureInfo.screenHeight = screenHeight;
            captureInfo.imageWidth = width;
            captureInfo.imageHeight = height;
            Bitmap image = null;
            try {
                image = BitmapUtil.takeScreenshot(rotation, width, height);
            } catch (Exception e) {
            }
            // 获取节点
            captureInfo.nodes = UiDevice.getInstance().getNodes();
            // 获取act
            try {
                captureInfo.act = UiDevice.getInstance().getAct();
            } catch (Exception e) {
            }
            ByteArrayOutputStream zipOutByteStream = new ByteArrayOutputStream();
            // 发送结果
            zipOut = new ZipOutputStream(zipOutByteStream);
            String infoJson = JSON.toJSONString(captureInfo);
            byte[] infoBytes = infoJson.getBytes();
            // 发送info
            ZipEntry entry = new ZipEntry("info");
            entry.setSize(infoBytes.length);
            zipOut.putNextEntry(entry);
            zipOut.write(infoBytes);
            if (image != null) {
                ByteArrayOutputStream bmpOut = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, captureOption.quality,
                        bmpOut);
                ZipEntry imageEntry = new ZipEntry("image");
                imageEntry.setSize(bmpOut.size());
                zipOut.putNextEntry(imageEntry);
                zipOut.write(bmpOut.toByteArray());
                image.recycle();
            }
            zipOut.close();
            socket.out().write(
                    ByteUtil.intToByteArray(zipOutByteStream.size()));
            socket.out().write(zipOutByteStream.toByteArray());
        } catch (Exception e) {

        } finally {
            try {
                zipOut.close();
            } catch (Exception e) {
            }
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }


    /**
     * 运行脚本
     *
     * @param event
     * @throws Exception
     */
    private void runScript(Event event) {
        event.type = EventType.TYPE_RUN_RESULT;
        try {
            Script script = JSON.parseObject(event.data, Script.class);
            JsDroidScript jsDroidScript = JsDroidScriptFactory.create(ClassLoader.getSystemClassLoader(), new File(script.dir), new File(script.dir, "dex"), script.text);
            jsDroidScript.setHandler(this);
            jsDroidScript.setPkg(script.pkg);
            jsDroidScript.dir = script.dir;
            Object result = jsDroidScript.run();
            event.data = JSON.toJSONString(result);
        } catch (Exception e) {
            e.printStackTrace();
            event.data = e.getMessage();
        }
        try {
            sendEvent(event);
        } catch (Exception e) {
        }
    }


    /**
     * 重启服务
     */
    private void restart() {
        try {
            Server.close();
            UiDevice.getInstance().disconnect();
        } finally {
            Main.main(new String[]{});
        }
    }

    /**
     * 退出服务
     */
    private void exit() {
        try {
            Server.close();
            UiDevice.getInstance().disconnect();
        } finally {
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    public void print(Object obj) {
        Event event = new Event();
        event.type = EventType.TYPE_LOG;
        event.data = JSON.toJSONString(obj);
        sendEvent(event);
    }

}
