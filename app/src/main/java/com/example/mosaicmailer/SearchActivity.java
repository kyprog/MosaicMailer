package com.example.mosaicmailer;

import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {
    MailProcessing mp;
    RecyclerView searchRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing)this.getApplication();

        //searchRecyclerView
        searchRecyclerView = findViewById(R.id.search_recycler_view);
        // RecyclerViewのレイアウトサイズを変更しない設定をONにする
        // パフォーマンス向上のための設定。
        searchRecyclerView.setHasFixedSize(true);
        // RecyclerViewにlayoutManagerをセットする。
        // このlayoutManagerの種類によって「1列のリスト」なのか「２列のリスト」とかが選べる。
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(layoutManager);


        //検索ボックスに入力されたときの処理を定義
        SearchView sv = findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    //javamailで検索し，該当するメッセージ一覧を取得する
                    mp.searchMessages(s);

                    //処理結果をhandler経由でUIに反映
                    HandlerCompat.createAsync(getMainLooper()).post(() ->{
                        // Adapter生成してRecyclerViewにセット
                        RecyclerView.Adapter mainAdapter = new SearchAdapter(getApplication(), searchRecyclerView);
                        searchRecyclerView.setAdapter(mainAdapter);
                    });
                });


                return false;
            }
        });
    }
}
