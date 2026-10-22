package com.edward.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.edward.smartpantrymanager.ui.InsetsHelper;
import com.google.android.material.appbar.MaterialToolbar;

/** Shows app settings and static information about pantry behavior. */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "pantry_settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        InsetsHelper.applySystemBarInsets(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        MaterialSwitch expirySwitch = findViewById(R.id.expiryAlertSwitch);
        expirySwitch.setChecked(isExpiryAlertsEnabled(this));

        expirySwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_EXPIRY_ALERTS, isChecked)
                        .apply());
    }

    /** Read helper so other screens (e.g. the pantry list) can check the setting. */
    public static boolean isExpiryAlertsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_EXPIRY_ALERTS, true);
    }
}
