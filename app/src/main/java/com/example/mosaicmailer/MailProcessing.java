package com.example.mosaicmailer;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import androidx.annotation.NonNull;

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
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

public class MailProcessing extends Application {
    //操作
    Session session=null;
    Store store = null;
    Folder inbox = null;

    //MessageList関連
    List<Message> MessageList = new ArrayList<Message>();
    int openMessageListPosition=0;
    int oldestMailPosition=0;

    //SearchResultList関連
    List<Message> SearchResultList = new ArrayList<Message>();
    int openSearchResultListPosition=0;
    String presentSearchWord="";

    //ダイアログ関連
    boolean SearchHeadUpFlag=false;//注意喚起メールを探したかどうかのフラグ
    boolean showSearchHeadUpAlertFlag=false;//SearchHeadUpAlertアラートが出現しているかどうか
    Snackbar SearchHeadUpAlert = null;
    Snackbar CheckAlert = null;
    Snackbar ReportAlert = null;
    Snackbar DeleteAlert = null;
    String realURL = "";
    String mailURL = "";
    String senderName = "";
    String senderMailAddress = "";

    //確認した位置
    int linkInfoIndex = 0;

    //注意喚起メール関連
    boolean existAlert = false; //注意喚起メールが来ているかどうかフラグ
    List<Message> AlertList = new ArrayList<Message>();

    //削除関連
    boolean phishingFlag = false;


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

            store = session.getStore("imap");
            store.connect("outlook.office365.com", 993, username, password);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
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

    public void setOpenMessageListPosition(int position) {// メール一覧の開くメールの位置番号をsetする
        this.openMessageListPosition = position;
    }

    public void setOpenSearchResultListPosition(int position) {// 検索結果一覧の開くメールの位置番号をsetする
        this.openSearchResultListPosition = position;
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
        CheckAlert = Snackbar.make(v.findViewById(R.id.body), "フィッシングメールかもしれません\nメールアドレスとURLを確認してください", Snackbar.LENGTH_INDEFINITE);
        CheckAlert.setBackgroundTint(getResources().getColor(R.color.red));
        CheckAlert.setTextColor(getResources().getColor(R.color.black));
        CheckAlert.show();

    }

    public void ReportAlert(View v) {
        ReportAlert = Snackbar.make(v.findViewById(R.id.myCoordinatorLayout), "フィッシングメールの報告をしてください", Snackbar.LENGTH_INDEFINITE);
        ReportAlert.setBackgroundTint(getResources().getColor(R.color.red));
        ReportAlert.setTextColor(getResources().getColor(R.color.black));
        ReportAlert.setAction("しました",  view -> DeleteAlert(v));
        ReportAlert.show();
    }

    public void DeleteAlert(View v) {
        DeleteAlert = Snackbar.make(v.findViewById(R.id.body), "このフィッシングメールを削除してください", Snackbar.LENGTH_INDEFINITE);
        DeleteAlert.setBackgroundTint(getResources().getColor(R.color.red));
        DeleteAlert.setTextColor(getResources().getColor(R.color.black));
        DeleteAlert.show();
    }

    public void searchOldestMailPosition(){
        int oldestTmp=0;
        for (Message message : MessageList) {
            Flags flags = null;

            try {
                if(message == null){System.out.println("message==null");}
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

    public List<Message> searchMessages(String s) {
        presentSearchWord=s;
        try {
            //検索条件の設定
            SearchTerm[] terms = {
                    new SubjectTerm(s),
                    new FromStringTerm(s),
                    new BodyTerm(s)
            };

            SearchTerm term = new OrTerm(terms);
            SearchResultList = Arrays.asList(inbox.search(term));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        //新しい順になるように逆順に並び替える
        Collections.reverse(SearchResultList);

        return SearchResultList;
    }

    public void deleteMessage(Message msg) {
        Folder deleted = null;
        try {
            //deleted = store.getFolder("削除済みアイテム");
            msg.setFlag(Flags.Flag.DELETED, true);
            //deleted.appendMessages(new Message[]{msg});
            inbox.close(true);
            inbox.open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void reloadMessageList(@NonNull String ListType){
        if(ListType.equals("MailList")){
            getMailListAll();
        }else if(ListType.equals("Search")){
            searchMessages(presentSearchWord);
        }
    }

    public boolean existSender() {

        InternetAddress addrFrom;
        int senderCount=0;
        String senderTmp;
        String senderAddressTmp;
        for(Message m : MessageList){
            try {
                addrFrom = (InternetAddress) m.getFrom()[0];
                senderTmp = addrFrom.getPersonal();
                if(senderTmp==null){
                    senderAddressTmp = addrFrom.getAddress();
                    if(senderAddressTmp.equals(senderMailAddress)){
                        senderCount++;
                        if(senderCount>=2){
                            return true;
                        }
                    }
                }else if(senderTmp.equals(senderName)){
                    senderCount++;
                    if(senderCount>=2){
                        return true;
                    }
                }

            } catch (MessagingException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public boolean existNameandAddress() {
        InternetAddress addrFrom;
        int senderCount=0;
        String senderNameTmp;
        String senderAddressTmp;
        for(Message m : MessageList){
            try {
                addrFrom = (InternetAddress) m.getFrom()[0];
                senderNameTmp = addrFrom.getPersonal();
                senderAddressTmp = addrFrom.getAddress();
                if(senderNameTmp==null){
                    if(senderAddressTmp.equals(senderMailAddress)){
                        senderCount++;
                        if(senderCount>=2){
                            return true;
                        }
                    }
                }else if(senderNameTmp.equals(senderName) && senderAddressTmp.equals(senderMailAddress)){
                    senderCount++;
                    if(senderCount>=2){
                        return true;
                    }
                }

            } catch (MessagingException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public void searchAlert() {//注意喚起メールを探す
        MosaicMailerDatabaseHelper helper = null;
        //注意喚起メール条件情報をDBから探す．
        helper = new MosaicMailerDatabaseHelper(this);
        String[] cols = {"_id", "mailaddress", "keyword"};
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cs = db.query("HeadsUpInfo", cols, null, null, null, null, null, null);
            if (cs.getCount()>0) {
                cs.moveToFirst();
                SearchTerm[] terms = {
                        new FromStringTerm(cs.getString(1)),
                        new SubjectTerm(cs.getString(2)),
                        new FlagTerm(new Flags(Flags.Flag.SEEN), false)
                };
                SearchTerm SearchTerm = new AndTerm(terms);
                AlertList = new ArrayList<Message>(Arrays.asList(inbox.search(SearchTerm)));

                if(AlertList.size()>0){
                    existAlert = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAlertMessege(int ps) {
        try {
            Message msg = MessageList.get(ps);
            String[] idMsgs = msg.getHeader("Message-ID");
            String idMsg = idMsgs[0];
            for(Message alert : AlertList){
                String[] idAlerts = alert.getHeader("Message-ID");
                String idAlert = idAlerts[0];
                if(idMsg.equals(idAlert)){return true;}
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setLinkInfoIndex(int index){
        linkInfoIndex = index;
    }

    public void dropAlert(int ps) {//dropついでにexist変更
        try {
            Message msg = MessageList.get(ps);
            String[] idMsgs = msg.getHeader("Message-ID");
            String idMsg = idMsgs[0];
            for(int i=0; i<AlertList.size(); i++){
                String[] idAlerts = AlertList.get(i).getHeader("Message-ID");
                String idAlert = idAlerts[0];
                if(idMsg.equals(idAlert)){
                    AlertList.remove(i);
                    if(AlertList.size()==0){
                        existAlert = false;
                    }
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
