package com.outlook.wn123o.mantis;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.RequiresPermission;

import com.outlook.wn123o.mantis.widget.SummaryWindow;

import java.lang.ref.WeakReference;

public final class MantisHelper {
    private MantisHelper() {}
    private static WeakReference<SummaryWindow> mWindow;

    @RequiresPermission("android.permission.SYSTEM_ALERT_WINDOW")
    public static boolean showSummaryWindow(Context applicationContext) {
        if (!Settings.canDrawOverlays(applicationContext)) return false;
        if (mWindow == null) {
            mWindow = new WeakReference<>(new SummaryWindow(applicationContext));
        }
        SummaryWindow window = mWindow.get();
        if (window != null) {
            window.show();
        } else {
            return false;
        }
        return true;
    }

    public void isUseDarkWindow(boolean darkMode) {
        if (mWindow == null) return;
        SummaryWindow window = mWindow.get();
        if (window != null) {
            window.isUseDarkMode(darkMode);
        }
    }

    public static void dismissSummaryWindow() {
        if (mWindow == null) return;
        SummaryWindow window = mWindow.get();
        if (window != null) {
            window.dismiss();
        }
    }
}
