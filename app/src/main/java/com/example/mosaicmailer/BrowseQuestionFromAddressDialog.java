package com.example.mosaicmailer;

import static android.os.Looper.getMainLooper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.DialogFragment;

import java.util.concurrent.Executors;

public class BrowseQuestionFromAddressDialog extends DialogFragment {
    BrowseActivity activity = null;
    MailProcessing mp;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BrowseActivity) {
            this.activity = (BrowseActivity) activity;
            mp = (MailProcessing) this.activity.getApplication();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //レイアウトの呼び出し
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(activity)
                .inflate(R.layout.browse_question_common_dialog, null);

        //差出人名の表示
        TextView senderName = layout.findViewById(R.id.textView13);
        senderName.setText("差出人名："+mp.senderName);

        //差出人のメールアドレス表示
        TextView senderMailAddress = layout.findViewById(R.id.textView14);
        senderMailAddress.setText("From："+mp.senderMailAddress);

        //質問文の表示
        TextView question = layout.findViewById(R.id.textView5);
        question.setText("その差出人名とメールアドレスの組み合わせは過去に来たことがありますか？");

        //Yesボタン押下時の処理
        layout.findViewById(R.id.YesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean exist = mp.existNameandAddress();
                    HandlerCompat.createAsync(getMainLooper()).post(() ->{
                        if(exist){//本当に来ている
                            activity.checkedMailAddress = true;
                            if(activity.checkedAll()){
                                DialogFragment dialogFragment = new BrowseQuestionFinalDialog();
                                dialogFragment.show( getFragmentManager(), "FinalQuestionDialog");
                            }
                            dismiss();
                        }
                        else{//本当は来ていない
                            TextView question = layout.findViewById(R.id.textView11);
                            question.setText("そのような組み合わせからのメールは来ていません");
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
                    boolean exist = mp.existNameandAddress();
                    HandlerCompat.createAsync(getMainLooper()).post(() ->{
                        if(exist){//本当は来ている
                            TextView question = layout.findViewById(R.id.textView11);
                            question.setText("その組み合わせからのメールは来ていますが？");
                        }
                        else{//本当に来ていない
                            activity.checkedMailAddress = true;
                            if(activity.checkedAll()){
                                DialogFragment dialogFragment = new BrowseQuestionFinalDialog();
                                dialogFragment.show( getFragmentManager(), "FinalQuestionDialog");
                            }
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
