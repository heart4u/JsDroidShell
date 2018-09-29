/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jsdroid.uiautomator;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityThread;
import android.app.Service;
import android.app.UiAutomation;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * @hide
 */
public class ContextUiAutomatorBridge extends UiAutomatorBridge {

    private Context mContext;

    public ContextUiAutomatorBridge(UiAutomation uiAutomation) {
        super(uiAutomation);

        // Enable multi-window support for API level 21 and up
        if (UiDevice.API_LEVEL_ACTUAL >= Build.VERSION_CODES.LOLLIPOP) {
            // Subscribe to window information
            AccessibilityServiceInfo info = uiAutomation.getServiceInfo();
            info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            uiAutomation.setServiceInfo(info);
        }

        try {
            mContext = ActivityThread.currentActivityThread().getSystemContext();
        } catch (Throwable e) {
        }
        try {
            if (mContext == null) {
                mContext = ActivityThread.currentActivityThread().getApplication();
            }
        } catch (Exception e) {
        }
    }

    public Display getDefaultDisplay() {
        WindowManager windowManager = (WindowManager)
                mContext.getSystemService(Service.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay();
    }

    @Override
    public int getRotation() {
        return getDefaultDisplay().getRotation();
    }

    @Override
    public boolean isScreenOn() {
        PowerManager pm = (PowerManager)
                mContext.getSystemService(Service.POWER_SERVICE);
        return pm.isScreenOn();
    }

    public long getSystemLongPressTime() {
        return ViewConfiguration.getLongPressTimeout();
    }

    Context getContext() {
        return mContext;
    }
}
