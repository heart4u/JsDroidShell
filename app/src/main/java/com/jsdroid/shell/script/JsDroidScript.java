package com.jsdroid.shell.script;


import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.RemoteException;

import com.jsdroid.findimg.FindImg;
import com.jsdroid.findpic.FindPic;
import com.jsdroid.input.InputMethod;
import com.jsdroid.service.ProxyServiceManager;
import com.jsdroid.shell.handler.Handler;
import com.jsdroid.uiautomator.BySelector;
import com.jsdroid.uiautomator.PointerGesture;
import com.jsdroid.uiautomator.UiDevice;
import com.jsdroid.uiautomator.UiObject2;
import com.jsdroid.utils.BitmapUtil;
import com.jsdroid.utils.FileUtil;
import com.jsdroid.utils.HttpUtil;
import com.jsdroid.utils.ShellUtil;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import groovy.lang.Script;

public abstract class JsDroidScript extends Script {
    public static final String VERSION = "2.0";

    public interface IApp {

        void toast(String text);

        String getConfig(String name);

        void saveConfig(String name, String value);

    }

    public interface IInput {

        void clear(int before, int after);

        void input(String text);

    }

    IApp app;
    IInput input;

    private Compiler compiler;

    public void setPkg(String pkg) {
        this.pkg = pkg;
        app = ProxyServiceManager.getService(pkg + ".app", IApp.class);
        input = ProxyServiceManager.getService(pkg + ".input", IInput.class);
    }

    private String pkg;
    public UiDevice device = UiDevice.getInstance();

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private Handler handler;

    public String dir;

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public String version() {
        return VERSION;
    }

    @Override
    public void run(File file, String[] arguments) throws CompilationFailedException, IOException {
    }

    private JsDroidScript evaluate(String code, String filename) throws Exception {
        JsDroidScript script = compiler.evaluate(code, filename, false);
        script.setBinding(getBinding());
        script.run();
        return script;
    }

    /**
     * 执行字符串
     *
     * @param code
     * @return
     * @throws Exception
     */
    public Object eval(String code) throws Exception {
        JsDroidScript object = evaluate(code, null);
        return object.run();
    }

    /**
     * 加载文件为对象
     *
     * @param file
     * @return
     * @throws Exception
     */
    public JsDroidScript load(String file) throws Exception {
        String code = FileUtil.read(file);
        JsDroidScript script = compiler.evaluate(code, file, false);
        return script;
    }

    /**
     * 执行shell命令
     *
     * @param shell
     * @return
     */
    public String exec(String shell) {
        return ShellUtil.exec(shell);
    }

    /**
     * 按下
     *
     * @param x
     * @param y
     */
    public void touchDown(int x, int y) {
        device.getInteractionController().touchDown(x, y);
    }

    /**
     * 抬起
     *
     * @param x
     * @param y
     */
    public void touchUp(int x, int y) {
        device.getInteractionController().touchUp(x, y);
    }

    /**
     * 滑动
     *
     * @param x
     * @param y
     */
    public void touchMove(int x, int y) {
        device.getInteractionController().touchMove(x, y);
    }

    /**
     * 区域找色
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param colors
     * @return
     */
    public Point findColor(int left, int top, int right, int bottom, String colors) {
        return null;
    }


    /**
     * 获取屏幕某点颜色
     *
     * @param x
     * @param y
     * @return
     */
    int getColor(int x, int y) {
        if (screen != null) {
            return screen.getPixel(x, y);
        }
        return BitmapUtil.takeScreenshot().getPixel(x, y);
    }

    int red(int color) {
        return Color.red(color);
    }

    int green(int color) {
        return Color.green(color);
    }

    int blue(int color) {
        return Color.blue(color);
    }

    Bitmap screen;

    /**
     * 锁定屏幕
     */
    public synchronized void lockScreen() {
        screen = BitmapUtil.takeScreenshot();
    }

    /**
     * 解锁屏幕
     */
    public synchronized void unlockScreen() {
        screen = null;
    }

    /**
     * 区域找图
     *
     * @param pngFile
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param offset
     * @param sim
     * @return
     */
    public Point findPic(String pngFile, int left, int top, int right, int bottom, int offset, float sim) {
        Bitmap screen;
        if (this.screen != null) {
            screen = this.screen;
        } else {
            screen = BitmapUtil.takeScreenshot();
        }
        if (new File(pngFile).exists()) {
            return FindPic.findPic(screen, BitmapUtil.read(pngFile),
                    (int) left, (int) top, (int) right, (int) bottom,
                    (int) offset, (float) sim);
        } else {
            return FindPic.findPic(screen, BitmapUtil.read(new File(dir, pngFile).getPath()),
                    (int) left, (int) top, (int) right, (int) bottom,
                    (int) offset, (float) sim);
        }
    }

    /**
     * 高级找图
     *
     * @param pngFile
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param offset
     * @param sim
     * @return
     */
    public FindImg.Rect findImg(String pngFile, int left, int top, int right, int bottom, int offset, float sim) {
        try {
            Bitmap screen;
            if (this.screen != null) {
                screen = this.screen;
            } else {
                screen = BitmapUtil.takeScreenshot();
            }
            Bitmap image;
            if (new File(pngFile).exists()) {
                image = BitmapUtil.read(pngFile);
            } else {
                image = BitmapUtil.read(new File(dir, pngFile).getPath());
            }
            if (image == null) {
                return null;
            }
            int distance = 1;
            int level = 8;
            if (sim > 0.7) {
                distance = 2;
                level = 16;
            }
            return FindImg.findImg(screen, image, level, left, top, right, bottom, offset, distance, sim);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 截图
     *
     * @param file    保存的文件为准
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param quality
     * @param type    保存的类型：png/jpg
     */
    public void screenshot(String file, int left, int top, int right, int bottom, int quality, String type) {
        Bitmap bitmap = BitmapUtil.takeScreenshot();
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap1.compress(type.equals("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, quality, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 截图
     *
     * @param file
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param quality
     */
    public void screenshot(String file, int left, int top, int right, int bottom, int quality) {
        screenshot(file, left, top, right, bottom, quality, "png");
    }

    /**
     * 截图
     *
     * @param file
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void screenshot(String file, int left, int top, int right, int bottom) {
        screenshot(file, left, top, right, bottom, 100);
    }

    /**
     * 启动app
     *
     * @param pkg 包名
     */
    public void runApp(String pkg) {
        try {
            Intent intent = device.getContext().getPackageManager().getLaunchIntentForPackage(pkg);
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    /**
     * 启动activity
     *
     * @param intent
     */
    public void startActivity(Intent intent) {
        IActivityManager service = ActivityManager.getService();
        try {
            service.startActivity(null, "android", intent, null, null, null, 0, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS, null, null);
        } catch (RemoteException e) {
        }
    }

    /**
     * 锁定屏幕
     */
    public void lockDevice() {
        try {
            device.sleep();
        } catch (RemoteException e) {
        }
    }

    /**
     * 解锁屏幕
     */
    public void unlockDevice() {
        try {
            device.wakeUp();
            KeyguardManager keyguardManager = (KeyguardManager) device.getContext().getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.newKeyguardLock("unLock").disableKeyguard();
        } catch (Throwable e) {
        }

    }

    /**
     * 找节点
     *
     * @param by
     * @return
     */
    public UiObject2 findObject(BySelector by) {
        return device.findObject(by);
    }


    /**
     * 线程等待一段时间，单位：毫秒
     *
     * @param time
     * @throws InterruptedException
     */
    public void delay(long time) throws InterruptedException {
        Thread.sleep(time);
    }

    /**
     * 线程等待一段时间，单位：毫秒
     *
     * @param time
     * @throws InterruptedException
     */
    public void sleep(long time) throws InterruptedException {
        delay(time);
    }

    /**
     * 点击屏幕
     *
     * @param x
     * @param y
     */
    public void tap(int x, int y) {
        device.click(x, y);
    }

    /**
     * 点击屏幕
     *
     * @param x
     * @param y
     */
    public void click(int x, int y) {
        device.click(x, y);
    }

    /**
     * 滑动
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param steps
     */
    public void swipe(int x1, int y1, int x2, int y2, int steps) {
        device.swipe(x1, y1, x2, y2, steps);
    }

    /**
     * 滑动
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void swipe(int x1, int y1, int x2, int y2) {
        swipe(x1, y1, x2, y2, (Math.abs(x1 - x2) + Math.abs(y1 - y2)) / 20);
    }

    /**
     * 拖动，精准滑动
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param steps
     */
    public void drag(int x1, int y1, int x2, int y2, int steps) {
        device.drag(x1, y1, x2, y2, steps);
    }

    /**
     * 拖动，精准滑动
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void drag(int x1, int y1, int x2, int y2) {
        drag(x1, y1, x2, y2, (Math.abs(x1 - x2) + Math.abs(y1 - y2)) / 20);
    }

    /**
     * 获取当前activity组件名
     *
     * @return
     */
    public String getActivity() {
        try {
            String result = ShellUtil.exec("dumpsys activity top");
            String lines[] = result.split("\n");
            for (String line : lines) {
                if (line.contains("ACTIVITY") && line.contains("/")) {
                    String sps[] = line.split(" ");
                    for (String sp : sps) {
                        if (sp.contains("/")) {
                            return sp;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 获取系统时间，单位毫秒
     *
     * @return
     */
    public long time() {
        return System.currentTimeMillis();
    }

    /**
     * 获取系统时间，单位纳秒
     *
     * @return
     */
    public long nanoTime() {
        return System.nanoTime();
    }

    public String read(String file, String charset) {
        try {
            return FileUtils.readFileToString(new File(file), charset);
        } catch (IOException e) {
        }
        return null;
    }

    public String read(String file) {
        return read(file, "utf-8");
    }

    public void write(String file, String content, String charset) {
        try {
            FileUtils.writeStringToFile(new File(file), content, charset);
        } catch (IOException e) {
        }
    }

    public void write(String file, String content) {
        write(file, content, "utf-8");
    }

    public void append(String file, String content) {
        FileUtil.append(file, content);
    }

    public void moveFileToDir(String from, String dir) {
        File fromFile = new File(from);
        File toFile = new File(dir);
        try {
            FileUtils.copyFileToDirectory(fromFile, toFile);
            fromFile.delete();
        } catch (IOException e) {
        }

    }

    public void moveFileToFile(String from, String to) {
        try {
            File fromFile = new File(from);
            FileUtils.copyFile(fromFile, new File(to));
        } catch (IOException e) {
        }
    }

    public void copyFileToDir(String from, String dir) {
        try {
            FileUtils.copyFileToDirectory(new File(from), new File(dir));
        } catch (IOException e) {
        }

    }

    public void copyFileToFile(String from, String to) {
        try {
            FileUtils.copyFile(new File(from), new File(to));
        } catch (IOException e) {
        }
    }

    public void deleteFile(String file) {
        new File(file).delete();
    }

    public void createFile(String file) {
        try {
            new File(file).createNewFile();
        } catch (IOException e) {
        }
    }

    public void mkdir(String file) {
        new File(file).mkdir();
    }

    public void mkdirs(String file) {
        new File(file).mkdirs();
    }

    public String httpGet(String url) {
        try {
            return HttpUtil.get(url);
        } catch (IOException e) {
        }
        return null;
    }

    public String httpGet(String url, Map<String, String> params) {
        try {
            return HttpUtil.get(url, params);
        } catch (IOException e) {
        }
        return null;
    }

    public String httpPost(String url, Map<String, String> params) {
        try {
            return HttpUtil.post(url, params);
        } catch (IOException e) {
        }
        return null;
    }

    public void killApp(String pkg) {
        exec("am force-stop " + pkg);
    }

    public void toast(String text) {
        app.toast(text);
    }

    /**
     * 打开输入法
     */
    public void openInputMethod() throws InterruptedException {
        String id = pkg + "/" + new ComponentName(pkg, "com.jsdroid.input.Input").getShortClassName();
        InputMethod.setIME(id);
        //延时一秒，防止用户输入失败
        delay(1000);
    }

    /**
     * 关闭输入法
     */
    public void closeInputMethod() {
        String id = pkg + "/" + new ComponentName(pkg, "com.jsdroid.input.Input").getShortClassName();
        InputMethod.setIME(id);
        InputMethod.closeIME(id);
    }

    /**
     * 输入文字
     *
     * @param text
     */
    public void inputText(String text) {
        if (text != null && text.length() > 0) {
            input.input(text);
        }

    }

    /**
     * 清除文字
     *
     * @param before
     * @param after
     */
    public void clearText(int before, int after) {
        input.clear(before, after);
    }

    /**
     * 输出日志
     *
     * @param data
     */
    public void print(Object data) {
        handler.print(data);
    }

    /**
     * 手势操作
     *
     * @param pointerGestures
     */
    public void performGestures(PointerGesture... pointerGestures) {
        device.performGesture(pointerGestures);
    }

}
