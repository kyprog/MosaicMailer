package com.example.mosaicmailer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

public class FinalQuestionDialog   extends DialogFragment {
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

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //レイアウトの呼び出し
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(activity)
                .inflate(R.layout.final_question_dialog, null);

        layout.findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンを押した時の処理
                activity.removeMosaic();
                mp.CheckAlert.dismiss();
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }
}