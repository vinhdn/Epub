package com.dteviot.epubviewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dteviot.epubviewer.Bookmark;
import com.dteviot.epubviewer.R;
import com.dteviot.epubviewer.epub.NavPoint;
import com.dteviot.epubviewer.epub.TableOfContents;

import java.util.ArrayList;

/**
 * Created by trungdo on 3/31/15.
 */
public class BookmarkAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private ArrayList<Bookmark> mToc;
    private int mCurChapterID;
    private Context mContext;

    public BookmarkAdapter(Context context, ArrayList<Bookmark> data, int chapterId) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mToc = data;
        mCurChapterID = chapterId;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BookmarHolder viewHolder; // holds references to current item's GUI

        // if convertView is null, inflate GUI and create ViewHolder;
        // otherwise, get existing ViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.epub_list_item, null);
            viewHolder = new BookmarHolder();

            viewHolder.nameTv = (TextView) convertView.findViewById(R.id.epub_title);
            convertView.setTag(viewHolder); // store as View's tag
        } else {
            viewHolder = (BookmarHolder) convertView.getTag();
        }

        // Populate the list item (view) with the chapters details
        viewHolder.chapter = (Bookmark)getItem(position);
        String title = viewHolder.chapter.getName();
        if(mCurChapterID == viewHolder.chapter.getId()){
            viewHolder.nameTv.setTextColor(mContext.getResources().getColor(R.color.blue));
        }else{
            viewHolder.nameTv.setTextColor(mContext.getResources().getColor(android.R.color.black));
        }
        viewHolder.nameTv.setText(title);

        return convertView;
    }

    @Override
    public int getCount() {
        return mToc.size();
    }

    @Override
    public Object getItem(int position) {
        return mToc.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class BookmarHolder{
        public Bookmark chapter;
        public TextView nameTv;
    }
}
