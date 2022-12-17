package com.example.mosaicmailer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

public class BrowseActivity extends AppCompatActivity implements View.OnLongClickListener{
    MailProcessing mp;
    ArrayList<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();
    int countCheckedLink = 0;//チェックしたリンク数
    int countAllLink = 0;//リンクの総数
    boolean MosaicMode = true;
    boolean seen = false;
    boolean checkedMailAddress = false;

    Message msg;
    String ListType;
    WebView body;
    String originalHTML;
    String originalPlanText;
    ArrayList<BodyPart> ImgPartList = new ArrayList<>();

    final String WINDOW = "mail_browse_window";

    static BrowseActivity instance = new BrowseActivity();

    class LinkInfo{
        String linkText;
        String href;
        int countSharp;
        boolean check = false;
    }

    class TagInfo {
        int start;
        int end;
        boolean tagA;
        boolean tagSlashA;
        boolean startTag;
        boolean leafStartTag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_activity);//xmlを読み込む
        mp = (MailProcessing) this.getApplication();

        //開いた画面のログの書き出し
        mp.writeLog(WINDOW,"open " + WINDOW);

        //確認したリンクのカウントのリセット
        countCheckedLink = 0;

        //戻れない問題の対策コード
        mp.phishingFlag = false;

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //webViewの準備
        body = findViewById(R.id.body);
        body.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //URLをタップしたことを表すログを書き出す
                mp.writeLog(WINDOW,"tap URL");
                if(MosaicMode){
                    //System.out.println("[MosaicModeOn]tap "+url);
                    mp.showLinkTapAlert(findViewById(R.id.bottomLinearLayout));
                    view.stopLoading();
                    return false;
                }else{
                    // trueを返すことで、WebView内で開かないようにさせる
                    // 今回はデフォルトのウェブブラウザでリンク先を開く
                    //System.out.println("tap "+url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            }
        });
        //body.getSettings().setLoadWithOverviewMode(true);
        //body.getSettings().setUseWideViewPort(true);
        body.getSettings().setBuiltInZoomControls(true);
        body.getSettings().setDisplayZoomControls(false);
        body.setLongClickable(true);
        body.setOnLongClickListener(this);

        //データを受け取る
        ListType = getIntent().getStringExtra("ListType");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if(ListType.equals("MailList")){
                    msg = mp.MessageList.get(mp.openMessageListPosition);
                }else{
                    msg = mp.SearchResultList.get(mp.openSearchResultListPosition);
                }

                //現在のメールをセットする
                mp.setCurrentMessage(msg);

                //このメールが既読か未読か調べる
                seen = msg.getFlags().contains(Flags.Flag.SEEN);
                if(seen){
                    MosaicMode = false;
                }else{
                    MosaicMode = true;
                }

                //メールの件名を取得
                String subject =  msg.getSubject();

                //メールの差出人名を取得
                final InternetAddress addrFrom = (InternetAddress) msg.getFrom()[0];
                String sender = addrFrom.getPersonal();
                mp.setSenderName(sender);

                //メールの受信者の取得
                final Address[] toArray = msg.getRecipients(Message.RecipientType.TO);
                StringBuilder toAddress = new StringBuilder();
                for(Address toTmp : toArray){
                    toAddress.append(toTmp.toString());
                }

                //差出人のメールアドレス取得
                mp.setSenderMailAddress(addrFrom.getAddress());

                //メールの本文中のテキストをモザイク化しセッティング
                String mosaicMailStr = Mosaic();

                //メール内のリンク一覧数の取得
                countAllLink = linkInfoList.size();

                //開いたメールのURL総数を表すログを書き出す
                mp.writeLog(WINDOW,"all URL number of this mail is " + countAllLink);

                if(MosaicMode){
                    //未読にする(モザイク状態のメールを開いた状態でメーラを終わると，未読になる不具合対策)
                    msg.setFlag(Flags.Flag.SEEN, false);
                }

                //処理結果をhandler経由でUIに反映
                HandlerCompat.createAsync(getMainLooper()).post(() ->{
                    ((TextView) findViewById(R.id.title)).setText(subject);
                    if(sender == null || sender.equals("")){
                        ((TextView) findViewById(R.id.sender)).setText(mp.senderMailAddress);
                    }else{
                        ((TextView) findViewById(R.id.sender)).setText(sender);
                    }
                    ((TextView) findViewById(R.id.receiver)).setText("To:"+toAddress.toString());
                    if(mp.habitFunction){//習慣化機能on
                        if(MosaicMode){
                            //URLとメールアドレスを確認しフィッシングメールかどうか判定するフェーズが始まったことを表すログの書き出し
                            mp.phaseConfirmMail = true;
                            mp.writeLog(WINDOW,"start confirmation mailAddress&URL");

                            body.loadDataWithBaseURL(null, mosaicMailStr, "text/html", "utf-8", null);
                            //ナビゲーション表示
                            mp.showCheckAlert(getWindow().getDecorView());
                        }else{
                            body.loadDataWithBaseURL(null, originalHTML, "text/html", "utf-8", null);
                        }
                    }else{//習慣化機能off
                        body.loadDataWithBaseURL(null, originalHTML, "text/html", "utf-8", null);
                    }

                    //System.out.println(mosaicMailStr);
                    /*for(LinkInfo l : linkInfoList){
                        System.out.println("linkText="+ l.linkText + "  /  href=" + l.href );
                    }*/
                    //body.setMovementMethod(new ScrollingMovementMethod());
                    //((TextView) findViewById(R.id.body)).setText(ss);
                    //((TextView) findViewById(R.id.body)).setMovementMethod(LinkMovementMethod.getInstance());
                });
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });

    }

    // メニューをActivity上に設置する
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 参照するリソースは上でリソースファイルに付けた名前と同じもの
        getMenuInflater().inflate(R.menu.activity_mail_browse_menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // ツールバーのメニューが選択されたときの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Executors.newSingleThreadExecutor().execute(() -> {
                    //学習者が削除したことを表すログを書き出す
                    mp.writeLog(WINDOW,"delete mail");

                    mp.deleteMessage(msg);
                    //mp.reloadMessageList(ListType);
                    countDownLatch.countDown();
                });

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mp.phishingFlag = false;

                if(mp.phaseReportAndRemove == true){
                    //フィッシングメールと判断したメールを報告し削除するフェーズが終わったことを表すログを書き出す
                    mp.phaseReportAndRemove = false;
                    mp.writeLog(WINDOW,"end reporting & removing");
                }

                if(mp.phaseConfirmMail == true){
                    //URLとメールアドレスを確認しフィッシングメールかどうか判定するフェーズが終わったことを表すログの書き出す
                    mp.phaseConfirmMail = false;
                    mp.writeLog(WINDOW,"end confirmation mailAddress&URL");
                }

                finish();
                return true;
            case R.id.reply:
                reply();
                return true;
            case R.id.forward:
                forward();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if(MosaicMode){
                    msg.setFlag(Flags.Flag.SEEN, false);
                }else{
                    msg.setFlag(Flags.Flag.SEEN, true);
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                if(mp.habitFunction == false){
                    //戻るボタンを押したことを表すログを書き出す
                    mp.writeLog(WINDOW,"back from browse mail");

                    if(mp.phaseReportAndRemove == true){
                        //フィッシングメールと判断したメールを報告し削除するフェーズが終わったことを表すログを書き出す
                        mp.phaseReportAndRemove = false;
                        mp.writeLog(WINDOW,"end reporting & removing");
                    }

                    if(mp.phaseConfirmMail == true) {
                        //URLとメールアドレスを確認しフィッシングメールかどうか判定するフェーズが終わったことを表すログの書き出し
                        mp.phaseConfirmMail = false;
                        mp.writeLog(WINDOW, "end confirmation mailAddress&URL");
                    }

                    finish();
                }
                else if(!mp.phishingFlag){
                    //戻るボタンを押したことを表すログを書き出す
                    mp.writeLog(WINDOW,"back from browse mail");

                    if(mp.phaseReportAndRemove == true){
                        //フィッシングメールと判断したメールを報告し削除するフェーズが終わったことを表すログを書き出す
                        mp.phaseReportAndRemove = false;
                        mp.writeLog(WINDOW,"end reporting & removing");
                    }

                    if(mp.phaseConfirmMail == true) {
                        //URLとメールアドレスを確認しフィッシングメールかどうか判定するフェーズが終わったことを表すログの書き出し
                        mp.phaseConfirmMail = false;
                        mp.writeLog(WINDOW, "end confirmation mailAddress&URL");
                    }

                    finish();
                }
            });
        });
        return super.onSupportNavigateUp();
    }

    //端末の戻るボタンで戻る
    @Override
    public void onBackPressed() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if(MosaicMode){
                    msg.setFlag(Flags.Flag.SEEN, false);
                }else{
                    msg.setFlag(Flags.Flag.SEEN, true);
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                if(mp.habitFunction == false){
                    //戻るボタンを押したことを表すログを書き出す
                    mp.writeLog(WINDOW,"back from browse mail");

                    if(mp.phaseReportAndRemove == true){
                        //フィッシングメールと判断したメールを報告し削除するフェーズが終わったことを表すログを書き出す
                        mp.phaseReportAndRemove = false;
                        mp.writeLog(WINDOW,"end reporting & removing");
                    }

                    if(mp.phaseConfirmMail == true) {
                        //URLとメールアドレスを確認しフィッシングメールかどうか判定するフェーズが終わったことを表すログの書き出し
                        mp.phaseConfirmMail = false;
                        mp.writeLog(WINDOW, "end confirmation mailAddress&URL");
                    }

                    finish();
                }
                else if(!mp.phishingFlag){
                    //戻るボタンを押したことを表すログを書き出す
                    mp.writeLog(WINDOW,"back from browse mail");

                    if(mp.phaseReportAndRemove == true){
                        //フィッシングメールと判断したメールを報告し削除するフェーズが終わったことを表すログを書き出す
                        mp.phaseReportAndRemove = false;
                        mp.writeLog(WINDOW,"end reporting & removing");
                    }

                    if(mp.phaseConfirmMail == true) {
                        //URLとメールアドレスを確認しフィッシングメールかどうか判定するフェーズが終わったことを表すログの書き出し
                        mp.phaseConfirmMail = false;
                        mp.writeLog(WINDOW, "end confirmation mailAddress&URL");
                    }

                    finish();
                }
            });
        });
    }

    @Override
    public boolean onLongClick(View v) {
        if(v == body){
            //長押しした箇所の情報を取得
            WebView.HitTestResult hittestresult = body.getHitTestResult();
            if( hittestresult.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE){
                String url = hittestresult.getExtra();
                int linkInfoIndex = 0;
                for(LinkInfo linkTmp : linkInfoList){
                    //System.out.println(url);
                    //System.out.println(linkTmp.href);
                    if(url.equals(linkTmp.href)){
                        url = url.substring(0, url.length()-linkTmp.countSharp);
                        mp.setMailURL(linkTmp.linkText);
                        mp.setRealURL(url);
                        //System.out.println(linkTmp.linkText);
                        mp.setLinkInfoIndex(linkInfoIndex);

                        //URLの確認画面を開いたことを表すログを書き出す
                        mp.writeLog(WINDOW,"open confirmation URL window");

                        if(linkTmp.check == true){
                            //確認済みのURLの確認画面を開いたことを表すログを書き出す
                            mp.writeLog(WINDOW,"open checked confirmation URL window");
                        }
                        if(MosaicMode == true){
                            DialogFragment compare_dialog = new BrowseQuestionURLCompareDialog();
                            compare_dialog.show(getSupportFragmentManager(), "url_compare_question_dialog");
                            break;
                        }else{
                            DialogFragment url_dialog = new BrowseNormalURLDialog();
                            url_dialog.show(getSupportFragmentManager(), "url_normal_dialog");
                            break;
                        }
                    }
                    linkInfoIndex++;
                }
            }
        }
        return false;
    }

    //返信
    public void reply(View view){
        Intent intent = new Intent(getApplication(), CreateActivity.class);
        intent.putExtra("createType", "reply");
        //System.out.println("CreateActivity:originalPlanText=" + originalPlanText);
        intent.putExtra("replyTextMessage", originalPlanText);
        startActivity(intent);
    }

    //ツールバーからの返信
    public void reply(){
        Intent intent = new Intent(getApplication(), CreateActivity.class);
        intent.putExtra("createType", "reply");
        intent.putExtra("replyTextMessage", originalPlanText);
        startActivity(intent);
    }

    //ツールバーからの転送
    public void forward(){
        Intent intent = new Intent(getApplication(), CreateActivity.class);
        intent.putExtra("createType", "forward");
        intent.putExtra("replyTextMessage", originalPlanText);
        //mp.setCurrentMessage(msg);
        startActivity(intent);
    }

    //メールアドレス確認ポップアップの表示
    public void QuestionDialog(View view) {
        //メールアドレスの確認画面を開いたことを表すログを書き出す
        mp.writeLog(WINDOW,"open confirmation mailAddress window");

        if(MosaicMode == true){
            DialogFragment name_dialog = new BrowseQuestionFromNameDialog();
            name_dialog.show(getSupportFragmentManager(), "name_question_dialog");
        }else{
            DialogFragment mailAddress_dialog = new BrowseNormalAddressDialog();
            mailAddress_dialog.show(getSupportFragmentManager(), "address_normal_dialog");
        }
    }

    // インスタンス取得メソッド
    public static BrowseActivity getInstance() {
        return instance;
    }

    public String Mosaic(){
        try {
            Object mailContent = msg.getContent();
            if (mailContent instanceof String) {//"text/plain" コンテンツに対して返されるオブジェクトは、通常、String オブジェクト
                //　text/planのメールの場合
                originalPlanText = mailContent.toString();
                String html = xformHTML(originalPlanText);//html形式に直す
                originalHTML = html;
                return insertMosaicToHTML(html);//モザイク処理
            } else if (mailContent instanceof Multipart) {
                //　Multipart形式のメールの場合
                Multipart multiContent = (Multipart) mailContent;
                originalPlanText = extractPlaininMlt(multiContent);//　text/plainの抽出
                //System.out.println("=====originalPlanText=" + originalPlanText + "(BrowseActivity)=====");
                String html = extractHTMLinMlt(multiContent);//　text/htmlの抽出
                extractImginMlt(multiContent);//imageを抽出し，ImgPartListに追加
                if(html == null){
                    html = xformHTML(originalPlanText);//html形式に直す
                }
                html = setImageInHTML(html);
                originalHTML = html;
                return insertMosaicToHTML(html);//モザイク処理

            } else if (mailContent instanceof InputStream) {
                //よくわからん
            }

        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String xformHTML(String plainText) {

        //plantextに対するURL探索用
        Pattern StdUrlPtrn = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
        Matcher StdUrlMtch = StdUrlPtrn.matcher(plainText);

        //編集用のHTML
        StringBuilder editHtml = new StringBuilder("");

        //エスケープ処理(URLは避ける)
        int endIndex = 0;
        int starIndex = 0;
        while (StdUrlMtch.find()){
            starIndex = StdUrlMtch.start();
            editHtml.append(escapeStr(plainText.substring( endIndex, starIndex )));
            endIndex = StdUrlMtch.end();
            editHtml.append(plainText.substring( starIndex, endIndex ));
        }
        editHtml.append(plainText.substring(endIndex));

        //aタグ付与
        ////editHtmlStrに対するURL探索用
        String editHtmlStr = editHtml.toString();
        Pattern MoUrlPtrn = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
        Matcher MoUrlMtch = MoUrlPtrn.matcher(editHtmlStr);
        ////ズレ
        int diff = 0;
        while (MoUrlMtch.find()){
            String aTag = "<a href=\"" + MoUrlMtch.group() + "\">";
            String aTagSlash = "</a>";

            //開始タグの挿入
            editHtml.insert(MoUrlMtch.start() + diff, aTag);
            diff += aTag.length();

            //終了タグの挿入
            editHtml.insert(MoUrlMtch.end() + diff, aTagSlash);
            diff += aTagSlash.length();
        }

        //\nを<br>に変換
        ////aタグ付与後のHTML文字列
        String aHtmlStr = editHtml.toString();
        aHtmlStr = aHtmlStr.replaceAll("\n", "<br>"); //メールとかに\nとか入れてたら，<br>に変換される．後で直す必要あり

        //HTMLテンプレートに挿入
        String textHtml = "<html><head></head><body>" + aHtmlStr + "</body></html>";

        return textHtml;
    }

    private String escapeStr(String str) {
        String escapeStr = str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("©", "&copy;")
                .replace("®", "&reg;")
                .replace("™", "&trade;")
                .replace(" ", "&nbsp;")
                .replace("+", "&plus;")
                .replace("−", "&minus;")
                .replace("×", "&times;")
                .replace("÷", "&divide;")
                .replace("=", "&equals;");
        return escapeStr;
    }

    private String setImageInHTML(String html) {

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(html);

        //imageタグを正規表現で探す準備
        String imageTagRegex = "src\\s*=\\s*([\"'])(.*?)\\1";
        Pattern imageTagPtrn = Pattern.compile(imageTagRegex, Pattern.CASE_INSENSITIVE);
        Matcher imageTagMtch = imageTagPtrn.matcher(html);

        int diff = 0;
        while (imageTagMtch.find()){
            String src = imageTagMtch.group(2);
            if(src.startsWith("cid:")){
                src = src.substring(4);
                imageTagMtch.start();
                for(BodyPart img : ImgPartList){
                    try {
                        if(img.getHeader("Content-ID")[0].contains(src)){
                            String[] fileName = img.getFileName().split("\\.");
                            //System.out.println("---img.getFileName():" + img.getFileName() + "(BrowseActivity)---");

                            //画像のバイト列取得
                            ByteArrayOutputStream byOutStr = new ByteArrayOutputStream();
                            img.writeTo(byOutStr);
                            String img_content = byOutStr.toString("utf-8");
                            int content_start = img_content.indexOf("Content-Transfer-Encoding")+"Content-Transfer-Encoding".length();
                            content_start = img_content.indexOf("base64",content_start)+"base64".length();
                            img_content = img_content.substring(content_start);
                            img_content = img_content.replaceAll("\r\n|\r|\n","");
                            String imgData = "data:image/" + fileName[fileName.length-1] + ";"
                                    + img.getHeader("Content-transfer-encoding")[0] + "," + img_content;
                            htmlBuilder.delete(imageTagMtch.start(2) + diff, imageTagMtch.end(2) + diff);
                            htmlBuilder.insert(imageTagMtch.start(2) + diff, imgData);
                            diff = imageTagMtch.start(2) - imageTagMtch.end(2);
                            diff += imgData.length();
                            break;
                        }
                    } catch (MessagingException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return htmlBuilder.toString();
    }

    public String insertMosaicToHTML(String html) {

        //tagを正規表現で探す準備
        String tagRegex = "<(\".*?\"|'.*?'|[^'\"])*?>";
        Pattern tagPtrn = Pattern.compile(tagRegex, Pattern.CASE_INSENSITIVE);
        Matcher tagMtch = tagPtrn.matcher(html);

        //タグ情報リスト
        ArrayList<TagInfo> tagInfoList = new ArrayList<>();

        //タグ情報リスト内の</header>タグの情報が入ったindex
        int styleInsertIndex = 0;

        while (tagMtch.find()){
            TagInfo t = new TagInfo();
            t.start = tagMtch.start();
            t.end = tagMtch.end();
            t.tagA = tagMtch.group().startsWith("<a");
            t.tagSlashA = tagMtch.group().startsWith("</a");
            t.startTag = !tagMtch.group().startsWith("</");
            int sizeTagInfoList = tagInfoList.size();
            if(sizeTagInfoList>=1){
                if(tagInfoList.get(sizeTagInfoList-1).startTag && !t.startTag){
                    t.leafStartTag = false;
                    tagInfoList.get(sizeTagInfoList-1).leafStartTag = true;
                }
            }else{
                t.leafStartTag = false;
            }
            tagInfoList.add(t);
            if(tagMtch.group().startsWith("</head") && styleInsertIndex==0){
                styleInsertIndex = tagInfoList.size()-1;
            }
        }

        //モザイク化用のStringBuilder
        StringBuilder mosaicHtml = new StringBuilder();
        mosaicHtml.append(html);
        //ズレ
        int diff = 0;
        //bodyが見つかったかフラグ
        boolean bodyFlag = false;
        //aタグが見つかったかフラグ
        boolean aFlag = false;
        //mosaic開始タグ
        String startMosaic = "<div id=\"Mosaic\">";
        int startMosaicLen = startMosaic.length();
        //mosaic終了タグ
        String endMosaic = "</div>";
        int endMosaicLen = endMosaic.length();
        //モザイク化用のstyleタグ
        String style = "<style>\n" +
                "    #Mosaic{filter: blur(7px);}\n" +
                "</style>";
        int styleLen = style.length();


        for(int i=styleInsertIndex; i<tagInfoList.size(); i++){
            tagMtch.find(tagInfoList.get(i).start);
            String group = tagMtch.group();
            if(group.startsWith("</head")){
                mosaicHtml.insert(tagInfoList.get(i).start + diff, style);
                diff += styleLen;
            }
            else if(group.startsWith("<body")){
                mosaicHtml.insert(tagInfoList.get(i).end + diff, startMosaic);
                diff += startMosaicLen;
                bodyFlag = true;
            }
            else if(group.startsWith("</a")){
                aFlag = false;
                mosaicHtml.insert(tagInfoList.get(i).end + diff, startMosaic);
                diff += startMosaicLen;
            }
            else if(group.startsWith("</body")){
                mosaicHtml.insert(tagInfoList.get(i).start + diff, endMosaic);
                diff += endMosaicLen;
                break;
            }
            else if(group.startsWith("<a")){
                aFlag = true;
                mosaicHtml.insert(tagInfoList.get(i).start + diff, endMosaic);
                diff += endMosaicLen;

                //リンクテキスト取得
                LinkInfo anchor = new LinkInfo();
                for(int j=i; j<tagInfoList.size(); j++){
                    if(tagInfoList.get(j).leafStartTag){
                        anchor.linkText = mosaicHtml.substring(tagInfoList.get(j).end + diff, tagInfoList.get(j+1).start + diff);
                        break;
                    }
                }
                //anchor.linkText = mosaicHtml.substring(tagInfoList.get(i).end + diff, tagInfoList.get(i+1).start + diff);
                //System.out.println(anchor.linkText);

                //URL取得
                Pattern StdUrlPtrn = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
                Matcher StdUrlMtch = StdUrlPtrn.matcher(group);
                //System.out.println(group);

                if(StdUrlMtch.find(0)){
                    anchor.href = StdUrlMtch.group();

                    //URLがユニークかどうかのフラグ
                    boolean uniqueHref = false;
                    //追加する#の数
                    int hashLen = 0;

                    while (!uniqueHref){
                        uniqueHref = true;
                        for(LinkInfo linkTmp : linkInfoList){
                            if(anchor.href.equals(linkTmp.href)){
                                anchor.href = anchor.href + "#";
                                hashLen++;
                                uniqueHref = false;
                                break;
                            }
                        }
                    }

                    //#の追加
                    mosaicHtml.insert(tagInfoList.get(i).start + diff + StdUrlMtch.end(), StringUtils.repeat("#", hashLen));
                    diff += hashLen;
                    anchor.countSharp = hashLen;

                    //linkInfoListに追加
                    linkInfoList.add(anchor);
                }


            }
            else if(group.startsWith("<img")){
                /*
                String imageTagRegex = "src\\s*=\\s*([\"'])(.*?)\\1";
                Pattern imageTagPtrn = Pattern.compile(imageTagRegex, Pattern.CASE_INSENSITIVE);
                Matcher imageTagMtch = imageTagPtrn.matcher(mosaicHtml.substring(tagInfoList.get(i).start + diff, tagInfoList.get(i).end + diff));
                if(imageTagMtch.find()){
                    String src = imageTagMtch.group(2);
                    if(src.startsWith("cid:")){
                        src = src.substring(4);
                        imageTagMtch.start();
                        for(BodyPart img : ImgPartList){
                            try {
                                if(img.getHeader("Content-ID")[0].contains(src)){
                                    String[] fileName = img.getFileName().split("\\.");
                                    System.out.println("---img.getFileName():" + img.getFileName() + "(BrowseActivity)---");

                                    //画像のバイト列取得
                                    ByteArrayOutputStream byOutStr = new ByteArrayOutputStream();
                                    img.writeTo(byOutStr);
                                    String img_content = byOutStr.toString("utf-8");
                                    int content_start = img_content.indexOf("Content-Transfer-Encoding")+"Content-Transfer-Encoding".length();
                                    content_start = img_content.indexOf("base64",content_start)+"base64".length();
                                    img_content = img_content.substring(content_start);
                                    img_content = img_content.replaceAll("\r\n|\r|\n","");
                                    String imgData = "data:image/" + fileName[fileName.length-1] + ";"
                                            + img.getHeader("Content-transfer-encoding")[0] + "," + img_content;
                                    mosaicHtml.delete(tagInfoList.get(i).start + diff +imageTagMtch.start(2), tagInfoList.get(i).start + diff +imageTagMtch.end(2));
                                    mosaicHtml.insert(tagInfoList.get(i).start + diff +imageTagMtch.start(2), imgData);
                                    diff += imageTagMtch.start(2) - imageTagMtch.end(2);
                                    diff += imgData.length();
                                    break;
                                }
                            } catch (MessagingException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                */
                continue;
            }
            else if(group.startsWith("<br")){
                continue;
            }
            else if(bodyFlag && !aFlag){
                mosaicHtml.insert(tagInfoList.get(i).start + diff, endMosaic);
                diff += endMosaicLen;
                mosaicHtml.insert(tagInfoList.get(i).end + diff, startMosaic);
                diff += startMosaicLen;
            }
        }
        return mosaicHtml.toString();
    }

    private String extractPlaininMlt(Multipart mltPart) {
        try {
            int count = mltPart.getCount();
            String plain;
            for(int i=0; i<count; i++){

                BodyPart bodypart = mltPart.getBodyPart(i);
                String bodyContentType = bodypart.getContentType();

                //System.out.println("====bodyContentType=" + bodyContentType + "(BrowseActivity)====");
                if(bodyContentType.contains("multipart")){
                    Multipart multiContent = (Multipart) bodypart.getContent();
                    plain = extractPlaininMlt(multiContent);
                    if(plain != null){return plain;}
                } else if(bodyContentType.contains("text/plain")){
                    return bodypart.getContent().toString();
                }
            }
        } catch (MessagingException | IOException e) {e.printStackTrace();}
        return null;
    }

    public String extractHTMLinMlt(Multipart mltPart) {
        try {
            int count = mltPart.getCount();
            String html;
            for(int i=0; i<count; i++){

                BodyPart bodypart = mltPart.getBodyPart(i);
                String bodyContentType = bodypart.getContentType();

                if(bodyContentType.contains("multipart")){
                    Multipart multiContent = (Multipart) bodypart.getContent();
                    html = extractHTMLinMlt(multiContent);
                    if(html != null){return html;}
                } else if(bodyContentType.contains("text/html")){
                    return bodypart.getContent().toString();
                }
            }
        } catch (MessagingException | IOException e) {e.printStackTrace();}
        return null;
    }
    public void extractImginMlt(Multipart mltPart) {
        try {
            int count = mltPart.getCount();
            for(int i=0; i<count; i++){

                BodyPart bodypart = mltPart.getBodyPart(i);
                String bodyContentType = bodypart.getContentType();

                if(bodyContentType.contains("multipart")){
                    Multipart multiContent = (Multipart) bodypart.getContent();
                    extractImginMlt(multiContent);
                } else if(bodyContentType.contains("image")){
                    ImgPartList.add(bodypart);
                }
            }
        } catch (MessagingException | IOException e) {e.printStackTrace();}
    }

    public void setChecked(int index){
        LinkInfo tmp = linkInfoList.get(index);
        if(tmp.check == false){
            countCheckedLink++;
            //確認したURL数とメール内のURL総数を表すログを書き出す
            mp.writeLog(WINDOW,"confirm URL: "+countCheckedLink+"/"+countAllLink);
        }else{
            //確認したURL数とメール内のURL総数を表すログを書き出す
            mp.writeLog(WINDOW,"confirm URL: "+countCheckedLink+"/"+countAllLink);
            //確認済みのURLの確認画面を開き最後まで確認したことを表すログを書き出す
            mp.writeLog(WINDOW,"confirm checked  URL window");
        }
        tmp.check = true;
        linkInfoList.set(index,tmp);
    }

    public boolean checkedAll(){
        if(!checkedMailAddress){return false;}
        for(LinkInfo linkTmp : linkInfoList){
            if(!linkTmp.check){return false;}
        }
        //すべてのメールアドレスとURLを確認したことを表すログを書き出す
        mp.writeLog(WINDOW,"confirm all mailAddress & URL");
        return true;
    }

    public void removeMosaic() {
        body.loadDataWithBaseURL(null, originalHTML, "text/html", "utf-8", null);
        MosaicMode = false;

        if(mp.phaseConfirmMail == true) {
            //URLとメールアドレスを確認しフィッシングメールかどうか判定するフェーズが終わったことを表すログの書き出し
            mp.phaseConfirmMail = false;
            mp.writeLog(WINDOW, "end confirmation mailAddress&URL");
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            mp.dropAlert(ListType);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}