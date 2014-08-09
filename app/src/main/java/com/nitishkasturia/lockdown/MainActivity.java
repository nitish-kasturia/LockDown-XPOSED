package com.nitishkasturia.lockdown;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.nitishkasturia.lockdown.adapters.ProfileList;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity {

    public static SharedPreferences securePrefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        securePrefs = new SecurePreferences(this);

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

        if(securePrefs.getBoolean("FIRST_LAUNCH", true)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_welcome);
            builder.setMessage(R.string.welcome_message); //TODO Update welcome message
            builder.setNeutralButton(R.string.button_OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    securePrefs.edit().putBoolean("FIRST_LAUNCH", false).apply();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        ListView profilesList = (ListView) findViewById(R.id.listview_profileList);

        refreshProfileList();
        registerForContextMenu(profilesList);
        profilesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create Intent and store profile info and launch EditProfileActivity
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if(view.getId() == R.id.listview_profileList){
            ListView list = (ListView) view.findViewById(R.id.listview_profileList);
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            View profileLayout = list.getChildAt(info.position);
            //IF view.id != textview_empty_profile
            //Add delete option
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_new_profile){
            newProfile();
        }else if(id == R.id.action_change_PIN){
            //Change PIN
        }else if(id == R.id.action_global_settings){
            startActivity(new Intent(this, GlobalSettings.class));
        }else if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void newProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_new_profile);
        builder.setItems(R.array.unlock_methods, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                if(selection == 0){
                    newPIN();
                }else if(selection == 1){
                    Toast.makeText(getApplicationContext(), R.string.password_WIP, Toast.LENGTH_SHORT).show();
                }else if(selection == 2){
                    Toast.makeText(getApplicationContext(), R.string.pattern_WIP, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    private void newPIN(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_new_PIN);
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_new_pin, null));
        builder.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String profileName = ((EditText) ((AlertDialog) dialog).findViewById(R.id.edittext_profile_name)).getText().toString();
                String pin = ((EditText) ((AlertDialog) dialog).findViewById(R.id.edittext_profile_PIN)).getText().toString();
                String confirmPin = ((EditText) ((AlertDialog) dialog).findViewById(R.id.edittext_profile_PIN_confirm)).getText().toString();

                if(profileName.length() == 0 || pin.length() == 0 || confirmPin.length() == 0){
                    Toast.makeText(getApplicationContext(), R.string.toast_missing_field, Toast.LENGTH_SHORT).show();
                    return;
                }else if(pin.length() < 4){
                    Toast.makeText(getApplicationContext(), R.string.toast_short_PIN, Toast.LENGTH_SHORT).show();
                    return;
                }else if(!pin.equals(confirmPin)){
                    Toast.makeText(getApplicationContext(), R.string.toast_PIN_mismatch, Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    String fileList[] = fileList();
                    if(fileList.length > 0){
                        for(int i = 0; i < fileList.length; i++){
                            if(fileList[i].equalsIgnoreCase(profileName)){
                                Toast.makeText(getApplicationContext(), R.string.toast_profile_exists, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //TODO Check if the same PIN exists elsewhere under a different profile name
                        }
                        createPIN(profileName, pin);
                        refreshProfileList();
                    }else{
                        createPIN(profileName, pin);
                        refreshProfileList();
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //User cancelled. Do nothing
            }
        });
        builder.create().show();
    }

    private void createPIN(String profileName, String profilePIN){
        try {
            RandomAccessFile profile = new RandomAccessFile(new File(getFilesDir(), profileName), "rw");
            RandomAccessFile profileSettings = new RandomAccessFile(new File(getFilesDir(), profileName + ".settings"), "rw");

            profile.writeInt(0);                //Number of apps listed

            profileSettings.writeBoolean(true); //Profile enabled location is 0
            profileSettings.seek(5);            //Hide apps location
            profileSettings.writeBoolean(false);
            profileSettings.seek(10);           //Notify user location
            profileSettings.writeBoolean(false);

            profile.close();
            profileSettings.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        securePrefs.edit().putString(profileName, profilePIN).commit();

        if(RootTools.isAccessGiven()){
            CommandCapture chmod = new CommandCapture(0, "cd data/data/com.nitishkasturia.lockdown/", "chmod -R 705 files", "chmod -R 705 shared_prefs");
            try {
                RootTools.getShell(true).add(chmod);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (RootDeniedException e) {
                e.printStackTrace();
            }
        }
    }

    private void changePIN(){
        //TODO Change launch PIN
    }

    private void refreshProfileList(){
        ListView profilesList = (ListView) findViewById(R.id.listview_profileList);

        if(fileList().length > 0){
            profilesList.setClickable(true);
            profilesList.setLongClickable(true);

            ArrayList<String> fileList = new ArrayList<String>();
            String files[] = fileList();

            for(String file : files){
                if(!file.contains(".settings")){
                    fileList.add(file);
                }
            }
            ProfileList profileListAdapter= new ProfileList(this, R.layout.listview_profiles, fileList);
            profilesList.setAdapter(profileListAdapter);
        }else{
            profilesList.setClickable(false);
            profilesList.setLongClickable(false);
            ArrayList<String> fileList = new ArrayList<String>();
            fileList.add("No Profiles");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listview_empty_profile, fileList);
            profilesList.setAdapter(adapter);
        }
    }
}