package com.example.mosaicmailer;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MailListAdapter extends RecyclerView.Adapter<MailListAdapter.MainViewHolder> {

    Context activity;
    private List<mailData> mailDataList;
    static RecyclerView tmprecyclerView;
    MailProcessing mp;

    MailListAdapter(Context context, List<mailData> mailDataList, RecyclerView recyclerView) {
        activity=context;
        this.mailDataList = mailDataList;
        tmprecyclerView = recyclerView;
        mp = (MailProcessing)activity;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_viewholder, parent, false);
        //ViewHolderを生成
        final MainViewHolder holder = new MainViewHolder(view);

        return holder;
    }

    /**
     * ViewHolderとRecyclerViewをバインドする
     * 一行のViewに対して共通でやりたい処理をここで書く。今回はテキストのセットしかしてないけど。
     */
    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        mailData mailData = this.mailDataList.get(position);
        if(mailData.unread){
            holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + mailData.sender + "</B></font>"));
            holder.title.setText(Html.fromHtml("<B>" + mailData.title + "</B>"));
        }else {
            holder.sender.setText(Html.fromHtml(mailData.sender));
            holder.title.setText(Html.fromHtml(mailData.title));
        }

        //リストをタップしたときにデータを持って遷移する為の処理
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ps = holder.getLayoutPosition();
                System.out.println(ps);

                if(ps<mp.oldestMailPosition+1 && !mp.showSearchHeadUpAlertFlag && !mp.SearchHeadUpFlag) {//探してない状態で，注意喚起メールアラートが出ていない状態で，一番下の未読メールまでスクロールせず，メールを開こうとしたとき
                    mp.showSearchHeadUpAlert(v);
                    mp.changeShowSearchHeadUpAlertFlag(true);
                }else if(mp.SearchHeadUpFlag){//探している状態で
                    Intent intent = new Intent(activity, MailBrowseActivity.class);
                    // Activity以外からActivityを呼び出すためのフラグを設定
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 引き渡す値
                    intent.putExtra("position", ps);
                    activity.startActivity(intent);
                }

            }
        });
        holder.mrecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                int ps = holder.getLayoutPosition();
                System.out.println(ps);
                if( !(ps<mp.oldestMailPosition+1) && !mp.SearchHeadUpFlag){
                    if( mp.showSearchHeadUpAlertFlag ){
                        mp.SearchHeadUpAlert.dismiss();
                        mp.changeShowSearchHeadUpAlertFlag(false);
                    }
                    mp.changeSearchHeadUpFlag(true);
                }
            }
        });
    }

    /**
     * リストの行数
     */
    @Override
    public int getItemCount() {
        return mailDataList.size();
    }
}
