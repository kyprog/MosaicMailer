package com.example.mosaicmailer;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import javax.mail.Flags;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

public class SettingInfoActivity extends AppCompatActivity {
    String settingType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_info_activity);//xmlを読み込む

        settingType = getIntent().getStringExtra("SettingType");

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(settingType.equals("headUp")){ getSupportActionBar().setTitle("注意喚起メールの設定情報"); }
        else if(settingType.equals("white")){ getSupportActionBar().setTitle("ホワイトリストの設定情報"); }
        else{ getSupportActionBar().setTitle("ブラックリストの設定情報"); }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //設定表示用のTextView
        TextView settingInfo = (TextView) findViewById(R.id.settingInfoTextView);
        DatabaseHelper helper = null;
        helper = new DatabaseHelper(this);
        String[] cols = {"_id", "mailaddress", "keyword"};
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cs = db.query("HeadsUpInfo", cols, null, null, null, null, null, null);

        if (cs.getCount()>0) {
            cs.moveToFirst();
            settingInfo.setText(
                    "注意喚起メールの送信元\n"+cs.getString(1)+"\n\n" +
                            "注意喚起メールに使われるキーワード\n"+cs.getString(2)
            );
        }else{
            settingInfo.setText("注意喚起メールの送信元\nnothing\n\n注意喚起メールに使われるキーワード\nnothing");
        }



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
