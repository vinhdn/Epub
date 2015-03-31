package com.dteviot.epubviewer.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dteviot.epubviewer.R;

/**
 * Created by trungdo on 3/31/15.
 */
public class LoadingDialog extends DialogFragment{
    private String title;
    private Context mContext;

    public static LoadingDialog newInstance(Context context,String title){
        LoadingDialog dialog = new LoadingDialog();
        dialog.title = title;
        dialog.mContext = context;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog localDialog = new Dialog(this.mContext, R.style.MyDialogTheme);
        localDialog.setCanceledOnTouchOutside(false);
        localDialog.setCancelable(false);
        localDialog.requestWindowFeature(1);
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.copyFrom(localDialog.getWindow().getAttributes());
        localLayoutParams.width = -1;
        localLayoutParams.height = (6 * localDialog.getWindow().getAttributes().height / 10);
        localDialog.getWindow().setAttributes(localLayoutParams);
        Display localDisplay = ((WindowManager) this.mContext
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        localDialog.getWindow().setLayout(-1, 3 * localDisplay.getHeight() / 5);
        View localView = LayoutInflater.from(this.mContext).inflate(
                R.layout.layout_dialog_loading, null);
        TextView tvTitle = (TextView) localView.findViewById(R.id.title_dialog_tv);
        tvTitle.setText(title);
        localDialog.setContentView(localView);
        return localDialog;
    }
}
