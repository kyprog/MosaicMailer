package com.example.mosaicmailer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

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

        layout.findViewById(R.id.button8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンを押した時の処理
                DialogFragment dialogFragment = new FromAddressQuestionDialog();
                dialogFragment.show( getFragmentManager(), "FromAddressQuestionDialog");
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }
}
