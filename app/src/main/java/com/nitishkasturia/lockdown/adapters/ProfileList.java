package com.nitishkasturia.lockdown.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.nitishkasturia.lockdown.Profile;
import com.nitishkasturia.lockdown.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

/**
 * Created by Nitish Kasturia on 2014-08-09.
 * Adapter for MainActivity profile list
 */
public class ProfileList extends ArrayAdapter<Profile>{

    Context context;
    List<Profile> profileList;

    public ProfileList(Context context, int resource, List<Profile> objects){
        super(context, resource, objects);
        this.context = context;
        profileList = objects;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_profile, null);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview_profile_type);
        TextView profile = (TextView) view.findViewById(R.id.textview_profile_name);
        Switch profileSwitch = (Switch) view.findViewById(R.id.switch_profile_enabled);

        if(profileList.get(position).getType().equals(Profile.TYPE_PIN)){
            imageView.setImageResource(R.drawable.ic_action_dial_pad);
        }else if(profileList.get(position).getType().equals(Profile.TYPE_PASSWORD)){
            //Set image to password drawable
        }else if(profileList.get(position).getType().equals(Profile.TYPE_PATTERN)){
            //Set image to pattern drawable
        }

        profile.setText(profileList.get(position).getName());
        profileSwitch.setChecked(profileList.get(position).isEnabled());

        profileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                View v = (View) buttonView.getParent();
                TextView profileName = (TextView) v.findViewById(R.id.textview_profile_name);

                try{
                    FileInputStream fileIn = context.openFileInput(profileName.getText().toString());
                    ObjectInputStream objectIn = new ObjectInputStream(fileIn);

                    Profile profile = (Profile) objectIn.readObject();
                    profile.setEnabled(isChecked);

                    fileIn.close();
                    objectIn.close();

                    FileOutputStream fileOut = context.openFileOutput(profile.getName(), 0);
                    ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

                    objectOut.writeObject(profile);

                    fileOut.close();
                    objectOut.close();
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
        });
        return view;
    }
}
