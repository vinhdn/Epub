package com.vinhblue.epub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vinhblue.epub.R;
import com.vinhblue.epub.epub.NavPoint;
import com.vinhblue.epub.epub.TableOfContents;

/**
 * Created by vinhdo on 3/30/15.
 */
public class ChapterAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        private TableOfContents mToc;
        private int mCurChapterID;
        private Context mContext;

        public ChapterAdapter(Context context, TableOfContents data, int chapterId) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mToc = data;
            mCurChapterID = chapterId;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChapterHolder viewHolder; // holds references to current item's GUI

            // if convertView is null, inflate GUI and create ViewHolder;
            // otherwise, get existing ViewHolder
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.epub_list_item, null);
                viewHolder = new ChapterHolder();

                viewHolder.nameTv = (TextView) convertView.findViewById(R.id.epub_title);
                convertView.setTag(viewHolder); // store as View's tag
            } else {
                viewHolder = (ChapterHolder) convertView.getTag();
            }

            // Populate the list item (view) with the chapters details
            viewHolder.chapter = (NavPoint)getItem(position);
            String title = viewHolder.chapter.getNavLabel();
            if(position == mCurChapterID - 1){
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

    public class ChapterHolder{
        public NavPoint chapter;
        public TextView nameTv;
    }
}
