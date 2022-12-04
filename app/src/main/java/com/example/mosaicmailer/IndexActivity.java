package com.example.mosaicmailer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class IndexActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MailProcessing mp;
    String testStr=null;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    IndexAdapter mainAdapter;
    boolean updateFlag = true;
    final String WINDOW = "mail_index_window";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_navigation_menu);//xmlを読み込む
        mp = (MailProcessing) this.getApplication();

        //開いた画面のログの書き出し
        mp.writeLog(WINDOW,"open " + WINDOW);

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
        View headerView = navigationView.getHeaderView(0);
        TextView mailAddress = (TextView) headerView.findViewById(R.id.mailAddress);
        mailAddress.setText(mp.accountInfo);
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

        //注意喚起メールを探すフェーズが始まったことを表すログの書き出し
        mp.phaseSearchAlertMail = true;
        mp.writeLog(WINDOW,"start searchAlert");

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            mp.searchOldestMailPosition();//(今，取得しているメッセージの中で)一番下の未読メールを取得する．
            mp.writeLog(WINDOW,"bottom unread mail position["+mp.oldestMailPosition+"]");//一番下の未読メールの位置を表すログの書き出し
            mp.searchAlert();//注意喚起メールが来ていないか調べる．
            if(mp.existAlert){
                //注意喚起メールが来ているかどうか表すログの書き出し
                mp.writeLog(WINDOW,"alertMail come");
            }else{
                //注意喚起メールが来ているかどうか表すログの書き出し
                mp.writeLog(WINDOW,"alertMail don't come");
            }
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                // Adapter生成してRecyclerViewにセット
                mainAdapter = new IndexAdapter(getApplication(), recyclerView, layoutManager);
                recyclerView.setAdapter(mainAdapter);
            });
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        updateFlag = true;
        mp.showSearchHeadUpAlert(findViewById(R.id.list_recycler_view));
        Executors.newSingleThreadExecutor().execute(() -> {
            while(true) {
                //応急処置
                if(layoutManager.findLastVisibleItemPosition()>=mp.oldestMailPosition){
                    mp.SearchHeadUpAlert.dismiss();
                    mp.changeSearchHeadUpFlag(true);
                    //System.out.println("-----");
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            while(true) {
                if(updateFlag){
                    //System.out.println("true:update:");
                    mp.reloadMessageList("MailList");
                    HandlerCompat.createAsync(getMainLooper()).post(() -> {
                        mainAdapter.reload(mp.MessageList);

                    });
                }else{
                    //System.out.println("false:not update:");
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
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView mailAddress = (TextView) headerView.findViewById(R.id.mailAddress);
        mailAddress.setText(mp.accountInfo);
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
        if(mp.searchPhishingMode){
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.setting:
                // ...処理を書きます
                intent = new Intent(getApplication(),SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.login:
                // ...処理を書きます
                intent = new Intent(getApplication(),LoginActivity.class);
                intent.putExtra("loginType", "Login");
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
