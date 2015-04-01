package com.vinhblue.epub.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.vinhblue.epub.R;
import com.vinhblue.epub.adapter.ChapterAdapter;
import com.vinhblue.epub.epub.NavPoint;
import com.vinhblue.epub.epub.TableOfContents;

/**
 * Created by vinhdo on 3/30/15.
 */
public class ChapterDialog extends DialogFragment{

    public interface OnItemClicked{
        public void onClicked(NavPoint item);
    }
    private OnItemClicked mListener;
    private Context mContext;
    private TableOfContents mData;
    private int mCurChapterID;
    private ListView mDataLv;
    public static ChapterDialog newInstance(Context context, TableOfContents data, int chapterID, OnItemClicked mListener){
        ChapterDialog dialog = new ChapterDialog();
        dialog.mContext = context;
        dialog.mData = data;
        dialog.mCurChapterID = chapterID;
        dialog.mListener = mListener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog localDialog = new Dialog(this.mContext, R.style.MyDialogTheme);
        localDialog.setCanceledOnTouchOutside(true);
        localDialog.setCancelable(true);
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
                R.layout.layout_dialog, null);
        localDialog.setContentView(localView);
        ((TextView)localView.findViewById(R.id.title_dialog_tv)).setText(mContext.getString(R.string.table_of_contents));
        mDataLv = (ListView) localView.findViewById(R.id.data_lv);
        if(mData.size() <= 0){
            mDataLv.setVisibility(View.GONE);
            localView.findViewById(R.id.no_data).setVisibility(View.VISIBLE);
            ((TextView)localView.findViewById(R.id.no_data_title)).setText(mContext.getString(R.string.no_chapter));
            ((TextView)localView.findViewById(R.id.no_data_content)).setText(mContext.getString(R.string.has_not_chapter));
            return localDialog;
        }
        mDataLv.setVisibility(View.VISIBLE);
        localView.findViewById(R.id.no_data).setVisibility(View.GONE);
        mDataLv.setAdapter(new ChapterAdapter(mContext,mData,mCurChapterID));
        mDataLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChapterAdapter.ChapterHolder holder = (ChapterAdapter.ChapterHolder)view.getTag();
                if(mListener != null){
                    mListener.onClicked(holder.chapter);
                    dismiss();
                }
            }
        });
        return localDialog;
    }
}
