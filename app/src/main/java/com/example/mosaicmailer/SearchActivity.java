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

public class SearchActivity extends AppCompatActivity {
    MailProcessing mp;
    RecyclerView searchRecyclerView;
    final String WINDOW = "mail_search_window";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing)this.getApplication();

        //ログの書き出し
        //mp.writeLog("search","onCreate");

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

                //検索した単語を表すログを書き出す
                mp.writeLog(WINDOW,"search word is \""+s+"\"");

                //注意喚起メールに含まれるキーワードかの判定
                boolean searchedAlertKeyword = mp.AlertMailSource.contains(s);

                Executors.newSingleThreadExecutor().execute(() -> {
                    //javamailで検索し，該当するメッセージ一覧を取得する
                    mp.searchMessages(s);

                    //検索結果の未読メール数
                    int countUnread = mp.countUnreadInSearchResultList();
                    //検索結果の未読メールの数を表示するログを書き出す
                    mp.writeLog(WINDOW,"number of unread mail in search result is "+countUnread);

                    //処理結果をhandler経由でUIに反映
                    HandlerCompat.createAsync(getMainLooper()).post(() ->{
                        // Adapter生成してRecyclerViewにセット
                        RecyclerView.Adapter mainAdapter = null;
                        if(mp.searchPhishingMode){
                            if(searchedAlertKeyword){
                                //検索した単語が注意喚起メールに含まれる単語かを表すログを書き出す
                                mp.writeLog(WINDOW,"searched \""+s+"\" is included in AlertMail");

                                if(mp.noKeywordAlertFlag){
                                    mp.noKeywordAlert.dismiss();
                                    mp.noKeywordAlertFlag = false;
                                }
                                mainAdapter = new SearchAdapter(getApplication(), searchRecyclerView);
                                searchRecyclerView.setAdapter(mainAdapter);
                                if(mp.allSeenInSearchResultList()){//注意喚起メールの情報をもとに検索して，全て既読の場合
                                    //すべての未読メールを確認したことを表すログの書き出し
                                    mp.writeLog(WINDOW,"checked all unread mail in search result");
                                    mp.allSeenSnackbar(searchRecyclerView);
                                    mp.searchPhishingMode = false;
                                }
                            }else{
                                //検索した単語が注意喚起メールに含まれる単語かを表すログを書き出す
                                mp.writeLog(WINDOW,"searched \""+s+"\" is not included in AlertMail");

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
            mp.reloadMessageList("Search");
            RecyclerView.Adapter mainAdapter = new SearchAdapter(getApplication(), searchRecyclerView);
            countDownLatch.countDown();

            //検索結果の未読メール数
            int countUnread = mp.countUnreadInSearchResultList();
            //検索結果の未読メールの数を表示するログを書き出す
            mp.writeLog(WINDOW,"number of unread mail in search result is "+countUnread);

            boolean all_seen_flag = mp.allSeenInSearchResultList();

            //処理結果をhandler経由でUIに反映
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                searchRecyclerView.setAdapter(mainAdapter);
                if(all_seen_flag){//注意喚起メールの情報をもとに検索して，全て既読の場合
                    //すべての未読メールを確認したことを表すログの書き出し
                    mp.writeLog(WINDOW,"checked all unread mail in search result");
                    mp.allSeenSnackbar(searchRecyclerView);
                    mp.searchPhishingMode = false;
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
