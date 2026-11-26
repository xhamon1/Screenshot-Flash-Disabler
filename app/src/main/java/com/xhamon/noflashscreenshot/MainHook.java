package com.xhamon.noflashscreenshot;

import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) return;

        XposedHelpers.findAndHookMethod(
            View.class,
            "setVisibility",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.thisObject;
                    if (isFlashView(view)) {
                        param.args[0] = View.GONE;
                        view.setAlpha(0f);
                    }
                }
            }
        );

        XposedHelpers.findAndHookMethod(
            View.class,
            "setAlpha",
            float.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.thisObject;
                    if (isFlashView(view)) {
                        param.args[0] = 0f;
                    }
                }
            }
        );
    }

    private boolean isFlashView(View view) {
        if (view == null || view.getId() == View.NO_ID) return false;
        try {
            String resName = view.getResources().getResourceEntryName(view.getId());
            return "screenshot_flash".equals(resName);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
