package com.example.mosaicmailer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class BrowseQuestionFinalDialog extends DialogFragment {
    BrowseActivity activity = null;
    MailProcessing mp;
    final String WINDOW = "mail_browse_window";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BrowseActivity) {
            this.activity = (BrowseActivity) activity;
            mp = (MailProcessing) this.activity.getApplication();
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //レイアウトの呼び出し
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(activity)
                .inflate(R.layout.browse_question_final_dialog, null);

        layout.findViewById(R.id.PhishingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // フィッシングメールであるボタンを押した時の処理
                activity.removeMosaic();
                if(mp.messageFunction){
                    mp.CheckAlert.dismiss();
                }
                //フィッシングメールと判断したことを表すログを書き出す
                mp.writeLog(WINDOW,"judge this mail as phishing");

                //フィッシングメールと判断したメールを報告し削除するフェーズが始まったことを表すログを書き出す
                mp.phaseReportAndRemove = true;
                mp.writeLog(WINDOW,"start reporting & removing");

                mp.ReportAlert(activity.getWindow().getDecorView());
                mp.phishingFlag = true;
                dismiss();
            }
        });

        layout.findViewById(R.id.NoPhishingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // フィッシングメールではないボタンを押した時の処理
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Executors.newSingleThreadExecutor().execute(() -> {
                    mp.currentMessageIsAlertMessage();
                    countDownLatch.countDown();
                });
                try {countDownLatch.await();} catch (InterruptedException e) {e.printStackTrace();}

                if(mp.currentMessageIsAlert){//注意喚起メールの時
                    mp.SearchPhishingAlertInBrowse(activity.getWindow().getDecorView());
                    mp.searchPhishingMode = true;
                    mp.AlertMailSource = activity.originalHTML; //注意喚起メールの内容をmailprocessingにわたす

                    //注意喚起メールの情報をもとにフィッシングメールを探すフェーズが始まったことを表すログを書き出す
                    mp.phaseSearchPhishing = true;
                    mp.writeLog(WINDOW,"start searching for phishing mails");
                }
                activity.removeMosaic();
                if(mp.messageFunction) {
                    mp.CheckAlert.dismiss();
                }
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }
}