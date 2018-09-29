package com.jsdroid.input;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.inputmethod.InputMethodInfo;

import com.android.internal.view.IInputMethodManager;

import java.util.ArrayList;
import java.util.List;

public class InputMethod {


    public static List<String> list() {
        List<String> ret = new ArrayList<String>();
        IInputMethodManager mImm;
        mImm = IInputMethodManager.Stub.asInterface(ServiceManager
                .getService("input_method"));
        try {

            for (InputMethodInfo info : mImm.getInputMethodList()) {
                ret.add(info.getId());
            }
        } catch (RemoteException e) {
        }
        return ret;
    }

    public static void setIME(String id) {
        IInputMethodManager mImm;
        mImm = IInputMethodManager.Stub.asInterface(ServiceManager
                .getService("input_method"));
        try {
            mImm.setInputMethodEnabled(id, true);
            mImm.setInputMethod(null, id);
        } catch (RemoteException e) {
        }
    }

    public static boolean closeIME(String id) {
        IInputMethodManager mImm;
        mImm = IInputMethodManager.Stub.asInterface(ServiceManager
                .getService("input_method"));
        try {
            mImm.setInputMethodEnabled(id, false);
            for (InputMethodInfo info : mImm.getEnabledInputMethodList()) {
                mImm.setInputMethodEnabled(info.getId(), true);
                mImm.setInputMethod(null, info.getId());
                return true;
            }
        } catch (RemoteException e) {
        }
        return false;
    }
}
