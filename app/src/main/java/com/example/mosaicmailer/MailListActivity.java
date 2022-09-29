package com.example.mosaicmailer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

public class MailListActivity extends AppCompatActivity {
    MailProcessing mp;
    String testStr=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_list);//xmlを読み込む
        mp = (MailProcessing) this.getApplication();
        //testStr = mp.getTestString();

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("受信トレイ");

        //recyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // RecyclerViewのレイアウトサイズを変更しない設定をONにする
        // パフォーマンス向上のための設定。
        recyclerView.setHasFixedSize(true);

        // RecyclerViewにlayoutManagerをセットする。
        // このlayoutManagerの種類によって「1列のリスト」なのか「２列のリスト」とかが選べる。
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Adapter生成してRecyclerViewにセット
        RecyclerView.Adapter mainAdapter = new MailListAdapter(getApplication(), createMailData(), recyclerView);
        recyclerView.setAdapter(mainAdapter);

        //(今，取得しているメッセージの中で)一番下の未読メールを取得する．
        Executors.newSingleThreadExecutor().execute(() -> {
            mp.searchOldestMailPosition();
        });

    }

    @NonNull
    private List<mailData> createMailData() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<mailData> dataSet = new ArrayList<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int listLen=50;
            mp.getMailListN(listLen);
            for (int i=0;i<listLen;i++) {
                mailData data = new mailData();
                try {
                    final InternetAddress addrFrom = (InternetAddress) mp.MessageList.get(i).getFrom()[0];
                    data.sender = addrFrom.getPersonal();
                    data.title =  mp.MessageList.get(i).getSubject();
                    Flags flags = mp.MessageList.get(i).getFlags();
                    if (!flags.contains(Flags.Flag.SEEN)){data.unread = true;}
                    else{data.unread = false;}
                } catch (MessagingException e) {
                    e.printStackTrace();
                }

                dataSet.add(data);
            }
            countDownLatch.countDown();
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return dataSet;
    }
}
