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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingActivity extends AppCompatActivity {
    final String WINDOW = "mail_setting_window";
    MailProcessing mp;
    String log="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);//xmlを読み込む
        mp = (MailProcessing)this.getApplication();

        //開いた画面のログの書き出し
        mp.writeLog(WINDOW,"open " + WINDOW);

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
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);//APIレベル19以降
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        final Date date = new Date(System.currentTimeMillis());

        intent.putExtra(Intent.EXTRA_TITLE, "MosaicLog"+df.format(date)+"_"+mp.accountInfo);
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

    public void functionOnClick(View v) {
        Intent intent = new Intent(getApplication(), SettingFunctionActivity.class);
        startActivity(intent);
    }
}
