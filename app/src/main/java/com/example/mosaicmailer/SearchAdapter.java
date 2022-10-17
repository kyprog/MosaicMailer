package com.example.mosaicmailer;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MainViewHolder> {

    Context activity;
    private List<Message> SearchResultList = new ArrayList<Message>();
    int lenSearchResultList = 0;
    static RecyclerView tmprecyclerView;
    MailProcessing mp;

    SearchAdapter(Context context, RecyclerView recyclerView) {
        activity=context;
        tmprecyclerView = recyclerView;
        mp = (MailProcessing)activity;
        SearchResultList.addAll(mp.SearchResultList);
    }

    /**
     * 一行分のデータ
     * ViewHolderはRecyclerViewのrowとの繋ぎ役
     * リスト内のビューはビューホルダーオブジェクトの中身(インスタンス)が表示される。
     */
    static class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView sender,title;
        LinearLayout linearLayout;
        RecyclerView mrecyclerView;

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.sender_image);
            sender = itemView.findViewById(R.id.sender_name);
            title = itemView.findViewById(R.id.title);
            linearLayout = itemView.findViewById(R.id.mail_list);
            mrecyclerView = tmprecyclerView;
        }
    }

    /**
     * ViewHolder作るメソッド
     * 最初しか呼ばれない。
     * ここでViewHolderのlayoutファイルをインフレーとして生成したViewHolderをRecyclerViewに返す。
     */
    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.index_item_recycler_viewholder, parent, false);
        //ViewHolderを生成
        final SearchAdapter.MainViewHolder holder = new SearchAdapter.MainViewHolder(view);
        return holder;
    }

    /**
     * ViewHolderとRecyclerViewをバインドする
     * 一行のViewに対して共通でやりたい処理をここで書く。今回はテキストのセットしかしてないけど。
     */
    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.MainViewHolder holder, int position) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Message SearchResult = this.SearchResultList.get(position);
                boolean unread = !SearchResult.getFlags().contains(Flags.Flag.SEEN);
                final InternetAddress addrFrom = (InternetAddress) SearchResult.getFrom()[0];

                if(unread){
                    if(addrFrom.getPersonal()==null){
                        holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getAddress() + "</B></font>"));
                    }else{
                        holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getPersonal() + "</B></font>"));
                    }
                    holder.title.setText(Html.fromHtml("<B>" + SearchResult.getSubject() + "</B>"));
                }else {
                    if(addrFrom.getPersonal()==null){
                        holder.sender.setText(Html.fromHtml(addrFrom.getAddress()));
                    }else{
                        holder.sender.setText(Html.fromHtml(addrFrom.getPersonal()));
                    }
                    holder.title.setText(Html.fromHtml(SearchResult.getSubject()));
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //リストをタップしたときにデータを持って遷移する為の処理
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ps = holder.getLayoutPosition();
                System.out.println(ps);
                Intent intent = new Intent(activity, BrowseActivity.class);
                // Activity以外からActivityを呼び出すためのフラグを設定
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //開く位置のセット
                mp.setOpenSearchResultListPosition(ps);
                // 引き渡す値
                intent.putExtra("ListType", "Search");
                activity.startActivity(intent);
            }
        });
    }

    /**
     * リストの行数
     */
    @Override
    public int getItemCount() {
        return SearchResultList.size();
    }
}
