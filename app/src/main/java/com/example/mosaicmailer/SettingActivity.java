package com.example.mosaicmailer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.OutputStream;

public class SettingActivity extends AppCompatActivity {
    MailProcessing mp;
    String log="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);//xmlを読み込む
        mp = (MailProcessing)this.getApplication();

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("設定");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData  = result.getData();
                    if (resultData  != null) {
                        Uri uri = resultData.getData();

                        // Uriを表示
                        //textView.setText(String.format(Locale.US, "Uri:　%s",uri.toString()));
                        try(OutputStream outputStream =
                                    getContentResolver().openOutputStream(uri)) {
                            if(outputStream != null){
                                outputStream.write(log.getBytes());
                            }

                        } catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            });

    public void exportLogOnClick(View v) {
        log = mp.readLog();
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, mp.logFileName);
        resultLauncher.launch(intent);

    }

    public void headUpOnClick(View v) {
        Intent intent = new Intent(getApplication(), SettingInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("SettingType", "headUp");
        startActivity(intent);
    }

    public void whiteOnClick(View v) {
        Intent intent = new Intent(getApplication(), SettingInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("SettingType", "white");
        startActivity(intent);
    }

    public void blackOnClick(View v) {
        Intent intent = new Intent(getApplication(), SettingInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("SettingType", "black");
        startActivity(intent);
    }
}
