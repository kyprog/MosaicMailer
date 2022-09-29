package com.example.mosaicmailer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    MailProcessing mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing)this.getApplication();

    }

    public void loginClick(View view) {
        Executors.newSingleThreadExecutor().execute(() -> {
            EditText inputMailaddress = (EditText) findViewById(R.id.editTextTextPersonName);
            String mailaddress = inputMailaddress.getText().toString();

            EditText inputPassword = (EditText) findViewById(R.id.editTextTextPassword);
            String password = inputPassword.getText().toString();

            //System.out.println(mailaddress+","+password);

            mp.connect(mailaddress,password);
            //mp.setTestString("MVMVMVM");
            Intent intent = new Intent(getApplication(), MailListActivity.class);
            startActivity(intent);
        });
    }
}