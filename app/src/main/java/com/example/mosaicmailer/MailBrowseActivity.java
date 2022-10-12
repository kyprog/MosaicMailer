package com.example.mosaicmailer;

import static java.lang.System.in;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

public class MailBrowseActivity extends AppCompatActivity implements View.OnLongClickListener{
    MailProcessing mp;
    ArrayList<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();
    boolean MosaicMode = true;
    boolean seen = false;
    boolean checkedMailAddress = false;

    Message msg;
    String ListType;
    WebView body;
    String originalHTML;
    String originalPlanText;
    static MailBrowseActivity instance = new MailBrowseActivity();

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
        setContentView(R.layout.activity_mail_browse);//xmlを読み込む
        mp = (MailProcessing) this.getApplication();

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ナビゲーション表示
        mp.showCheckAlert(getWindow().getDecorView());

        //webViewの準備
        body = findViewById(R.id.body);
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

                //このメールが既読か未読か調べる
                seen = msg.getFlags().contains(Flags.Flag.SEEN);
                if(seen){MosaicMode = false;}else{MosaicMode = true;}

                //メールの件名を取得
                String subject =  msg.getSubject();

                //メールの差出人名を取得
                final InternetAddress addrFrom = (InternetAddress) msg.getFrom()[0];
                String sender = addrFrom.getPersonal();
                mp.setSenderName(sender);

                //差出人のメールアドレス取得
                mp.setSenderMailAddress(addrFrom.getAddress());

                //メールの本文中のテキストをモザイク化しセッティング
                String mosaicMailStr = Mosaic();

                //処理結果をhandler経由でUIに反映
                HandlerCompat.createAsync(getMainLooper()).post(() ->{
                    ((TextView) findViewById(R.id.title)).setText(subject);
                    ((TextView) findViewById(R.id.sender)).setText(sender);
                    ((TextView) findViewById(R.id.receiver)).setText("To: 自分");
                    if(MosaicMode){
                        body.loadDataWithBaseURL(null, mosaicMailStr, "text/html", "utf-8", null);
                    }else{
                        body.loadDataWithBaseURL(null, originalHTML, "text/html", "utf-8", null);
                    }

                    /*System.out.println(mosaicMailStr);
                    for(LinkInfo l : linkInfoList){
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
                    mp.deleteMessage(msg);
                    mp.reloadMessageList(ListType);
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mp.phishingFlag = false;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if(MosaicMode){
                try {
                    msg.setFlag(Flags.Flag.SEEN, false);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
            HandlerCompat.createAsync(getMainLooper()).post(() ->{
                if(!mp.phishingFlag){finish();}
            });
        });
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onLongClick(View v) {
        if(v == body){
            //長押しした箇所の情報を取得
            WebView.HitTestResult hittestresult = body.getHitTestResult();
            String url = hittestresult.getExtra();
            int linkInfoIndex = 0;
            for(LinkInfo linkTmp : linkInfoList){
                if(url.equals(linkTmp.href)){
                    url = url.substring(0, url.length()-linkTmp.countSharp);
                    mp.setMailURL(linkTmp.linkText);
                    mp.setRealURL(url);
                    //System.out.println(linkTmp.linkText);
                    mp.setLinkInfoIndex(linkInfoIndex);
                    DialogFragment compare_dialog = new URLCompareQuestionDialog();
                    compare_dialog.show(getSupportFragmentManager(), "url_compare_question_dialog");
                   break;
                }
                linkInfoIndex++;
            }
        }
        return false;
    }

    //メールアドレス確認ポップアップの表示
    public void QuestionDialog(View view) {
        DialogFragment name_dialog = new FromNameQuestionDialog();
        name_dialog.show(getSupportFragmentManager(), "name_question_dialog");
    }

    // インスタンス取得メソッド
    public static MailBrowseActivity getInstance() {
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
                String html = extractHTMLinMlt(multiContent);//　text/htmlの抽出
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
        int starIndex =0;
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
                StdUrlMtch.find(0);
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
            else if(group.startsWith("<img") || group.startsWith("<br")){
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

    public void setChecked(int index){
        LinkInfo tmp = linkInfoList.get(index);
        tmp.check = true;
        linkInfoList.set(index,tmp);
    }

    public boolean checkedAll(){
        if(!checkedMailAddress){return false;}
        for(LinkInfo linkTmp : linkInfoList){
            if(!linkTmp.check){return false;}
        }
        return true;
    }

    public void removeMosaic() {
        body.loadDataWithBaseURL(null, originalHTML, "text/html", "utf-8", null);
        MosaicMode = false;
        mp.dropAlert(mp.openMessageListPosition);
    }



}