package com.example.mosaicmailer;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingAddActivity extends AppCompatActivity {
    String settingType = "";
    MosaicMailerDatabaseHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_add);//xmlを読み込む

        settingType = getIntent().getStringExtra("SettingType");

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(settingType.equals("headUp")){
            getSupportActionBar().setTitle("注意喚起メールの条件追加");
            ((TextView) findViewById(R.id.textView8)).setText("注意喚起メールの送信元");
            ((TextView) findViewById(R.id.textView9)).setText("注意喚起メールに使われるキーワード");
        }
        else if(settingType.equals("white")){
            getSupportActionBar().setTitle("ホワイトリストの追加");
            ((TextView) findViewById(R.id.textView8)).setText("正規表現or文字列");
            ((TextView) findViewById(R.id.textView9)).setText("安全なURLのドメイン名");
        }
        else{
            getSupportActionBar().setTitle("ブラックリストの追加");
            ((TextView) findViewById(R.id.textView8)).setText("正規表現or文字列");
            ((TextView) findViewById(R.id.textView9)).setText("怪しいURLのドメイン名");
        }

        helper = new MosaicMailerDatabaseHelper(this);

    }

    public void addTermsOnClick(View view){
        String aboveText = ((EditText) findViewById(R.id.inputText8)).getText().toString();
        String belowText = ((EditText) findViewById(R.id.inputText9)).getText().toString();

        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            if(settingType.equals("headUp")){
                cv.put("mailaddress", aboveText);
                cv.put("keyword", belowText);
                db.insert("HeadsUpInfo", null, cv);
                //db.insertWithOnConflict("books", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }else if(settingType.equals("white")){
                cv.put("type", aboveText);
                cv.put("URL", belowText);
                db.insert("WhiteList", null, cv);
                //db.insertWithOnConflict("books", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }else if(settingType.equals("black")){
                cv.put("type", aboveText);
                cv.put("URL", belowText);
                db.insert("BlackList", null, cv);
                //db.insertWithOnConflict("books", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }

            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
