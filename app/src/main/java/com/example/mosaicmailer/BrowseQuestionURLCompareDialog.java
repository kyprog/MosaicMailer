package com.example.mosaicmailer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

public class BrowseQuestionURLCompareDialog extends DialogFragment {
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

        if( !Patterns.WEB_URL.matcher(mp.mailURL).matches() ){
            DialogFragment dialogFragment = new BrowseQuestionURLSuspiciousDialog();
            dialogFragment.show( getFragmentManager(), "URLSuspiciousQuestionDialog");
            dismiss();
        }



        //実際のURL
        TextView realURL = layout.findViewById(R.id.textView13);
        realURL.setText("リンク先のURL\n"+mp.realURL);

        //メールに表示されているURL
        TextView mailURL = layout.findViewById(R.id.textView14);
        mailURL.setText("メールに表示されているURL\n"+mp.mailURL);

        //メールに表示されているURLと質問文の表示
        TextView question = layout.findViewById(R.id.textView5);
        question.setText("メールに表示されているURLとリンク先のURLは同じですか？");

        layout.findViewById(R.id.YesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Yesボタンを押した時の処理
                if(mp.realURL.equals(mp.mailURL)){
                    DialogFragment dialogFragment = new BrowseQuestionURLSuspiciousDialog();
                    dialogFragment.show( getFragmentManager(), "URLSuspiciousQuestionDialog");
                    dismiss();
                }else{
                    ((TextView)layout.findViewById(R.id.textView11)).setText("違います");
                }
            }
        });

        layout.findViewById(R.id.NoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Noボタンを押した時の処理
                if(mp.realURL.equals(mp.mailURL)){
                    ((TextView)layout.findViewById(R.id.textView11)).setText("同じです");
                }else{
                    DialogFragment dialogFragment = new BrowseQuestionURLSuspiciousDialog();
                    dialogFragment.show( getFragmentManager(), "URLSuspiciousQuestionDialog");
                    dismiss();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }
}