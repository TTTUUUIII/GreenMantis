package com.outlook.wn123o.mantis.util;

import android.content.res.Resources;
import android.util.TypedValue;

public final class AndroidUtils {
    private AndroidUtils() {}

    public static int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
