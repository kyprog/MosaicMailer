package com.example.mosaicmailer;

import android.app.Application;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

public class MailProcessing extends Application {
    Session session=null;
    Store store = null;
    Folder inbox = null;
    List<Message> MessageList = new ArrayList<Message>();
    int openMailPosition=0;
    int oldestMailPosition=0;
    boolean SearchHeadUpFlag=false;//注意喚起メールを探したかどうかのフラグ
    boolean showSearchHeadUpAlertFlag=false;//SearchHeadUpAlertアラートが出現しているかどうか
    Snackbar SearchHeadUpAlert = null;
    Snackbar CheckAlert = null;
    String realURL = "";
    String mailURL = "";
    String senderName = "";
    String senderMailAddress = "";

    @Override
    public void onCreate() {
        /** Called when the Application-class is first created. */
        super.onCreate();
        showSearchHeadUpAlertFlag=false;
    }

    @Override
    public void onTerminate() {
        /** This Method Called when this Application finished. */
        super.onTerminate();
        try {
            inbox.close(false);
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void connect(String username, String password) {
        try{
            Properties props = new Properties();
            props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.imap.socketFactory.fallback", "false");
            props.put("mail.imap.socketFactory.port", "993");
            props.put("mail.imap.port", "993");
            props.put("mail.imap.host", "outlook.office365.com");
            props.put("mail.imap.user", username);
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.ssl.protocols", "TLSv1.2");

            session = Session.getInstance(props, null);

            // 4. Get the POP3 store provider and connect to the store.
            store = session.getStore("imap");
            store.connect("outlook.office365.com", 993, username, password);

            // 5. Get folder and open the INBOX folder in the store.
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public void getMailListAll(){
        try {
            MessageList = Arrays.asList(inbox.getMessages());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        //新しい順になるように逆順に並び替える
        Collections.reverse(MessageList);
    }
    /*
    public void getMailListN(int getListLen) {// メールをn件取得する．
        try {
            int message_count = inbox.getMessageCount();
            MessageList = Arrays.asList(inbox.getMessages(message_count - getListLen + 1, message_count));

            //新しい順になるように逆順に並び替える
            Collections.reverse(MessageList);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }*/

    public void setOpenMailPosition(int position) {// 開くメールの位置番号をsetする
        openMailPosition = position;
    }

    public void changeSearchHeadUpFlag(boolean status) {
        SearchHeadUpFlag = status;
    }

    public void changeShowSearchHeadUpAlertFlag(boolean status) {
        showSearchHeadUpAlertFlag = status;
    }

    public void showSearchHeadUpAlert(View v){
        SearchHeadUpAlert = Snackbar.make(v, "一番下の未読メールまでスクロールして注意喚起メールを探してください", Snackbar.LENGTH_INDEFINITE);
        SearchHeadUpAlert.setBackgroundTint(getResources().getColor(R.color.red));
        SearchHeadUpAlert.setTextColor(getResources().getColor(R.color.black));
        SearchHeadUpAlert.show();

    }

    public void showCheckAlert(View v){
        CheckAlert = Snackbar.make(v, "フィッシングメールかもしれません\nメールアドレスとURLを確認してください", Snackbar.LENGTH_INDEFINITE);
        CheckAlert.setBackgroundTint(getResources().getColor(R.color.red));
        CheckAlert.setTextColor(getResources().getColor(R.color.black));
        CheckAlert.show();

    }

    public void searchOldestMailPosition(){
        int oldestTmp=0;
        for (Message message : MessageList) {
            Flags flags = null;

            try {
                flags = message.getFlags();
                if (!flags.contains(Flags.Flag.SEEN)){
                    oldestMailPosition=oldestTmp;
                    System.out.println(oldestMailPosition);
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            oldestTmp++;
        }
    }

    public void setRealURL(String realURL) {
        this.realURL = realURL;
    }

    public void setMailURL(String mailURL) {
        this.mailURL = mailURL;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSenderMailAddress(String senderMailAddress) {
        this.senderMailAddress = senderMailAddress;
    }

}
