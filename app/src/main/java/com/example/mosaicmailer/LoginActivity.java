package com.example.mosaicmailer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    MailProcessing mp;
    DatabaseHelper helper = null;
    String loginType=null;
    final String WINDOW = "login_window";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing)this.getApplication();

        //機能on/off設定
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mp.habitFunction = pref.getBoolean("habitFunction", true);
        mp.messageFunction = pref.getBoolean("messageFunction", true);
        mp.numberInViewable = Integer.parseInt( pref.getString("numberInViewable","10") )-1;

        if( pref.getBoolean("firstBoot", true) ){//初回起動の際，ログ・ファイルを生成する
            mp.createLog();//ログ・ファイルの作成
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("firstBoot", false).apply();//値変更
        }

        if(!mp.boot){
            //起動ログの書き出し
            mp.writeLog(WINDOW,"boot MosaicMailer");
            mp.boot=true;
        }

        //開いた画面のログの書き出し
        mp.writeLog(WINDOW,"open " + WINDOW);

        //ログインタイプ
        loginType = getIntent().getStringExtra("loginType");

    }

    @Override
    public void onResume() {
        super.onResume();

        if(loginType==null){
            //アカウント情報をDBから探す．
            helper = new DatabaseHelper(this);
            String[] cols = {"_id", "mailaddress", "password"};
            try {
                SQLiteDatabase db = helper.getReadableDatabase();
                Cursor cs = db.query("accountsInfo", cols, null, null, null, null, null, null);
                if (cs.getCount()>0) {
                    cs.moveToFirst();
                    ((TextView)findViewById(R.id.editTextTextPersonName)).setText(cs.getString(1));
                    ((TextView)findViewById(R.id.editTextTextPassword)).setText(cs.getString(2));
                    //login(cs.getString(1), cs.getString(2));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loginClick(View view) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {

            EditText inputMailaddress = (EditText) findViewById(R.id.editTextTextPersonName);
            String mailaddress = inputMailaddress.getText().toString();
            EditText inputPassword = (EditText) findViewById(R.id.editTextTextPassword);
            String password = inputPassword.getText().toString();
            //System.out.println(mailaddress+","+password);

            /*
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
            }*/

            mp.connect(mailaddress,password);

            /*
            loginSuccess = false;
            loginSuccess = mp.connect(mailaddress,password);
            if(loginSuccess == false){
                //progressBar.setVisibility(android.widget.ProgressBar.INVISIBLE);
                //loginSnackbar = Snackbar.make(findViewById(R.id.loginError), "ログインに失敗しました", Snackbar.LENGTH_SHORT);
                //loginSnackbar.show();
                return;
            }*/
            mp.getMailListAll();
            mp.getMosaicTrashAll();
            countDownLatch.countDown();
            //mp.setTestString("MVMVMVM");
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(loginType==null){
            Intent intent = new Intent(getApplication(), IndexActivity.class);
            startActivity(intent);
        }else if(loginType.equals("Login")){
            finish();
        }
    }

    public void login(String mailaddress, String password) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            //System.out.println(mailaddress+","+password);
            //loginSuccess = false;
            mp.connect(mailaddress,password);
            /*
            if(loginSuccess == false){
                //progressBar.setVisibility(android.widget.ProgressBar.INVISIBLE);
                //loginSnackbar = Snackbar.make(findViewById(R.id.loginError), "ログインに失敗しました", Snackbar.LENGTH_SHORT);
                //loginSnackbar.show();
                return;
            }*/
            mp.getMailListAll();
            mp.getMosaicTrashAll();
            //mp.setTestString("MVMVMVM");
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplication(), IndexActivity.class);
        startActivity(intent);

    }
}