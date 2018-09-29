package com.jsdroid.view;

import android.app.Activity;

public interface IScriptView {
    void onCreate(Activity activity);

    void onResume();

    void onPause();

    void onDestroy();
    
}
