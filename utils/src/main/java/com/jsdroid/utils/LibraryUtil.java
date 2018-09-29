package com.jsdroid.utils;

import android.os.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class LibraryUtil {

    public static void loadAllSoLib() {
        File sdPluginDir = new File("/sdcard/jsdroid/plugin");
        File tmpPluginDir = new File("/data/local/tmp/jsdroid/plugin");
        File tmpJsDroidDir = new File("/data/local/tmp/jsdroid/");
        File tmpArmeabiLibDir = new File("/data/local/tmp/jsdroid/lib/armeabi");
        File tmpArmeabV7aiLibDir = new File("/data/local/tmp/jsdroid/lib/armeabi-v7a");
        File tmpX86LibDir = new File("/data/local/tmp/jsdroid/lib/x86");
        File shellApkFilr = new File("/data/local/tmp/jsdroid_shell.apk");
        tmpJsDroidDir.mkdirs();
        //0.app将assets里面的plugin文件夹解压到/sdcard/jsdroid/plugin文件夹
        //1.shell将plugin文件夹拷贝到/data/local/tmp/jsdroid/plugin文件夹
        if (sdPluginDir.exists()) {
            tmpPluginDir.mkdirs();
            try {
                org.apache.commons.io.FileUtils.copyFileToDirectory(sdPluginDir, tmpPluginDir);
            } catch (IOException e) {
            }
        }
        //2.shell将/data/local/tmp/jsdroid/plugin文件夹的所有apk里面的lib文件夹解压到/data/local/tmp/jsdroid/lib
        File[] files = tmpPluginDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".apk")) {
                    try {
                        ApkUtil.zipLib(file, tmpJsDroidDir);
                    } catch (IOException e) {
                    }
                }
            }
        }
        //3.shell将jsdroid_shell.apk的lib解压到/data/local/tmp/jsdroid/lib
        if (shellApkFilr.exists()) {
            try {
                ApkUtil.zipLib(shellApkFilr, tmpJsDroidDir);
            } catch (IOException e) {
            }
        }
        //4.加载/data/local/tmp/jsdroid/lib下面的所有so
        //优选加载armeabi
        if (loadLibOnDir(tmpArmeabiLibDir)) {
            return;
        }
        if (loadLibOnDir(tmpX86LibDir)) {
            return;
        }
        if (loadLibOnDir(tmpArmeabV7aiLibDir)) {
            return;
        }
    }

    private static boolean loadLibOnDir(File dir) {

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().endsWith(".so")) {
                    continue;
                }
                try {
                    file.setExecutable(true);
                    file.setReadable(true);
                    file.setWritable(true);
                    System.load(file.getPath());
                    System.out.println("load ok:" + dir.getPath());
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }
}
