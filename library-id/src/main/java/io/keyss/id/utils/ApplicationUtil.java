package io.keyss.id.utils;

import android.app.Application;

import java.lang.reflect.Field;

/**
 * @author Key
 * Time: 2022/03/01 19:53
 * Description:
 */
public class ApplicationUtil {
    private static Application mApp;

    public static Application getApplication() {
        if (null == mApp) {
            mApp = getApplicationByReflect();
        }
        return mApp;
    }

    private static Application getApplicationByReflect() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object thread = getActivityThread();
            Object app = activityThreadClass.getMethod("getApplication").invoke(thread);
            if (app == null) {
                return null;
            }
            return (Application) app;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static Object getActivityThread() {
        Object activityThread = getActivityThreadInActivityThreadStaticField();
        if (activityThread != null) return activityThread;
        return getActivityThreadInActivityThreadStaticMethod();
    }

    private static Object getActivityThreadInActivityThreadStaticMethod() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            return activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getActivityThreadInActivityThreadStaticField() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            return sCurrentActivityThreadField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
