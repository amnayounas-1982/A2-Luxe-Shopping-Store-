package com.example.snapstore.application;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.snapstore.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
public class Myapp extends Application {

    public static String currentLang = "en";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();

        // Local defaults
        config.setDefaultsAsync(R.xml.remote_config_defaults);

        // Dev: always fetch fresh (set to >=3600 for production)
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        config.setConfigSettingsAsync(settings);

        // Fetch & apply
        config.fetchAndActivate().addOnCompleteListener(task -> {
            String theme = config.getString("app_theme");
            Log.d("RemoteConfig", "app_theme = " + theme);

            if ("dark".equalsIgnoreCase(theme)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });


        config.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String language = config.getString("app_language");

                Log.d("RemoteConfig", "Fetched language = " + language);

                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                String savedTheme = prefs.getString("app_theme", "light");


                prefs.edit()
                        .putString("lang_code", language)
                        .apply();
            }
        });
    }
}

