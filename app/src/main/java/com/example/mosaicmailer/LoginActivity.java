package com.example.mosaicmailer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    MailProcessing mp;
    MosaicMailerDatabaseHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing)this.getApplication();

        //アカウント情報をDBから探す．
        helper = new MosaicMailerDatabaseHelper(this);
        String[] cols = {"_id", "mailaddress", "password"};
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cs = db.query("accountsInfo", cols, null, null, null, null, null, null);
            if (cs.getCount()>0) {
                cs.moveToFirst();
                login(cs.getString(1), cs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loginClick(View view) {
        Executors.newSingleThreadExecutor().execute(() -> {

            EditText inputMailaddress = (EditText) findViewById(R.id.editTextTextPersonName);
            String mailaddress = inputMailaddress.getText().toString();
            EditText inputPassword = (EditText) findViewById(R.id.editTextTextPassword);
            String password = inputPassword.getText().toString();
            //System.out.println(mailaddress+","+password);

            //アカウント情報のDBに登録
            try {
                SQLiteDatabase db = helper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("mailaddress", mailaddress);
                cv.put("password", password);
                db.insert("accountsInfo", null, cv);
                //db.insertWithOnConflict("books", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mp.connect(mailaddress,password);
            mp.getMailListAll();
            //mp.setTestString("MVMVMVM");
            Intent intent = new Intent(getApplication(), MailListActivity.class);
            startActivity(intent);
        });
    }

    public void login(String mailaddress, String password) {

        Executors.newSingleThreadExecutor().execute(() -> {
            //System.out.println(mailaddress+","+password);
            mp.connect(mailaddress,password);
            mp.getMailListAll();
            //mp.setTestString("MVMVMVM");
            Intent intent = new Intent(getApplication(), MailListActivity.class);
            startActivity(intent);
        });
    }
}