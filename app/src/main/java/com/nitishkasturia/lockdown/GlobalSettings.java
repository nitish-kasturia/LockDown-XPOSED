package com.nitishkasturia.lockdown;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class GlobalSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_global_settings);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(RootTools.isAccessGiven()){
            CommandCapture chmod = new CommandCapture(0, "cd data/data/com.nitishkasturia.lockdown/", "chmod -R 705 files", "chmod -R 705 shared_prefs");
            try{
                RootTools.getShell(true).add(chmod);
            }catch (IOException e){
                e.printStackTrace();
            }catch (TimeoutException e){
                e.printStackTrace();
            }catch (RootDeniedException e){
                e.printStackTrace();
            }
        }

        Toast.makeText(getApplicationContext(), R.string.toast_restart_device_settings, Toast.LENGTH_SHORT).show();
    }
}