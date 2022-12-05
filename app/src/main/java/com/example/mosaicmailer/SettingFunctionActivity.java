package com.example.mosaicmailer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingFunctionActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_function_pref);

    }
}
