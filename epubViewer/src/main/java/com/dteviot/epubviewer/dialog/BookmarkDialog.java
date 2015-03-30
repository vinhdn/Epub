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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.dteviot.epubviewer.Bookmark;
import com.dteviot.epubviewer.R;
import com.dteviot.epubviewer.adapter.BookmarkAdapter;
import com.dteviot.epubviewer.adapter.ChapterAdapter;
import com.dteviot.epubviewer.database.dao.MyDAOFactory;
import com.dteviot.epubviewer.epub.NavPoint;
import com.dteviot.epubviewer.epub.TableOfContents;

import java.util.ArrayList;

/**
 * Created by trungdo on 3/31/15.
 */
public class BookmarkDialog extends DialogFragment{
    public interface OnItemClicked{
        public void onClicked(Bookmark item);
    }
    private OnItemClicked mListener;
    private Context mContext;
    private ArrayList<Bookmark> mData;
    private int mCurChapterID;
    private ListView mDataLv;
    public static BookmarkDialog newInstance(Context context, int chapterID, OnItemClicked mListener){
        BookmarkDialog dialog = new BookmarkDialog();
        dialog.mContext = context;
        dialog.mData = MyDAOFactory.instance(context).getBookmarkDAO().selectAllRow();
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
        ((TextView)localView.findViewById(R.id.title_dialog_tv)).setText(mContext.getString(R.string.bookmark));
        mDataLv = (ListView) localView.findViewById(R.id.data_lv);
        if(mData.size() <= 0){
            mDataLv.setVisibility(View.GONE);
            localView.findViewById(R.id.no_data).setVisibility(View.VISIBLE);
            return localDialog;
        }
        mDataLv.setVisibility(View.VISIBLE);
        localView.findViewById(R.id.no_data).setVisibility(View.GONE);
        mDataLv.setAdapter(new BookmarkAdapter(mContext,mData,mCurChapterID));
        mDataLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BookmarkAdapter.BookmarHolder holder = (BookmarkAdapter.BookmarHolder)view.getTag();
                if(mListener != null){
                    mListener.onClicked(holder.chapter);
                    dismiss();
                }
            }
        });
        return localDialog;
    }
}
