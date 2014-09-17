package com.nitishkasturia.lockdown;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Nitish Kasturia on 2014-08-03.
 * Xposed controller for KK
 */
public class LockScreenControllerKK implements IXposedHookLoadPackage{

    Context lockdownContext = null;

    boolean pinQuickUnlock = false;

    ArrayList<Profile> profileList = new ArrayList<Profile>();

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if(Build.VERSION.SDK_INT > 18){
            if(loadPackageParam.packageName.equals("com.android.keyguard")){
                XposedBridge.log("LockDown: Loaded package com.android.keyguard");

                XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.keyguard.KeyguardAbsKeyInputView", loadPackageParam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //Create lockdown context from Android context
                        lockdownContext = ((Context) param.args[0]).createPackageContext("com.nitishkasturia.lockdown", 0);

                        //Load lockdown global preferences
                        pinQuickUnlock = PreferenceManager.getDefaultSharedPreferences(lockdownContext).getBoolean("preference_quick_unlock", false);

                        String[] fileList = lockdownContext.fileList();
                        for(String file : fileList){
                            try{
                                FileInputStream fileIn = lockdownContext.openFileInput(file);
                                ObjectInputStream objectIn = new ObjectInputStream(fileIn);

                                Profile profile = (Profile) objectIn.readObject();

                                fileIn.close();
                                objectIn.close();

                                profileList.add(profile);
                            }catch (FileNotFoundException e){
                                e.printStackTrace();
                            }catch (StreamCorruptedException e){
                                e.printStackTrace();
                            }catch (IOException e){
                                e.printStackTrace();
                            }catch (ClassNotFoundException e){
                                e.printStackTrace();
                            }
                        }
                    }
                });

                XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardAbsKeyInputView",loadPackageParam.classLoader, "onFinishInflate", new XC_MethodHook(1) {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        if (pinQuickUnlock) {
                            final TextView passwordEntry = (TextView) XposedHelpers.getObjectField(param.thisObject, "mPasswordEntry");
                            if(passwordEntry != null){
                                passwordEntry.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                        //Do nothing
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        //Do nothing
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        final Object callback = XposedHelpers.getObjectField(param.thisObject, "mCallback");
                                        final Object lockPatternUtils = XposedHelpers.getObjectField(param.thisObject, "mLockPatternUtils");
                                        String entry = passwordEntry.getText().toString();

                                        if (callback != null && lockPatternUtils != null && entry.length() > 3 && (Boolean) XposedHelpers.callMethod(lockPatternUtils, "checkPassword", entry)) {
                                            XposedHelpers.callMethod(callback, "reportSuccessfulUnlockAttempt");
                                            XposedHelpers.callMethod(callback, "dismiss", true);
                                        }else if(callback != null && lockPatternUtils != null && entry.length() > 3){
                                            for(Profile profile : profileList){
                                                if(entry.equals(profile.getPIN()) && profile.isEnabled() && profile.getType().equals(Profile.TYPE_PIN)){
                                                    XposedHelpers.callMethod(callback, "reportSuccessfulUnlockAttempt");
                                                    XposedHelpers.callMethod(callback, "dismiss", true);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

                XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardAbsKeyInputView", loadPackageParam.classLoader, "verifyPasswordAndUnlock", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if(!pinQuickUnlock){
                            //Check PIN against profiles and unlock
                            final TextView passwordEntry = (TextView) XposedHelpers.getObjectField(param.thisObject, "mPasswordEntry");
                            final Object callback = XposedHelpers.getObjectField(param.thisObject, "mCallback");
                            final Object lockPatternUtils = XposedHelpers.getObjectField(param.thisObject, "mLockPatternUtils");

                            String entry = passwordEntry.getText().toString();

                            if(callback != null && lockPatternUtils != null && entry.length() > 3){
                                for(Profile profile : profileList){
                                    if(entry.equals(profile.getPIN()) && profile.isEnabled() && profile.getType().equals(Profile.TYPE_PIN)){
                                        XposedHelpers.callMethod(callback, "reportSuccessfulUnlockAttempt");
                                        XposedHelpers.callMethod(callback, "dismiss", true);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}