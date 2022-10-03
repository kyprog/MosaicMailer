package com.example.mosaicmailer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.Executors;

public class MailListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MailProcessing mp;
    String testStr=null;

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
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // RecyclerViewのレイアウトサイズを変更しない設定をONにする
        // パフォーマンス向上のための設定。
        recyclerView.setHasFixedSize(true);

        // RecyclerViewにlayoutManagerをセットする。
        // このlayoutManagerの種類によって「1列のリスト」なのか「２列のリスト」とかが選べる。
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Adapter生成してRecyclerViewにセット
        RecyclerView.Adapter mainAdapter = new MailListAdapter(getApplication(), recyclerView);
        recyclerView.setAdapter(mainAdapter);

        //(今，取得しているメッセージの中で)一番下の未読メールを取得する．
        Executors.newSingleThreadExecutor().execute(() -> {
            mp.searchOldestMailPosition();
        });
    }

    // メニューをActivity上に設置する
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 参照するリソースは上でリソースファイルに付けた名前と同じもの
        getMenuInflater().inflate(R.menu.activity_mail_list_menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // メニューが選択されたときの処理
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem1:
                ...
                return true;

            case R.id.menuItem2:
                ...
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

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
