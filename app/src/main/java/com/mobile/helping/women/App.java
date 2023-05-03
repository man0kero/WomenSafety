package com.mobile.helping.women;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

public class App extends Application {

    private static final String APP_SETTINGS = "App_settings";
    private static final String IS_STARTED_UP = "Is started up";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        boolean flag = preferences.contains(IS_STARTED_UP);

        if(!flag) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(IS_STARTED_UP, true);
            editor.apply();
            Intent intent = new Intent(this, InitialActivity.class);
            intent.setFlags(FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
