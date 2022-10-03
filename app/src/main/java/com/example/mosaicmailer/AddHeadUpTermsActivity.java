package com.example.mosaicmailer;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddHeadUpTermsActivity extends AppCompatActivity {
    MosaicMailerDatabaseHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_headsup_terms);//xmlを読み込む

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("注意喚起メールの設定");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        helper = new MosaicMailerDatabaseHelper(this);

    }

    public void addTermsOnClick(View view){
        String headsUpMailaddress = ((EditText) findViewById(R.id.headsUpMailAddress)).getText().toString();
        String headsUpKeyword = ((EditText) findViewById(R.id.headsUpKeyword)).getText().toString();

        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("mailaddress", headsUpMailaddress);
            cv.put("keyword", headsUpKeyword);
            db.insert("HeadsUpInfo", null, cv);
            //db.insertWithOnConflict("books", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
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
