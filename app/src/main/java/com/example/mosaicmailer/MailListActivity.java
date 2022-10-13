package com.example.mosaicmailer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class MailListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MailProcessing mp;
    String testStr=null;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);//xmlを読み込む
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Executors.newSingleThreadExecutor().execute(() -> {
            mp.searchOldestMailPosition();//(今，取得しているメッセージの中で)一番下の未読メールを取得する．
            mp.searchAlert();//注意喚起メールが来ていないか調べる．
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                // Adapter生成してRecyclerViewにセット
                RecyclerView.Adapter mainAdapter = new MailListAdapter(getApplication(), recyclerView);
                recyclerView.setAdapter(mainAdapter);
            });
        });
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            //mp.reloadMessageList("MailList");
            RecyclerView.Adapter mainAdapter = new MailListAdapter(getApplication(), recyclerView);
            countDownLatch.countDown();
            //処理結果をhandler経由でUIに反映
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                recyclerView.setAdapter(mainAdapter);
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
}
