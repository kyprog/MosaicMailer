package com.example.mosaicmailer;


import static android.os.Looper.getMainLooper;

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
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.MainViewHolder> {

    Context activity;
    private List<Message> mailDataList = new ArrayList<Message>();
    int lenMailDataList = 0;
    static RecyclerView tmprecyclerView;
    MailProcessing mp;
    LinearLayoutManager mLinearLayoutManager;


    IndexAdapter(Context context, RecyclerView recyclerView, LinearLayoutManager layoutManager) {
        activity=context;
        tmprecyclerView = recyclerView;
        mp = (MailProcessing)activity;
        mLinearLayoutManager = layoutManager;
        initMailData();

    }


    @NonNull
    public void initMailData(){
        int window=50;
        int addPoint=mailDataList.size();
        if(addPoint+window < mp.MessageList.size()){
            mailDataList.addAll(mp.MessageList.subList(addPoint, addPoint+window));
        }else if( (addPoint+window >= mp.MessageList.size()) && !(addPoint>=mp.MessageList.size())){
            mailDataList.addAll(mp.MessageList.subList(addPoint, mp.MessageList.size()-1));
        }
        lenMailDataList = mailDataList.size();
    }

    @NonNull
    public void addMailData(){
        int window=50;
        int addPoint=mailDataList.size();
        if(addPoint+window < mp.MessageList.size()){
            mailDataList.addAll(mp.MessageList.subList(addPoint, addPoint+window));
        }else if( (addPoint+window >= mp.MessageList.size()) && !(addPoint>=mp.MessageList.size())){
            mailDataList.addAll(mp.MessageList.subList(addPoint, mp.MessageList.size()-1));
        }
        lenMailDataList = mailDataList.size();
        notifyItemRangeInserted(addPoint, window);
    }

    public void reload(List<Message> messageList) {
        mailDataList = messageList;
        notifyDataSetChanged();
    }

    /**
     * 一行分のデータ
     * ViewHolderはRecyclerViewのrowとの繋ぎ役
     * リスト内のビューはビューホルダーオブジェクトの中身(インスタンス)が表示される。
     */
    static class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView senderImage,star;
        TextView sender,title;
        LinearLayout linearLayout;
        RecyclerView mrecyclerView;

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            senderImage = itemView.findViewById(R.id.sender_image);
            star = itemView.findViewById(R.id.star);
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
        final MainViewHolder holder = new MainViewHolder(view);
        return holder;
    }

    /**
     * ViewHolderとRecyclerViewをバインドする
     * 一行のViewに対して共通でやりたい処理をここで書く。
     */
    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Message mailData = mailDataList.get(position);
                boolean unread = !mailData.getFlags().contains(Flags.Flag.SEEN);
                boolean flagged = mailData.getFlags().contains(Flags.Flag.FLAGGED);
                //System.out.println("position="+position+"/flagged="+flagged);
                if(flagged){
                    holder.star.setImageResource(R.drawable.ic_baseline_star_36);
                }else{
                    holder.star.setImageResource(R.drawable.ic_baseline_star_outline_36);
                }
                final InternetAddress addrFrom = (InternetAddress) mailData.getFrom()[0];

                if(mp.searchAlertMode){
                    if(mp.openMessageListPosition == position){
                        if(unread){
                            if(addrFrom.getPersonal()==null){
                                holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getAddress() + "</B></font>"));
                            }else{
                                holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getPersonal() + "</B></font>"));
                            }
                            String subject = mailData.getSubject();
                            if(subject == null){
                                holder.title.setText(Html.fromHtml("<B>件名なし</B>"));
                            }else{
                                holder.title.setText(Html.fromHtml("<B>" + subject + "</B>"));
                            }
                        }else {
                            if(addrFrom.getPersonal()==null){
                                holder.sender.setText(Html.fromHtml(addrFrom.getAddress()));
                            }else{
                                holder.sender.setText(Html.fromHtml(addrFrom.getPersonal()));
                            }
                            String subject = mailData.getSubject();
                            if(subject == null){
                                holder.title.setText(Html.fromHtml("件名なし"));
                            }else{
                                holder.title.setText(Html.fromHtml(subject));
                            }
                        }
                    }else {
                        if(unread){
                            if(addrFrom.getPersonal()==null){
                                holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>□□□□□□□□□□□□□□</B></font>"));
                            }else{
                                holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>□□□□□□□□□□□□□□</B></font>"));
                            }
                            holder.title.setText(Html.fromHtml("<B>□□□□□□□□□□□□□□□□□□□□□□□□□□□□</B>"));
                        }else {
                            if(addrFrom.getPersonal()==null){
                                holder.sender.setText(Html.fromHtml("□□□□□□□□□□□□□□"));
                            }else{
                                holder.sender.setText(Html.fromHtml("□□□□□□□□□□□□□□"));
                            }
                            holder.title.setText(Html.fromHtml("□□□□□□□□□□□□□□□□□□□□□□□□□□□□"));
                        }
                    }
                }else{
                    if(unread){
                        if(addrFrom.getPersonal()==null){
                            holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getAddress() + "</B></font>"));
                        }else{
                            holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getPersonal() + "</B></font>"));
                        }
                        String subject = mailData.getSubject();
                        if(subject == null){
                            holder.title.setText(Html.fromHtml("<B>件名なし</B>"));
                        }else{
                            holder.title.setText(Html.fromHtml("<B>" + subject + "</B>"));
                        }
                    }else {
                        if(addrFrom.getPersonal()==null){
                            holder.sender.setText(Html.fromHtml(addrFrom.getAddress()));
                        }else{
                            holder.sender.setText(Html.fromHtml(addrFrom.getPersonal()));
                        }
                        String subject = mailData.getSubject();
                        if(subject == null){
                            holder.title.setText(Html.fromHtml("件名なし"));
                        }else{
                            holder.title.setText(Html.fromHtml(subject));
                        }
                    }
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
                //System.out.println(ps);
                if(!mp.SearchHeadUpFlag && mLinearLayoutManager.getChildCount()>=mp.oldestMailPosition){
                    //System.out.println("child"+mLinearLayoutManager.getChildCount());
                    mp.changeSearchHeadUpFlag(true);
                }
                if( !(mp.searchAlertMode&&(ps!=mp.openMessageListPosition)) ){
                    Executors.newSingleThreadExecutor().execute(() -> {
                        boolean isAlert = mp.isAlertMessege(ps);
                        HandlerCompat.createAsync(getMainLooper()).post(() ->{
                            if(mp.existAlert && isAlert){
                                Intent intent = new Intent(activity, BrowseActivity.class);
                                // Activity以外からActivityを呼び出すためのフラグを設定
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //開く位置のセット
                                mp.setOpenMessageListPosition(ps);
                                // 引き渡す値
                                intent.putExtra("ListType", "MailList");
                                activity.startActivity(intent);

                            }else if(!mp.showSearchHeadUpAlertFlag && !mp.SearchHeadUpFlag) {//探してない状態で，注意喚起メールアラートが出ていない状態で，一番下の未読メールまでスクロールせず，メールを開こうとしたとき

                                mp.showSearchHeadUpAlert(v);
                                mp.changeShowSearchHeadUpAlertFlag(true);

                            }else if(!mp.existAlert && mp.SearchHeadUpFlag){//探した状態で
                                Intent intent = new Intent(activity, BrowseActivity.class);
                                // Activity以外からActivityを呼び出すためのフラグを設定
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //開く位置のセット
                                mp.setOpenMessageListPosition(ps);
                                // 引き渡す値
                                intent.putExtra("ListType", "MailList");
                                activity.startActivity(intent);
                            }
                        });
                    });
                }
            }
        });
        holder.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ps = holder.getLayoutPosition();
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean flagged = mp.FlaggedinMessageList(ps);
                    if(flagged){
                        holder.star.setImageResource(R.drawable.ic_baseline_star_outline_36);
                        mp.setFlaggedinMessageList(ps, false);
                    }else{
                        holder.star.setImageResource(R.drawable.ic_baseline_star_36);
                        mp.setFlaggedinMessageList(ps, true);
                    }
                });
            }
        });
        holder.mrecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                //System.out.println("dy:"+dy);
                //System.out.println("dy:"+dy);
                System.out.println("topPosition" + mLinearLayoutManager.findFirstVisibleItemPosition() +
                        "/" + "bottomPosition" + mLinearLayoutManager.findLastVisibleItemPosition());
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                int ps = holder.getLayoutPosition();
                //System.out.println(ps);
                if(!mp.SearchHeadUpFlag && ps+1>=mp.oldestMailPosition){
                    if( mp.showSearchHeadUpAlertFlag ){
                        mp.SearchHeadUpAlert.dismiss();
                        mp.changeShowSearchHeadUpAlertFlag(false);
                    }
                    mp.changeSearchHeadUpFlag(true);
                }

                if(ps==lenMailDataList-1){
                    addMailData();
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
