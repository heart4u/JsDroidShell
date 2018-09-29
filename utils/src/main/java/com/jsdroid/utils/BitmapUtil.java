package com.jsdroid.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.IUiAutomationConnection;
import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Surface;
import android.view.SurfaceControl;

import com.jsdroid.uiautomator.UiDevice;


public class BitmapUtil {
    private static float getDegreesForRotation(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_90: {
                return 360f - 90f;
            }
            case Surface.ROTATION_180: {
                return 360f - 180f;
            }
            case Surface.ROTATION_270: {
                return 360f - 270f;
            }
            default: {
                return 0;
            }
        }
    }

    public static Bitmap takeScreenshot(int screenWidth, int screenHeight) {
        int rotation = UiDevice.getInstance().getRotation();
        return takeScreenshot(rotation, screenWidth, screenHeight);
    }

    @SuppressLint("NewApi")
    public static Bitmap takeScreenshot(int rotation, int screenWidth,
                                        int screenHeight) {
        final int displayWidth = screenWidth;
        final int displayHeight = screenHeight;
        final float screenshotWidth;
        final float screenshotHeight;
        switch (rotation) {
            case UiAutomation.ROTATION_FREEZE_0: {
                screenshotWidth = displayWidth;
                screenshotHeight = displayHeight;
            }
            break;
            case UiAutomation.ROTATION_FREEZE_90: {
                screenshotWidth = displayHeight;
                screenshotHeight = displayWidth;
            }
            break;
            case UiAutomation.ROTATION_FREEZE_180: {
                screenshotWidth = displayWidth;
                screenshotHeight = displayHeight;
            }
            break;
            case UiAutomation.ROTATION_FREEZE_270: {
                screenshotWidth = displayHeight;
                screenshotHeight = displayWidth;
            }
            break;
            default: {
                return null;
            }
        }

        Bitmap screenShot = null;
        try {
            screenShot = SurfaceControl.screenshot((int) screenshotWidth,
                    (int) screenshotHeight);
        } catch (Throwable re) {
        }
        try {
            if (screenShot == null) {
                IUiAutomationConnection ac = null;
                Field declaredField = UiAutomation.class
                        .getDeclaredField("mUiAutomationConnection");
                declaredField.setAccessible(true);
                ac = (IUiAutomationConnection) declaredField.get(UiDevice
                        .getInstance().getUiAutomation());
                screenShot = ac.takeScreenshot((int) screenshotWidth,
                        (int) screenshotHeight);
            }
        } catch (Exception e) {
            return null;
        }
        if (screenShot == null) {
            return null;
        }
        if (rotation != UiAutomation.ROTATION_FREEZE_0) {
            Bitmap unrotatedScreenShot = Bitmap.createBitmap(displayWidth,
                    displayHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(unrotatedScreenShot);
            canvas.translate(unrotatedScreenShot.getWidth() / 2,
                    unrotatedScreenShot.getHeight() / 2);
            canvas.rotate(getDegreesForRotation(rotation));
            canvas.translate(-screenshotWidth / 2, -screenshotHeight / 2);
            canvas.drawBitmap(screenShot, 0, 0, null);
            canvas.setBitmap(null);
            screenShot.recycle();
            screenShot = unrotatedScreenShot;
        }
        // Optimization
        screenShot.setHasAlpha(false);
        return screenShot;
    }

    public static PicUtil.Pic bitmap2Pic(Bitmap bmp) {
        PicUtil.Pic pic = new PicUtil.Pic();
        pic.width = bmp.getWidth();
        pic.height = bmp.getHeight();
        pic.pixels = new int[pic.width * pic.height];
        bmp.getPixels(pic.pixels, 0, pic.width, 0, 0, pic.width, pic.height);
        return pic;
    }

    public static void save(String picFile, Bitmap bmp) {
        File dir = new File(picFile).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(picFile);
            bmp.compress(CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

    public static void save(String path, String picName, Bitmap bmp) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File picFile = new File(dir, picName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(picFile);
            bmp.compress(CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }

    }

    public static Bitmap read(String file) {
        byte[] data = FileUtil.readBytes(file);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static PicUtil.Pic readPic(String file) {
        Bitmap bmp = read(file);
        PicUtil.Pic pic = bitmap2Pic(bmp);
        bmp.recycle();
        return pic;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static Bitmap takeScreenshot() {
        return UiDevice.getInstance().getUiAutomation().takeScreenshot();
    }
}
