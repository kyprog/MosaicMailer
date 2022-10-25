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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

public class CreateAdapter extends RecyclerView.Adapter<CreateAdapter.MainViewHolder>{

    Context activity;
    private List<MimeBodyPart> attachmentList = new ArrayList<MimeBodyPart>();
    static RecyclerView tmprecyclerView;
    MailProcessing mp;

    CreateAdapter(Context context, RecyclerView recyclerView) {
        activity=context;
        tmprecyclerView = recyclerView;
        mp = (MailProcessing)activity;

    }

    /**
     * 一行分のデータ
     * ViewHolderはRecyclerViewのrowとの繋ぎ役
     * リスト内のビューはビューホルダーオブジェクトの中身(インスタンス)が表示される。
     */
    static class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView cancelButton;
        TextView fileName;
        RecyclerView mrecyclerView;

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            fileName = itemView.findViewById(R.id.fileName);
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
    public CreateAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_item_recycler_viewholder, parent, false);
        //ViewHolderを生成
        final CreateAdapter.MainViewHolder holder = new CreateAdapter.MainViewHolder(view);
        return holder;
    }

    /**
     * ViewHolderとRecyclerViewをバインドする
     * 一行のViewに対して共通でやりたい処理をここで書く。今回はテキストのセットしかしてないけど。
     */
    @Override
    public void onBindViewHolder(@NonNull CreateAdapter.MainViewHolder holder, int position) {
        int pos = position;

        try {//ファイル名のセット
            holder.fileName.setText(attachmentList.get(pos).getFileName());
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        //キャンセルボタンををタップしたときの処理
        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(pos);
                deleteAttachment(pos);
            }
        });
    }

    //リストの行数
    @Override
    public int getItemCount() {
        return attachmentList.size();
    }

    //添付ファイルリストから特定の添付ファイルの削除
    private void deleteAttachment(int delPos){
        attachmentList.remove(delPos);
        notifyDataSetChanged();
    }

    //添付ファイルの追加
    public void addAttachment(MimeBodyPart attachment){
        attachmentList.add(attachment);
        notifyDataSetChanged();
    }

    //添付ファイル一覧返す
    public List<MimeBodyPart> getAttachmentList(){
        return attachmentList;
    }
}
