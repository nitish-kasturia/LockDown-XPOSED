package com.nitishkasturia.lockdown;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            //Create New Profile
        }else if(id == R.id.action_change_PIN){
            //Change PIN
        }else if(id == R.id.action_global_settings){
            //Global Settings
        }else if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}