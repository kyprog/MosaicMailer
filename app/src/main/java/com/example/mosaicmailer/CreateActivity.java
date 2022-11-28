package com.example.mosaicmailer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

public class CreateActivity  extends AppCompatActivity {

    private final static int CHOSE_FILE_CODE = 1002;// 識別用のコード
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    CreateAdapter mainAdapter;
    MailProcessing mp;
    TextView to,cc,bcc,subject,body;
    String createType;
    String replyTextMessage="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_activity);//xmlを読み込む
        mp = (MailProcessing) this.getApplication();
        
        //view
        to = findViewById(R.id.editTo);
        cc = findViewById(R.id.editCc);
        bcc = findViewById(R.id.editBcc);
        subject = findViewById(R.id.editSubject);
        body = findViewById(R.id.editBody);

        //ツールバー
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recyclerView
        recyclerView = findViewById(R.id.attachment_recycler_view);

        // RecyclerViewのレイアウトサイズを変更しない設定をONにする
        // パフォーマンス向上のための設定。
        recyclerView.setHasFixedSize(true);

        // RecyclerViewにlayoutManagerをセットする。
        // このlayoutManagerの種類によって「1列のリスト」なのか「２列のリスト」とかが選べる。
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mainAdapter = new CreateAdapter(getApplication(), recyclerView);
        recyclerView.setAdapter(mainAdapter);

        //パーミッションチェックで，許可されてなかったら，リクエストする
        if (ActivityCompat.checkSelfPermission(CreateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(CreateActivity.this, permissions, 1000);
            return;
        }

        //データを受け取る
        createType = getIntent().getStringExtra("createType");
        if(createType.equals("reply")){
            to.setText(mp.getCurrentTo());
            cc.setText(mp.getCurrentCc());
            bcc.setText(mp.getCurrentBcc());
            subject.setText("Re:" + mp.getCurrentSubject());
            body.append("\n\nOn " + mp.getCurrentSentDate() + ",<" + mp.getCurrentTo() + "> wrote:\n>");
            replyTextMessage = getIntent().getStringExtra("replyTextMessage");
            //System.out.println("CreateActivity:replyTextMessage=" + replyTextMessage);
            if(replyTextMessage==null){replyTextMessage="テキストメッセージを取得できませんでした";}
            replyTextMessage = replyTextMessage.replace("\n", "\n>");
            body.append(replyTextMessage);
        }
        else if(createType.equals("forward")){
            subject.setText("Fwd:" + mp.getCurrentSubject());
            body.append("\n\nOn " + mp.getCurrentSentDate() + ",<" + mp.getCurrentTo() + "> wrote:\n>");
            replyTextMessage = getIntent().getStringExtra("replyTextMessage");
            if(replyTextMessage==null){replyTextMessage="テキストメッセージを取得できませんでした";}
            replyTextMessage = replyTextMessage.replace("\n", "\n>");
            body.append(replyTextMessage);
        }


    }

    //戻るボタンで戻る
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    // メニューをActivity上に設置する
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 参照するリソースは上でリソースファイルに付けた名前と同じもの
        getMenuInflater().inflate(R.menu.activity_mail_create_menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // ツールバーのメニューが選択されたときの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.attach:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, CHOSE_FILE_CODE);
                return true;

            case R.id.send:
                String[] tos = to.getText().toString().replaceAll("\\s", "").split(",");
                for(String toTmp : tos){
                    if(!Patterns.EMAIL_ADDRESS.matcher(toTmp).matches()){
                        Snackbar emailSnackbar = Snackbar.make(body, "Toのメールアドレスを正しく入力してください", Snackbar.LENGTH_SHORT);
                        emailSnackbar.show();
                        return true;
                    }
                }
                String ccStr = cc.getText().toString().replaceAll("\\s", "");
                String[] ccs = ccStr.split(",");
                if(!ccStr.equals("")){
                    for(String ccTmp : ccs){
                        if(!Patterns.EMAIL_ADDRESS.matcher(ccTmp).matches()){
                            Snackbar emailSnackbar = Snackbar.make(body, "Ccのメールアドレスを正しく入力してください", Snackbar.LENGTH_SHORT);
                            emailSnackbar.show();
                            return true;
                        }
                    }
                }else{
                    ccs = null;
                }
                String bccStr = bcc.getText().toString().replaceAll("\\s", "");
                String[] bccs = bccStr.split(",");
                if(!bccStr.equals("")) {
                    for (String bccTmp : bccs) {
                        if (!Patterns.EMAIL_ADDRESS.matcher(bccTmp).matches()) {
                            Snackbar emailSnackbar = Snackbar.make(body, "Bccのメールアドレスを正しく入力してください", Snackbar.LENGTH_SHORT);
                            emailSnackbar.show();
                            return true;
                        }
                    }
                }else{
                    bccs = null;
                }
                //System.out.println("---" + to.getText().toString().replaceAll("\\s", "") + "---(CreateActivity)");
                String[] finalCcs = ccs;
                String[] finalBccs = bccs;
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        final String charset = "UTF-8";
                        final String encoding = "base64";

                        String Subject = subject.getText().toString();

                        List<MimeBodyPart> allPartList = mainAdapter.getAttachmentList();

                        //text/html
                        MimeBodyPart htmlPart = new MimeBodyPart();
                        htmlPart.setText( body.getText().toString().replaceAll("\n", "<br>"), charset,"html");
                        allPartList.add(0, htmlPart);

                        //text/plain
                        MimeBodyPart textPart = new MimeBodyPart();
                        textPart.setText( body.getText().toString(), charset, "text");
                        allPartList.add(0, textPart);

                        mp.sendMail(tos, finalCcs, finalBccs,Subject,allPartList,charset,encoding);

                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                });
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //選択したファイルの処理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOSE_FILE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            if ("content".equalsIgnoreCase( uri.getScheme() )) { //MediaStore
                try {
                    //inputstreamの取得
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    //MIMEタイプの取得
                    String mimeType = getContentResolver().getType(uri);
                    //ファイル名の取得
                    Cursor returnCursor = getContentResolver().query(
                            uri, null, null, null, null
                    );
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);

                    //添付ファイルのパートづくり
                    DataSource dataSource = new ByteArrayDataSource(inputStream,mimeType);
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.setDataHandler(new DataHandler(dataSource));
                    attachmentPart.setFileName(fileName);
                    mainAdapter.addAttachment(attachmentPart);
                } catch (IOException | MessagingException e) {
                    e.printStackTrace();
                }
            } else if ("file".equalsIgnoreCase( uri.getScheme() )) { // File
                //System.out.println("this uri is file://~");
            } else{
                //System.out.println("else uri type");
            }
        }
    }

}
