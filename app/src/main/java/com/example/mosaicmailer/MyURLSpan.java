package com.example.mosaicmailer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Parcel;
import android.text.style.URLSpan;
import android.view.View;
public class MyURLSpan extends URLSpan {

    public MyURLSpan(Parcel src) {
        super(src);
    }

    public MyURLSpan(String url) {
        super(url);
    }
}