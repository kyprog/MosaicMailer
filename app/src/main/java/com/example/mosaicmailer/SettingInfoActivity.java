package com.example.mosaicmailer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingInfoActivity extends AppCompatActivity {
    String settingType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_info);//xmlを読み込む

        settingType = getIntent().getStringExtra("SettingType");

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(settingType.equals("headUp")){ getSupportActionBar().setTitle("注意喚起メールの設定情報"); }
        else if(settingType.equals("white")){ getSupportActionBar().setTitle("ホワイトリストの設定情報"); }
        else{ getSupportActionBar().setTitle("ブラックリストの設定情報"); }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void addOnClick(View v) {
        Intent intent = new Intent(getApplication(), SettingAddActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("SettingType", settingType);
        startActivity(intent);
    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}
