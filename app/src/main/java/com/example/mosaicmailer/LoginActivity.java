package com.example.mosaicmailer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    MailProcessing mp;
    DatabaseHelper helper = null;
    ProgressBar progressBar;
    String loginType=null;
    final String WINDOW = "login_window";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing)this.getApplication();

        if(!mp.boot){
            //起動ログの書き出し
            mp.writeLog(WINDOW,"boot MosaicMailer");
            mp.boot=true;
        }

        //開いた画面のログの書き出し
        mp.writeLog(WINDOW,"open " + WINDOW);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(android.widget.ProgressBar.INVISIBLE);
        //ログインタイプ
        loginType = getIntent().getStringExtra("loginType");

        File file = this.getFileStreamPath(mp.logFileName);
        if(!file.exists()){
            //ログ・ファイルの作成
            mp.createLog();
        }

        //ログの書き出し
        mp.writeLog(WINDOW,"onCreate");

        if(loginType==null){
            //アカウント情報をDBから探す．
            helper = new DatabaseHelper(this);
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
    }

    public void loginClick(View view) {
        progressBar.setVisibility(android.widget.ProgressBar.VISIBLE);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {

            EditText inputMailaddress = (EditText) findViewById(R.id.editTextTextPersonName);
            String mailaddress = inputMailaddress.getText().toString();
            EditText inputPassword = (EditText) findViewById(R.id.editTextTextPassword);
            String password = inputPassword.getText().toString();
            //System.out.println(mailaddress+","+password);

            //アカウント情報のDBに登録
            try {
                helper = new DatabaseHelper(this);
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete("accountsInfo", null, null);
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
            countDownLatch.countDown();
            //mp.setTestString("MVMVMVM");
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressBar.setVisibility(android.widget.ProgressBar.INVISIBLE);
        if(loginType==null){
            Intent intent = new Intent(getApplication(), IndexActivity.class);
            startActivity(intent);
        }else if(loginType.equals("Login")){
            finish();
        }
    }

    public void login(String mailaddress, String password) {
        progressBar.setVisibility(android.widget.ProgressBar.VISIBLE);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            //System.out.println(mailaddress+","+password);
            mp.connect(mailaddress,password);
            mp.getMailListAll();
            //mp.setTestString("MVMVMVM");
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressBar.setVisibility(android.widget.ProgressBar.INVISIBLE);
        Intent intent = new Intent(getApplication(), IndexActivity.class);
        startActivity(intent);

    }
}