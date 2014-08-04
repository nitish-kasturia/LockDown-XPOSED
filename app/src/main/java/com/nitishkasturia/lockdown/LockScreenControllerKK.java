package com.nitishkasturia.lockdown;

import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Nitish Kasturia on 2014-08-03.
 * Xposed controller for KK
 */
public class LockScreenControllerKK implements IXposedHookLoadPackage{
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if(Build.VERSION.SDK_INT > 18){
            if(loadPackageParam.packageName.equals("com.android.keyguard")){
                XposedBridge.log("LockDown: Loaded package com.android.keyguard");

            }
        }
    }
}