package com.nitishkasturia.lockdown.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.nitishkasturia.lockdown.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Created by Nitish Kasturia on 2014-08-09.
 * Adapter for MainActivity profile list
 */
public class ProfileList extends ArrayAdapter<String>{

    Context context;
    List<String> profileList;

    public ProfileList(Context context, int resource, List<String> objects){
        super(context, resource, objects);
        this.context = context;
        profileList = objects;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_profiles, null);
        }

        TextView profile = (TextView) view.findViewById(R.id.textview_profile_name);
        Switch profileSwitch = (Switch) view.findViewById(R.id.switch_profile_enabled);

        try {
            RandomAccessFile profileSettingsRAF = new RandomAccessFile(new File(context.getFilesDir(), (profileList.get(position) + ".settings")), "rw");

            profile.setText(profileList.get(position));
            profileSwitch.setChecked(profileSettingsRAF.readBoolean());

            profileSettingsRAF.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        profileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                View v = (View) buttonView.getParent();
                TextView profile = (TextView) v.findViewById(R.id.textview_profile_name);

                try {
                    RandomAccessFile profileSettings = new RandomAccessFile(new File(context.getFilesDir(), (profile.getText().toString() + ".settings")), "rw");

                    profileSettings.seek(0);
                    profileSettings.writeBoolean(isChecked);

                    profileSettings.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}
