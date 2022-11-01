package com.example.mosaicmailer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.mail.Message;

public class IndexActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MailProcessing mp;
    String testStr=null;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    IndexAdapter mainAdapter;
    boolean updateFlag = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_navigation_menu);//xmlを読み込む
        mp = (MailProcessing) this.getApplication();
        //testStr = mp.getTestString();

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("受信トレイ");

        // DrawerToggle
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        //if(drawer==null){System.out.println("drawer==null");}
        //if(toggle==null){System.out.println("toggle==null");}
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView Listener
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //recyclerView
        recyclerView = findViewById(R.id.list_recycler_view);

        // RecyclerViewのレイアウトサイズを変更しない設定をONにする
        // パフォーマンス向上のための設定。
        recyclerView.setHasFixedSize(true);

        // RecyclerViewにlayoutManagerをセットする。
        // このlayoutManagerの種類によって「1列のリスト」なのか「２列のリスト」とかが選べる。
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            mp.searchOldestMailPosition();//(今，取得しているメッセージの中で)一番下の未読メールを取得する．
            mp.searchAlert();//注意喚起メールが来ていないか調べる．
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                // Adapter生成してRecyclerViewにセット
                mainAdapter = new IndexAdapter(getApplication(), recyclerView);
                recyclerView.setAdapter(mainAdapter);
            });
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //応急処置
        if(mp.oldestMailPosition<5){mp.changeSearchHeadUpFlag(true);}

        updateFlag = true;

        Executors.newSingleThreadExecutor().execute(() -> {
            while(true) {
                if(updateFlag){
                    System.out.println("true:update:");
                    mp.reloadMessageList("MailList");
                    HandlerCompat.createAsync(getMainLooper()).post(() -> {
                        mainAdapter.reload(mp.MessageList);

                    });
                }else{
                    System.out.println("false:not update:");
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFlag = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateFlag = false;
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            mp.reloadMessageList("MailList");
            //mainAdapter = new IndexAdapter(getApplication(), recyclerView);
            countDownLatch.countDown();
            //処理結果をhandler経由でUIに反映
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                //recyclerView.setAdapter(mainAdapter);
                mainAdapter.reload(mp.MessageList);
            });
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(mp.searchAlertMode){
            mp.SearchPhishingAlertInList(this.getWindow().getDecorView());
        }
    }

    // メニューをActivity上に設置する
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 参照するリソースは上でリソースファイルに付けた名前と同じもの
        getMenuInflater().inflate(R.menu.activity_mail_list_menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // ツールバーのメニューが選択されたときの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(getApplication(),SearchActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                // ...処理を書きます
                Intent intent = new Intent(getApplication(),SettingActivity.class);
                startActivity(intent);
                break;
            default:
                // ...処理を書きます
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                break;
        }
        return false;
    }

    public void createMail(View view) {
        Intent intent = new Intent(getApplication(), CreateActivity.class);
        intent.putExtra("createType", "normal");
        startActivity(intent);
    }
}
