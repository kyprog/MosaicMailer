package com.example.mosaicmailer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public class MailBrowseActivity extends AppCompatActivity{
    MailProcessing mp;
    static MailBrowseActivity instance = new MailBrowseActivity();
    Message msg;
    String ListType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_browse);//xmlを読み込む
        mp = (MailProcessing) this.getApplication();

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ナビゲーション表示
        mp.showCheckAlert(getWindow().getDecorView());

        //データを受け取る
        ListType = getIntent().getStringExtra("ListType");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if(ListType.equals("MailList")){
                    msg = mp.MessageList.get(mp.openMessageListPosition);
                }else{
                    msg = mp.SearchResultList.get(mp.openSearchResultListPosition);
                }

                //メールの件名を取得
                String subject =  msg.getSubject();

                //メールの差出人名を取得
                final InternetAddress addrFrom = (InternetAddress) msg.getFrom()[0];
                String sender = addrFrom.getPersonal();
                mp.setSenderName(sender);

                //差出人のメールアドレス取得
                mp.setSenderMailAddress(addrFrom.getAddress());

                //メールの本文中のテキストをモザイク化しセッティング
                SpannableString ss = Mosaic();
                final Pattern STANDARD_URL_MATCH_PATTERN = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
                Matcher m = STANDARD_URL_MATCH_PATTERN.matcher(ss.toString());
                while(m.find()) {
                    ss.setSpan(new URLSpan(m.group()){
                        String mgroup=m.group();
                        @Override
                        public void onClick(View widget) {
                            //System.out.println(mgroup);
                            mp.setMailURL(mgroup);
                            mp.setRealURL(mgroup);
                            DialogFragment compare_dialog = new URLCompareQuestionDialog();
                            compare_dialog.show(getSupportFragmentManager(), "url_compare_question_dialog");
                        }
                    }, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                //処理結果をhandler経由でUIに反映
                HandlerCompat.createAsync(getMainLooper()).post(() ->{
                    ((TextView) findViewById(R.id.title)).setText(subject);
                    ((TextView) findViewById(R.id.sender)).setText(sender);
                    ((TextView) findViewById(R.id.receiver)).setText("To: 自分");
                    ((TextView) findViewById(R.id.body)).setMovementMethod(new ScrollingMovementMethod());
                    ((TextView) findViewById(R.id.body)).setText(ss);
                    ((TextView) findViewById(R.id.body)).setMovementMethod(LinkMovementMethod.getInstance());
                });
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });

    }

    // メニューをActivity上に設置する
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 参照するリソースは上でリソースファイルに付けた名前と同じもの
        getMenuInflater().inflate(R.menu.activity_mail_browse_menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // ツールバーのメニューが選択されたときの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Executors.newSingleThreadExecutor().execute(() -> {
                    mp.deleteMessage(msg);
                    mp.reloadMessageList(ListType);
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mp.phishingFlag = false;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        if(!mp.phishingFlag){finish();}
        return super.onSupportNavigateUp();
    }

    //メールアドレス確認ポップアップの表示
    public void QuestionDialog(View view) {
        DialogFragment name_dialog = new FromNameQuestionDialog();
        name_dialog.show(getSupportFragmentManager(), "name_question_dialog");
    }

    // インスタンス取得メソッド
    public static MailBrowseActivity getInstance() {
        return instance;
    }

    public void test() {
        System.out.println("test test test test test");
    }

    public String multSquare(int n){
        StringBuilder result = new StringBuilder();
        for(int i=0; i<n; i++){
            result.append("□");
        }
        return result.toString();
    }

    public SpannableString Mosaic(){
        try {
            String content = msg.getContent().toString();
            final Pattern STANDARD_URL_MATCH_PATTERN = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
            Matcher m = STANDARD_URL_MATCH_PATTERN.matcher(content);

            char[] charContent = content.toCharArray();

            int end = 0;
            while(m.find()) {
                for(int i=end; i<m.start(); i++){
                    if(Character.toString(charContent[i]).matches("[^\n]")){
                        charContent[i]='□';
                    }
                }
                end = m.end()+1;
            }

            String MosaicContent =new String(charContent);

            SpannableString ss = new SpannableString(MosaicContent);

            return ss;

        }  catch (MessagingException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeMosaic() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String content = msg.getContent().toString();
                final Pattern STANDARD_URL_MATCH_PATTERN = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
                Matcher m = STANDARD_URL_MATCH_PATTERN.matcher(content);

                SpannableString ss = new SpannableString(content);
                while (m.find()) {
                    ss.setSpan(new URLSpan(m.group()) {
                        @Override
                        public void onClick(View widget) {
                            URLCompareQuestionDialog compare_dialog = new URLCompareQuestionDialog();
                            compare_dialog.show(getSupportFragmentManager(), "url_compare_question_dialog");
                        }
                    }, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //処理結果をhandler経由でUIに反映
                HandlerCompat.createAsync(getMainLooper()).post(() -> {
                    ((TextView) findViewById(R.id.body)).setText(ss);
                });

            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        });
    }

}