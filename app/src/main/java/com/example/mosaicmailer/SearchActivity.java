package com.example.mosaicmailer;

import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.MessagingException;

public class SearchActivity extends AppCompatActivity {
    MailProcessing mp;
    RecyclerView searchRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing)this.getApplication();

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //searchRecyclerView
        searchRecyclerView = findViewById(R.id.search_recycler_view);
        // RecyclerViewのレイアウトサイズを変更しない設定をONにする
        // パフォーマンス向上のための設定。
        searchRecyclerView.setHasFixedSize(true);
        // RecyclerViewにlayoutManagerをセットする。
        // このlayoutManagerの種類によって「1列のリスト」なのか「２列のリスト」とかが選べる。
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter mainAdapter = new SearchAdapter(getApplication(), searchRecyclerView);
        searchRecyclerView.setAdapter(mainAdapter);


        //検索ボックスに入力されたときの処理を定義
        SearchView sv = findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String s) {
                //注意喚起メールに含まれるキーワードかの判定
                boolean searchedAlertKeyword = mp.AlertMailSource.contains(s);
                Executors.newSingleThreadExecutor().execute(() -> {
                    //javamailで検索し，該当するメッセージ一覧を取得する
                    mp.searchMessages(s);

                    //処理結果をhandler経由でUIに反映
                    HandlerCompat.createAsync(getMainLooper()).post(() ->{
                        // Adapter生成してRecyclerViewにセット
                        RecyclerView.Adapter mainAdapter = null;
                        if(mp.searchAlertMode){
                            if(searchedAlertKeyword){
                                if(mp.noKeywordAlertFlag){
                                    mp.noKeywordAlert.dismiss();
                                    mp.noKeywordAlertFlag = false;
                                }
                                mainAdapter = new SearchAdapter(getApplication(), searchRecyclerView);
                                searchRecyclerView.setAdapter(mainAdapter);
                                if(mp.allSeenInSearchResultList()){//注意喚起メールの情報をもとに検索して，全て未読の場合
                                    mp.allSeenSnackbar(searchRecyclerView);
                                    mp.searchAlertMode = false;
                                }
                            }else{
                                mp.noKeywordAlert(searchRecyclerView);
                            }
                        }else{
                            mainAdapter = new SearchAdapter(getApplication(), searchRecyclerView);
                            searchRecyclerView.setAdapter(mainAdapter);
                        }
                    });
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            //mp.reloadMessageList("Search");
            RecyclerView.Adapter mainAdapter = new SearchAdapter(getApplication(), searchRecyclerView);
            countDownLatch.countDown();
            //処理結果をhandler経由でUIに反映
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                searchRecyclerView.setAdapter(mainAdapter);
                if(mp.allSeenInSearchResultList()){//注意喚起メールの情報をもとに検索して，全て未読の場合
                    mp.allSeenSnackbar(searchRecyclerView);
                    mp.searchAlertMode = false;
                }
            });
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
