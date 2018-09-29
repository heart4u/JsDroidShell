package com.jsdroid.uiautomator.eample;

import android.app.ActivityThread;
import android.os.Looper;

import com.jsdroid.uiautomator.UiDevice;

public class Main {


    public static void main(String args[]) {
        Looper.prepare();
        try {
            ActivityThread.systemMain();
        } catch (Exception e) {
        }
        try {
            UiDevice.getInstance().initialize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        Looper.loop();
    }
}
