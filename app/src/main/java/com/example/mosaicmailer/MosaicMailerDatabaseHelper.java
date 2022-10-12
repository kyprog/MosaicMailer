package com.example.mosaicmailer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MosaicMailerDatabaseHelper extends SQLiteOpenHelper {
    static final private String DBNAME = "MosaicMailerDB";
    static final private int VERSION = 1;
    Context context;

    MosaicMailerDatabaseHelper(Context context){
        super(context,DBNAME,null,VERSION);
        this.context=context;
    }

    //onCreateイベントは、データベースが使用されるときにデータベースファイルが存在していない場合に発生する
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db != null) {
            db.execSQL("CREATE TABLE accountsInfo (_id INTEGER PRIMARY KEY, mailaddress TEXT, password TEXT);");
            db.execSQL("CREATE TABLE HeadsUpInfo (_id INTEGER PRIMARY KEY, mailaddress TEXT, keyword TEXT);");
            db.execSQL("CREATE TABLE WhiteList (_id INTEGER PRIMARY KEY, type TEXT, URL TEXT);");
            db.execSQL("CREATE TABLE BlackList (_id INTEGER PRIMARY KEY, type TEXT, URL TEXT);");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS books");
            onCreate(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public void fileDelete(){
        context.deleteDatabase(DBNAME);
    }
}
