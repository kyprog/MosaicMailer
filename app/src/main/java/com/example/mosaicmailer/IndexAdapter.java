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
    final String WINDOW = "mail_index_window";


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
            mailDataList.addAll(mp.MessageList.subList(addPoint, mp.MessageList.size()));
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
            mailDataList.addAll(mp.MessageList.subList(addPoint, mp.MessageList.size()));
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
            //senderImage = itemView.findViewById(R.id.sender_image);
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

                if(mp.searchPhishingMode){
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

                CountDownLatch countDownLatch = new CountDownLatch(1);
                Executors.newSingleThreadExecutor().execute(() -> {
                    mp.isAlertMail = mp.isAlertMessage(ps);
                    boolean isLatestAlert = mp.isLatestAlert(ps);
                    boolean isUnreadAlert = mp.isUnreadAlertMessege(ps);
                    try {
                        System.out.println("mp.habitFunction="+mp.habitFunction);
                        System.out.println("mp.phaseSearchAlertMail="+mp.phaseSearchAlertMail);
                        System.out.println("mp.phaseSearchPhishing="+mp.phaseSearchPhishing);
                        System.out.println("mp.scrolledBottomUnread="+mp.scrolledBottomUnread);
                        System.out.println("mp.existAlert="+mp.existAlert);
                        if (mp.habitFunction) {//習慣化機能onの場合
                            if (mp.phaseSearchPhishing) {//フィッシングメールを探すフェーズ
                                if (isLatestAlert) {//見た注意喚起メールをタップした場合
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW,"tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                    openMessage(ps);
                                }
                                else {//それ以外をタップした場合
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can't open]");
                                    mp.showSearchPhishingAlert(v);
                                }
                            } else if (mp.phaseSearchAlertMail) {//注意喚起メールを探すフェーズ
                                if (mp.scrolledBottomUnread == false) {//一番下の未読メールまでスクロールしていないでタップした場合
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can't open]");
                                    mp.showSearchHeadUpAlert(v);
                                    mp.changeShowSearchedHeadUpAlertFlag(true);
                                }
                                else if (mp.existAlert) {//未読の注意喚起メールがある場合
                                    if (isUnreadAlert) {//未読の注意喚起メールをタップした場合
                                        //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                        //注意喚起メールがタップされたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW,"tap unread alertMail");
                                        mp.latestAlertMail = mp.MessageList.get(ps);
                                        openMessage(ps);
                                    }
                                    else {//未読の注意喚起メール以外をタップした場合
                                        //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can't open]");
                                        mp.showOpenHeadUpAlert(v);
                                    }
                                }
                            }
                            else {//注意喚起メールを探すフェーズでもフィッシングメールを探すフェーズでもない
                                //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                openMessage(ps);
                            }
                        }
                        else {//習慣機能off
                            if (mp.phaseSearchPhishing) {//フィッシングメールを探すフェーズ
                                if (isLatestAlert) {//見た注意喚起メールをタップした場合
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW,"tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                    mp.writeLog(WINDOW,"tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                    openMessage(ps);
                                }
                                else {//それ以外をタップした場合
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                    openMessage(ps);
                                }
                            } else if (mp.phaseSearchAlertMail) {//注意喚起メールを探すフェーズ
                                if (!mp.scrolledBottomUnread) {//一番下の未読メールまでスクロールしていないでタップした場合
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                    openMessage(ps);
                                }
                                else if (mp.existAlert) {//未読の注意喚起メールがある場合
                                    if (isUnreadAlert) {//未読の注意喚起メールをタップした場合
                                        //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                        //注意喚起メールがタップされたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW,"tap unread alertMail");
                                        mp.latestAlertMail = mp.MessageList.get(ps);
                                        openMessage(ps);
                                    }
                                    else {//未読の注意喚起メール以外をタップした場合
                                        //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                        openMessage(ps);
                                    }
                                }
                            }
                            else {//注意喚起メールを探すフェーズでもフィッシングメールを探すフェーズでもない
                                //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                mp.writeLog(WINDOW, "tap mail \"" + mp.MessageList.get(ps).getSubject() + "\" [can open]");
                                openMessage(ps);
                            }
                        }
                        countDownLatch.countDown();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*
                if(mp.habitFunction){//習慣化機能onの場合
                    System.out.println("------------------------------0");
                    if( !(mp.searchPhishingMode &&(ps!=mp.openMessageListPosition)) ){//!(フィッシングメール探すフェーズで，押したメールが注意喚起メールでないとき)
                        Executors.newSingleThreadExecutor().execute(() -> {
                            boolean isAlert = mp.isAlertMessage(ps);
                            mp.isAlertMail = isAlert;
                            System.out.println(isAlert);
                            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                                System.out.println("------------------------------1");
                                if(mp.existAlert && isAlert){//注意喚起メールをタップした時
                                    System.out.println("------------------------------2");
                                    try {
                                        //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW,"tap mail \"" + mp. MessageList.get(ps).getSubject() + "\" [can open]");
                                        //注意喚起メールがタップされたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW,"tap unread alertMail");
                                    } catch (MessagingException e) {
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent(activity, BrowseActivity.class);
                                    // Activity以外からActivityを呼び出すためのフラグを設定
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    //開く位置のセット
                                    mp.setOpenMessageListPosition(ps);
                                    // 引き渡す値
                                    intent.putExtra("ListType", "MailList");
                                    activity.startActivity(intent);

                                }else if(!mp.showSearchedHeadUpAlertFlag && !mp.SearchedHeadUpFlag) {//探してない状態で，注意喚起メールアラートが出ていない状態で，一番下の未読メールまでスクロールせず，メールを開こうとしたとき
                                    try {
                                        //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW,"tap mail \"" + mp. MessageList.get(ps).getSubject() + "\" [can't open]");
                                    } catch (MessagingException e) {
                                        e.printStackTrace();
                                    }
                                    if(mp.messageFunction){
                                        mp.showSearchHeadUpAlert(v);
                                    }
                                    mp.changeShowSearchedHeadUpAlertFlag(true);

                                }else if(!mp.existAlert && mp.SearchedHeadUpFlag){//探した状態で
                                    try {
                                        //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                        mp.writeLog(WINDOW,"tap mail \"" + mp. MessageList.get(ps).getSubject() + "\" [can open]");
                                    } catch (MessagingException e) {
                                        e.printStackTrace();
                                    }
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
                    }else{
                        try {
                            //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                            mp.writeLog(WINDOW,"tap mail \"" + mp. MessageList.get(ps).getSubject() + "\" [can't open]");
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                    }
                }else{//習慣化機能off
                    Executors.newSingleThreadExecutor().execute(() -> {
                        boolean isAlert = mp.isAlertMessage(ps);
                        HandlerCompat.createAsync(getMainLooper()).post(() ->{
                            if(mp.existAlert && isAlert){//注意喚起メールをタップした時
                                try {
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW,"tap mail \"" + mp. MessageList.get(ps).getSubject() + "\" [can open]");
                                    //注意喚起メールがタップされたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW,"tap unread alertMail");
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                            }else if(!mp.showSearchedHeadUpAlertFlag && !mp.SearchedHeadUpFlag) {//探してない状態で，注意喚起メールアラートが出ていない状態で，一番下の未読メールまでスクロールせず，メールを開こうとしたとき
                                try {
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW,"tap mail \"" + mp. MessageList.get(ps).getSubject() + "\" [can open]");
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                                if(mp.messageFunction){
                                    mp.showSearchHeadUpAlert(v);
                                }
                                mp.changeShowSearchedHeadUpAlertFlag(true);
                            }else if(!mp.existAlert && mp.SearchedHeadUpFlag){//探した状態で
                                try {
                                    //タップしたメールのタイトルと開けたかどうかを表すログの書き出し
                                    mp.writeLog(WINDOW,"tap mail \"" + mp. MessageList.get(ps).getSubject() + "\" [can open]");
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                            }

                            Intent intent = new Intent(activity, BrowseActivity.class);
                            // Activity以外からActivityを呼び出すためのフラグを設定
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //開く位置のセット
                            mp.setOpenMessageListPosition(ps);
                            // 引き渡す値
                            intent.putExtra("ListType", "MailList");
                            activity.startActivity(intent);
                        });
                    });
                }*/
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
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                int ps = holder.getLayoutPosition();
                if(mp.phaseSearchAlertMail) {//注意喚起メールを探すフェーズの時
                    if (/*!mp.SearchedHeadUpFlag &&*/ ps + 1 >= mp.oldestMailPosition) {//一番下の未読メールまでスクロールした時
                        if(mp.scrolledBottomUnread == false){//一番下の未読メールまでスクロールしたと判定されてない時
                            //一番下の未読メールまでスクロールしたことを表すログの書き出し
                            mp.writeLog(WINDOW, "scroll to the bottom unread mail position");
                            mp.scrolledBottomUnread = true;
                        }

                        if (mp.showSearchedHeadUpAlertFlag) {//注意喚起メールを探せメッセージが出ている時
                            if (mp.messageFunction) {
                                mp.SearchHeadUpAlert.dismiss();
                            }
                            mp.changeShowSearchedHeadUpAlertFlag(false);
                        }
                        if (!mp.existAlert) {
                            //注意喚起メールを探すフェーズが終わったことを表すログの書き出し
                            mp.changeSearchedHeadUpFlag(true);
                            mp.phaseSearchAlertMail = false;
                            mp.writeLog(WINDOW, "end searchAlert");
                        }
                    }
                }

                if(ps==lenMailDataList-1){
                    addMailData();
                }
            }
        });
    }

    private void openMessage(int ps){
        Intent intent = new Intent(activity, BrowseActivity.class);
        // Activity以外からActivityを呼び出すためのフラグを設定
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //開く位置のセット
        mp.setOpenMessageListPosition(ps);
        // 引き渡す値
        intent.putExtra("ListType", "MailList");
        activity.startActivity(intent);
    }
    /**
     * リストの行数
     */
    @Override
    public int getItemCount() {
        return mailDataList.size();
    }
}
