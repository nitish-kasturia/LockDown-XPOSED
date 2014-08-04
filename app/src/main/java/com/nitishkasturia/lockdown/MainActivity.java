package com.nitishkasturia.lockdown;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
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

        refreshProfileList(profilesList);

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
                    Toast.makeText(getApplicationContext(), "Pattern unlock is WIP", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    private void newPIN(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New PIN Profile");
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_new_pin, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Make profile with given data
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //User cancelled. Do nothing
            }
        });
        builder.create().show();
    }

    private void changePIN(){
        //TODO Change launch PIN
    }

    private void refreshProfileList(ListView profilesList){
        if(fileList().length > 0){
            //SHOW FILES
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