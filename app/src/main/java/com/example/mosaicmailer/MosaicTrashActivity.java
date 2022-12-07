package com.example.mosaicmailer;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.Executors;

public class MosaicTrashActivity extends AppCompatActivity {
    MailProcessing mp;
    RecyclerView mosaicTrashRecyclerView;
    final String WINDOW = "mosaic_trash_window";
    boolean updateFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mosaic_trash_activity);//xmlを読み込む
        /* getApplication()で自己アプリケーションクラスのインスタンスを拾う */
        mp = (MailProcessing) this.getApplication();

        //開いた画面のログの書き出し
        mp.writeLog(WINDOW,"open " + WINDOW);

        //モザイクメーラ用ゴミ箱のメール一覧取得
        Executors.newSingleThreadExecutor().execute(() -> mp.getMosaicTrashAll());

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //searchRecyclerView
        mosaicTrashRecyclerView = findViewById(R.id.mosaic_trash_recycler_view);
        // RecyclerViewのレイアウトサイズを変更しない設定をONにする
        // パフォーマンス向上のための設定。
        mosaicTrashRecyclerView.setHasFixedSize(true);
        // RecyclerViewにlayoutManagerをセットする。
        // このlayoutManagerの種類によって「1列のリスト」なのか「２列のリスト」とかが選べる。
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mosaicTrashRecyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter mainAdapter = new MosaicTrashAdapter(getApplication(), mosaicTrashRecyclerView);
        mosaicTrashRecyclerView.setAdapter(mainAdapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            updateFlag = true;
            while(true) {
                if(updateFlag){
                    //System.out.println("true:update:");
                    mp.reloadMessageList("MosaicTrash");
                    HandlerCompat.createAsync(getMainLooper()).post(() -> {
                        ((MosaicTrashAdapter) mainAdapter).reload(mp.MosaicTrashList);

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

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
