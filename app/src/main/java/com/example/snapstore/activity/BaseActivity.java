package com.example.snapstore.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.example.snapstore.application.LocalHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("lang_code", "en"); // default English

        Context context = LocalHelper.updateLocale(newBase, langCode);
        super.attachBaseContext(context);
    }

    /**
     * Call this method to change app language dynamically
     * @param langCode language code like "en", "ur", etc.
     */
    public void changeLanguage(String langCode) {
        // Save lang code
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("lang_code", langCode).apply();

        // Update locale for current context
        LocalHelper.updateLocale(this, langCode);

        // Restart activity to apply changes
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
