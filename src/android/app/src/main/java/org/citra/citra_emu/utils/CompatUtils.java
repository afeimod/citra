package org.citra.citra_emu.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.AttrRes;

import org.citra.citra_emu.CitraApplication;

public class CompatUtils {
    public static Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if ((context instanceof ContextWrapper)) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        }

        return ((Activity) context);
    }
}
