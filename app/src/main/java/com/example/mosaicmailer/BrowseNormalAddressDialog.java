package com.example.mosaicmailer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

public class BrowseNormalAddressDialog extends DialogFragment {
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
                .inflate(R.layout.browse_normal_address_dialog, null);

        //差出人
        TextView fromName = layout.findViewById(R.id.FromNameTextView);
        if(mp.senderName == null || mp.senderName.equals("")){
            fromName.setText("差出人名："+mp.senderMailAddress);
        }else{
            fromName.setText("差出人名："+mp.senderName);
        }

        //実際のメールアドレス
        TextView mailAddress = layout.findViewById(R.id.AddressTextView);
        mailAddress.setText("From："+mp.senderMailAddress);
        mailAddress.setMovementMethod(new ScrollingMovementMethod());
        mailAddress.setHeight(400);
        mailAddress.setScrollbarFadingEnabled(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(layout).create();
    }
}
