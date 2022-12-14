package com.example.mosaicmailer;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public class MosaicTrashAdapter extends RecyclerView.Adapter<MosaicTrashAdapter.MainViewHolder> {
    Context activity;
    private List<Message> MosaicTrashList = new ArrayList<Message>();
    int lenMosaicTrashList = 0;
    static RecyclerView tmprecyclerView;
    MailProcessing mp;
    final String WINDOW = "mosaic_trash_window";

    MosaicTrashAdapter(Context context, RecyclerView recyclerView) {
        activity=context;
        tmprecyclerView = recyclerView;
        mp = (MailProcessing)activity;
        initMailData();
    }

    @NonNull
    public void initMailData(){
        int window=50;
        int addPoint=MosaicTrashList.size();
        if(addPoint+window < mp.MosaicTrashList.size()){
            MosaicTrashList.addAll(mp.MosaicTrashList.subList(addPoint, addPoint+window));
        }else if( (addPoint+window >= mp.MosaicTrashList.size()) && !(addPoint>=mp.MosaicTrashList.size())){
            MosaicTrashList.addAll(mp.MosaicTrashList.subList(addPoint, mp.MosaicTrashList.size()));
        }
        lenMosaicTrashList = MosaicTrashList.size();
    }

    @NonNull
    public void addMailData(){
        int window=50;
        int addPoint=MosaicTrashList.size();
        if(addPoint+window < mp.MosaicTrashList.size()){
            MosaicTrashList.addAll(mp.MosaicTrashList.subList(addPoint, addPoint+window));
        }else if( (addPoint+window >= mp.MosaicTrashList.size()) && !(addPoint>=mp.MosaicTrashList.size())){
            MosaicTrashList.addAll(mp.MosaicTrashList.subList(addPoint, mp.MosaicTrashList.size()));
        }
        lenMosaicTrashList = MosaicTrashList.size();
        notifyItemRangeInserted(addPoint, window);
    }

    public void reload(List<Message> mosaicTrashList) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        try {
            /* ?????? */
            mainHandler.post(() -> {
                MosaicTrashList = mosaicTrashList;
                notifyDataSetChanged();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????
     * ViewHolder???RecyclerView???row???????????????
     * ???????????????????????????????????????????????????????????????????????????(??????????????????)?????????????????????
     */
    static class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView image,star;
        TextView sender,title;
        LinearLayout linearLayout;
        RecyclerView mrecyclerView;

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            //image = itemView.findViewById(R.id.sender_image);
            star = itemView.findViewById(R.id.star);
            sender = itemView.findViewById(R.id.sender_name);
            title = itemView.findViewById(R.id.title);
            linearLayout = itemView.findViewById(R.id.mail_list);
            mrecyclerView = tmprecyclerView;
        }
    }

    /**
     * ViewHolder??????????????????
     * ??????????????????????????????
     * ?????????ViewHolder???layout???????????????????????????????????????????????????ViewHolder???RecyclerView????????????
     */
    @NonNull
    @Override
    public MosaicTrashAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.index_item_recycler_viewholder, parent, false);
        //ViewHolder?????????
        final MosaicTrashAdapter.MainViewHolder holder = new MosaicTrashAdapter.MainViewHolder(view);
        return holder;
    }

    /**
     * ViewHolder???RecyclerView?????????????????????
     * ?????????View????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public void onBindViewHolder(@NonNull MosaicTrashAdapter.MainViewHolder holder, int position) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Message MosaicTrash = this.MosaicTrashList.get(position);
                boolean unread = !MosaicTrash.getFlags().contains(Flags.Flag.SEEN);
                boolean flagged = MosaicTrash.getFlags().contains(Flags.Flag.FLAGGED);
                if(flagged){
                    holder.star.setImageResource(R.drawable.ic_baseline_star_36);
                }else{
                    holder.star.setImageResource(R.drawable.ic_baseline_star_outline_36);
                }
                final InternetAddress addrFrom = (InternetAddress) MosaicTrash.getFrom()[0];

                if(unread){
                    if(addrFrom.getPersonal()==null){
                        holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getAddress() + "</B></font>"));
                    }else{
                        holder.sender.setText(Html.fromHtml("<font color=\"blue\"><B>" + addrFrom.getPersonal() + "</B></font>"));
                    }
                    holder.title.setText(Html.fromHtml("<B>" + MosaicTrash.getSubject() + "</B>"));
                }else {
                    if(addrFrom.getPersonal()==null){
                        holder.sender.setText(Html.fromHtml(addrFrom.getAddress()));
                    }else{
                        holder.sender.setText(Html.fromHtml(addrFrom.getPersonal()));
                    }
                    holder.title.setText(Html.fromHtml(MosaicTrash.getSubject()));
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

        //?????????????????????????????????????????????????????????????????????????????????
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ps = holder.getLayoutPosition();
                Executors.newSingleThreadExecutor().execute(() -> {
                    mp.touchedMosaicTrashListPosition = ps;
                    String subject = mp.restoration();
                    //????????????????????????????????????????????????????????????
                    mp.writeLog(WINDOW,"restore mail \""+subject+"\"");
                    mp.reloadMessageList("MosaicTrash");
                    reload(mp.MosaicTrashList);
                });
            }
        });

        holder.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ps = holder.getLayoutPosition();
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean flagged = mp.FlaggedinMosaicTrashList(ps);
                    if(flagged){
                        holder.star.setImageResource(R.drawable.ic_baseline_star_outline_36);
                        mp.setFlaggedinMosaicTrashList(ps, false);
                    }else{
                        holder.star.setImageResource(R.drawable.ic_baseline_star_36);
                        mp.setFlaggedinMosaicTrashList(ps, true);
                    }
                });
            }
        });

        holder.mrecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                int ps = holder.getLayoutPosition();
                if(ps==lenMosaicTrashList-1){
                    addMailData();
                }
            }
        });
    }

    /**
     * ??????????????????
     */
    @Override
    public int getItemCount() {
        return MosaicTrashList.size();
    }
}
