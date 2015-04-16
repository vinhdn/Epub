package com.vinhblue.epub.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vinhblue.epub.EbookApplication;
import com.vinhblue.epub.R;
import com.vinhblue.epub.database.dao.MyDAOFactory;
import com.vinhblue.epub.epub.Book;
import com.vinhblue.epub.models.SearchResult;

import java.util.ArrayList;

/**
 * Created by trungdo on 4/14/15.
 */
public class SearchDialog extends DialogFragment implements View.OnClickListener{
    private EditText mSearchEdt;
    private ListView mResultLv;
    private LinearLayout mNoData;
    private EbookApplication mApp;
    private LoadingDialog mDialog;

    private Context mContext;

    public interface OnItemSearchClick{
        public void onClick(String uri, String searchText);
    }

    private OnItemSearchClick mListener;

    public static SearchDialog newInstance(Context context, OnItemSearchClick mListener){
        SearchDialog dialog = new SearchDialog();
        dialog.mContext = context;
        dialog.mListener = mListener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog localDialog = new Dialog(mContext, R.style.MyDialogTheme);
        localDialog.setCanceledOnTouchOutside(true);
        localDialog.setCancelable(true);
        localDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = localDialog.getWindow().getAttributes();

//        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
//        wmlp.horizontalMargin = 30;
//        wmlp.verticalMargin = 50;
        mDialog = LoadingDialog.newInstance(mContext,getResources().getString(R.string.searching));
        mApp = (EbookApplication)getActivity().getApplication();
        View localView = LayoutInflater.from(this.mContext).inflate(
                R.layout.activity_search, null);
        localDialog.setContentView(localView);
        initViews(localView);
        return localDialog;
    }

    private void initViews(View view){
        mSearchEdt = (EditText) view.findViewById(R.id.search_edt);
        mSearchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    searchClicked();
                }
                return false;
            }
        });
        mResultLv = (ListView) view.findViewById(R.id.result_search_lv);
        view.findViewById(R.id.search_btn).setOnClickListener(this);
        mResultLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchAdapter.SearchHolder holder = (SearchAdapter.SearchHolder) view.getTag();
                Intent intent = new Intent();
                intent.putExtra("URI", holder.chapter.getUri().toString());
                intent.putExtra("SearchText", mSearchEdt.getText().toString());
                if (mListener != null) {
                    mListener.onClick(holder.chapter.getUri().toString(),mSearchEdt.getText().toString());
                }
                dismiss();
            }
        });
        mNoData = (LinearLayout)view.findViewById(R.id.no_data_result);
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
                mDialog.show(getActivity().getSupportFragmentManager(),"Searching dialog");
                mNoData.setVisibility(View.GONE);
                mApp.getBook().search(searchText,new Book.SearchListener() {
                    @Override
                    public void onFinish(ArrayList<SearchResult> data) {
                        if(data.size() <= 0){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultLv.setVisibility(View.GONE);
                                    mResultLv.setAdapter(null);
                                    mNoData.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mNoData.setVisibility(View.VISIBLE);
                                        }
                                    }, 100);
                                }
                            });
                        }{
                            mResultLv.setAdapter(new SearchAdapter(mContext, data));
                            getActivity().runOnUiThread(new Runnable() {
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
                Toast.makeText(mContext, getString(R.string.have_error), Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(mContext,getString(R.string.search_text_must_least_3),Toast.LENGTH_SHORT).show();
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
