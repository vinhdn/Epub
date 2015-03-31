package com.dteviot.epubviewer.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dteviot.epubviewer.EbookApplication;
import com.dteviot.epubviewer.R;
import com.dteviot.epubviewer.dialog.LoadingDialog;
import com.dteviot.epubviewer.epub.Book;
import com.dteviot.epubviewer.models.SearchResult;

import java.util.ArrayList;


public class SearchActivity extends FragmentActivity implements View.OnClickListener{

    private EditText mSearchEdt;
    private ListView mResultLv;
    private LinearLayout mNoData;
    private EbookApplication mApp;
    private LoadingDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = LoadingDialog.newInstance(this,getResources().getString(R.string.searching));
        mApp = (EbookApplication)getApplication();
        setContentView(R.layout.activity_search);
        initViews();
    }
    private void initViews(){
        mSearchEdt = (EditText) findViewById(R.id.search_edt);
        mSearchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    searchClicked();
                }
                return false;
            }
        });
        mResultLv = (ListView) findViewById(R.id.result_search_lv);
        findViewById(R.id.search_btn).setOnClickListener(this);
        mResultLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchAdapter.SearchHolder holder = (SearchAdapter.SearchHolder)view.getTag();
                Intent intent = new Intent();
                intent.putExtra("URI",holder.chapter.getUri().toString());
                intent.putExtra("SearchText",mSearchEdt.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mNoData = (LinearLayout)findViewById(R.id.no_data_result);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.search_btn){
            searchClicked();
        }
    }

    private void searchClicked(){
        String searchText = mSearchEdt.getText().toString();
        if(searchText.length() >= 3){
            if(mApp.getBook() != null){
                mDialog.show(getSupportFragmentManager(),"Searching dialog");
                mNoData.setVisibility(View.GONE);
                mApp.getBook().search(searchText,new Book.SearchListener() {
                    @Override
                    public void onFinish(ArrayList<SearchResult> data) {
                        if(data.size() <= 0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultLv.setVisibility(View.GONE);
                                    mResultLv.setAdapter(null);
                                    mNoData.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                           mNoData.setVisibility(View.VISIBLE);
                                        }
                                    },100);
                                }
                            });
                        }{
                            mResultLv.setAdapter(new SearchAdapter(SearchActivity.this,data));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultLv.setVisibility(View.VISIBLE);
                                    mNoData.setVisibility(View.GONE);
                                }
                            });
                        }
                        mDialog.dismiss();
                    }
                });
            }else
                Toast.makeText(this,getString(R.string.have_error),Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(this,getString(R.string.search_text_must_least_3),Toast.LENGTH_SHORT).show();
    }
}
class SearchAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<SearchResult> mToc;
    private Context mContext;

    public SearchAdapter(Context context, ArrayList<SearchResult> data) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mToc = data;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchHolder viewHolder; // holds references to current item's GUI

        // if convertView is null, inflate GUI and create ViewHolder;
        // otherwise, get existing ViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_result_search, null);
            viewHolder = new SearchHolder();

            viewHolder.nameTv = (TextView) convertView.findViewById(R.id.result_title);
            viewHolder.previewTv = (TextView) convertView.findViewById(R.id.result_content);
            convertView.setTag(viewHolder); // store as View's tag
        } else {
            viewHolder = (SearchHolder) convertView.getTag();
        }

        // Populate the list item (view) with the chapters details
        viewHolder.chapter = (SearchResult)getItem(position);
        String title = viewHolder.chapter.getTitle();
        viewHolder.nameTv.setTextColor(mContext.getResources().getColor(android.R.color.black));
        viewHolder.nameTv.setText(title);
        String preview = viewHolder.chapter.getPreviewContent();
        if(preview.equals(""))
            viewHolder.previewTv.setVisibility(View.GONE);
        else{
            viewHolder.previewTv.setVisibility(View.VISIBLE);
            viewHolder.previewTv.setText(preview);
        }
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

    public class SearchHolder{
        public SearchResult chapter;
        public TextView nameTv, previewTv;
    }
}
