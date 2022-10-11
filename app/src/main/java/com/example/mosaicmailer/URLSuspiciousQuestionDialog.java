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

import java.util.Random;

public class URLSuspiciousQuestionDialog  extends DialogFragment {
    MailBrowseActivity activity = null;
    MailProcessing mp;
    String[] quiestions = {
            "一般的でないTLDが使われているか",
            "全角の文字が使用されているか",
            "サブドメインに公式ドメインが使われているかどうか",
            "IPアドレスが使用されているか",
            "全角文字が使用されているか"
    };

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
                .inflate(R.layout.url_suspect_dialog, null);

        //実際のURL
        TextView realURL = layout.findViewById(R.id.textView2);
        realURL.setText("リンク先のURL\n"+mp.realURL);

        //質問文1の表示
        Random rand = new Random();
        int quiestionsIndex = rand.nextInt(quiestions.length);
        TextView question1 = layout.findViewById(R.id.textView4);
        question1.setText(quiestions[quiestionsIndex]);

        //質問文2の表示
        TextView question2 = layout.findViewById(R.id.textView6);
        question2.setText("上記を踏まえて，このURLは怪しいですか");

        layout.findViewById(R.id.NoSuspectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンを押した時の処理
                DialogFragment dialogFragment = new FinalQuestionDialog();
                dialogFragment.show( getFragmentManager(), "FinalQuestionDialog");
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }
}