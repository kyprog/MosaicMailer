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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class URLSuspiciousQuestionDialog  extends DialogFragment {
    MailBrowseActivity activity = null;
    MailProcessing mp;
    String URLdomain;
    int quiestionsIndex = -1;
    boolean judgeFlag = false;
    TextView judgeText;
    String[] quiestions = {
            "一般的でないTLDが使われているか",
            "全角の文字が使用されているか",
            /*"サブドメインに公式ドメインが使われているかどうか",*/
            "IPアドレスが使用されているか"
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
        quiestionsIndex = rand.nextInt(quiestions.length);
        TextView question1 = layout.findViewById(R.id.textView4);
        question1.setText(quiestions[quiestionsIndex]);

        //URLのドメインを取得
        URLdomain=extractDomain();

        judgeText = layout.findViewById(R.id.textView12);

        //質問文2の表示
        TextView question2 = layout.findViewById(R.id.textView6);
        question2.setText("上記を踏まえて，このURLは怪しいですか");

        layout.findViewById(R.id.SuspectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 怪しいボタンを押した時の処理
                switch(quiestionsIndex){
                    case 0://一般的でないTLDが使われているか
                        if(!judgeCommonlyTLD(URLdomain)){
                            judgeFlag = true;
                        }else{
                            judgeText.setText("一般的なTLDです．もう一度確認してください");
                        }
                        break;
                    case 1://全角の文字が使用されているか
                        if(judgeContain2ByteC(URLdomain)){
                            judgeFlag = true;
                        }else{
                            judgeText.setText("全角の文字は使用されていません．もう一度確認してください");
                        }
                        break;
                    case 2://IPアドレスが使用されているか
                        if(judgeIP(URLdomain)){
                            judgeFlag = true;
                        }else{
                            judgeText.setText("IPアドレスは使用されていません．もう一度確認してください");
                        }
                        break;
                }
                if(judgeFlag){
                    activity.setChecked(mp.linkInfoIndex);
                    if(activity.checkedAll()){
                        DialogFragment dialogFragment = new FinalQuestionDialog();
                        dialogFragment.show( getFragmentManager(), "FinalQuestionDialog");
                    }
                    dismiss();
                }
            }
        });

        layout.findViewById(R.id.NoSuspectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 怪しくないボタンを押した時の処理
                switch(quiestionsIndex){
                    case 0://一般的でないTLDが使われているか
                        if(judgeCommonlyTLD(URLdomain)){
                            judgeFlag = true;
                        }else{
                            judgeText.setText("一般的でないTLDです．もう一度確認してください");
                        }
                        break;
                    case 1://全角の文字が使用されているか
                        if(!judgeContain2ByteC(URLdomain)){
                            judgeFlag = true;
                        }else{
                            judgeText.setText("全角の文字が使用されています．もう一度確認してください");
                        }
                        break;
                    case 2://IPアドレスが使用されているか
                        if(!judgeIP(URLdomain)){
                            judgeFlag = true;
                        }else{
                            judgeText.setText("IPアドレスが使用されています．もう一度確認してください");
                        }
                        break;
                }
                if(judgeFlag){
                    activity.setChecked(mp.linkInfoIndex);
                    if(activity.checkedAll()){
                        DialogFragment dialogFragment = new FinalQuestionDialog();
                        dialogFragment.show( getFragmentManager(), "FinalQuestionDialog");
                    }
                    dismiss();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }

    public String extractDomain() {
        String domain;
        String url = mp.realURL;

        if (url.contains("://")) {
            domain = url.split("/")[2];
        }
        else {
            domain = url.split("/")[0];
        }

        domain = domain.split(":")[0];
        System.out.println(domain);

        return domain;
    }

    public boolean judgeCommonlyTLD(String domain){
        //一般的なTLDが使われているかどうかの判定
        if(domain.endsWith(".com")||
                domain.endsWith(".jp")){
            return true;
        }
        return false;

    }

    public boolean judgeContain2ByteC(String domain){
        //全角の文字が使用されているかどうかの判定
        char[] chars = domain.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if ((c <= '\u007e') || // 英数字
                    (c == '\u00a5') || // \記号
                    (c == '\u203e') || // ~記号
                    (c >= '\uff61' && c <= '\uff9f') // 半角カナ
            ) {
                //System.out.print("半");
            } else {
                return true; //System.out.print("全");
            }
        }
        /*
        try {
            if((domain.getBytes("SJIS").length) == domain.length()){return true;}
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

        return false;
    }

    /*
    public boolean judgeSubDomain(String domain){
        //サブドメインに公式ドメインが使われているかどうかの判定
    }*/

    public boolean judgeIP(String domain){
        //IPアドレスが使用されているかどうかの判定
        String IPregex = "^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$";
        Pattern IPptrn = Pattern.compile(IPregex, Pattern.CASE_INSENSITIVE);
        Matcher IPmtch = IPptrn.matcher(domain);
        if( IPmtch.find() ){return true;}
        return false;
    }

}