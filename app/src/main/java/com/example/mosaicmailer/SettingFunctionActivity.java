package com.example.mosaicmailer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.MessagingException;

public class SettingFunctionActivity extends PreferenceActivity {

    final String WINDOW = "setting_function_window";
    MailProcessing mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_function_pref);
        mp = (MailProcessing)this.getApplication();

    }

    //端末の戻るボタンで戻る
    @Override
    public void onBackPressed() {
        //機能on/off設定
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mp.habitFunction = pref.getBoolean("habitFunction", true);
        mp.messageFunction = pref.getBoolean("messageFunction", true);
        mp.numberInViewable = Integer.parseInt( pref.getString("numberInViewable","10") )-1;
        //System.out.println("numberInViewable is "+mp.numberInViewable);

        //習慣化機能とメッセージ機能がonかoffどうか表すログの書き出し
        mp.writeLog(WINDOW,"habit function is "+ mp.habitFunction);
        mp.writeLog(WINDOW,"message function is "+ mp.messageFunction);
        finish();
    }
}
