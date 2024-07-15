package com.outlook.wn123o.mantis;

import android.content.Context;
import android.provider.Settings;

import com.outlook.wn123o.mantis.widget.FloatWindow;

import java.lang.ref.WeakReference;

public final class MantisHelper {
    private MantisHelper() {}

    private static WeakReference<FloatWindow> mWindow;

    public static boolean showFloatWindow(Context applicationContext) {
        if (!Settings.canDrawOverlays(applicationContext)) return false;
        if (mWindow == null) {
            mWindow = new WeakReference<>(new FloatWindow(applicationContext));
        }
        FloatWindow window = mWindow.get();
        if (window != null) {
            window.show();
        }
        return true;
    }

    public void isWindowDarkMode(boolean darkMode) {
        if (mWindow == null) return;
        FloatWindow window = mWindow.get();
        if (window != null) {
            window.isDarkMode(darkMode);
        }
    }

    public static void dismissFloatWindow() {
        if (mWindow == null) return;
        FloatWindow window = mWindow.get();
        if (window != null) {
            window.dismiss();
        }
    }
}
