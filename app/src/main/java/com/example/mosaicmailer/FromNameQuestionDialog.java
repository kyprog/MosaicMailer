package com.example.mosaicmailer;

import static android.os.Looper.getMainLooper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.DialogFragment;

import java.util.concurrent.Executors;

public class FromNameQuestionDialog extends DialogFragment {
    MailBrowseActivity activity = null;
    MailProcessing mp;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MailBrowseActivity) {
            this.activity = (MailBrowseActivity) activity;
            mp = (MailProcessing) this.activity.getApplication();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //レイアウトの呼び出し
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(activity)
                .inflate(R.layout.sender_dialog, null);

        //差出人名の表示
        TextView senderName = layout.findViewById(R.id.textView13);
        senderName.setText("差出人名："+mp.senderName);

        //差出人のメールアドレス表示
        TextView senderMailAddress = layout.findViewById(R.id.textView14);
        senderMailAddress.setText("From："+mp.senderMailAddress);

        //質問文の表示
        TextView question = layout.findViewById(R.id.textView5);
        question.setText("差出人名に身に覚えはありますか");

        //Yesボタン押下時の処理
        layout.findViewById(R.id.YesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean exist = mp.existSender();
                    HandlerCompat.createAsync(getMainLooper()).post(() ->{
                        if(exist){//本当に,その差出人名が来ている場合
                            DialogFragment dialogFragment = new FromAddressQuestionDialog();
                            dialogFragment.show( getFragmentManager(), "FromAddressQuestionDialog");
                            dismiss();
                        }else{//本当は,その差出人名が来ていない場合
                            TextView question = layout.findViewById(R.id.textView11);
                            question.setText("そのような差出人名からのメールは来ていません");
                        }
                    });
                });
            }
        });

        //Noボタン押下時の処理
        layout.findViewById(R.id.NoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean exist = mp.existSender();
                    HandlerCompat.createAsync(getMainLooper()).post(() ->{
                        if(exist) {//本当は,その差出人名が来ていた場合
                            TextView question = layout.findViewById(R.id.textView11);
                            question.setText("本当に覚えはないのですか？");
                            dismiss();
                        }else{//本当に,その差出人名が来ていない場合
                            //気をつけてねダイアログ遷移
                            dismiss();
                        }
                    });
                });
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }
}
