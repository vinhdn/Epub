package com.dteviot.epubviewer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dteviot.epubviewer.epub.NavPoint;
import com.dteviot.epubviewer.epub.TableOfContents;

/**
 * Created by vinhdo on 3/29/15.
 */
public class  BookmarkAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private TableOfContents mToc;

    public BookmarkAdapter(Context context, TableOfContents tableOfContents) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mToc = tableOfContents;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BookmarkHolder viewHolder; // holds references to current item's GUI

        // if convertView is null, inflate GUI and create ViewHolder;
        // otherwise, get existing ViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.epub_list_item, null);
            viewHolder = new BookmarkHolder();

            viewHolder.textView = (TextView) convertView.findViewById(R.id.epub_title);
            convertView.setTag(viewHolder); // store as View's tag
        } else {
            viewHolder = (BookmarkHolder) convertView.getTag();
        }

        // Populate the list item (view) with the chapters details
        viewHolder.chapter = (NavPoint)getItem(position);
        String title = viewHolder.chapter.getNavLabel();
        viewHolder.textView.setText(title);

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

    /*
 * Class implementing the "ViewHolder pattern", for better ListView
 * performance
 */
    public class BookmarkHolder {
        public TextView textView; // refers to ListView item's TextView
        public NavPoint chapter;
    }
}


