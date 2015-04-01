package com.vinhblue.epub.Activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.vinhblue.epub.R;
import com.vinhblue.epub.Utils.ConvertUtil;

/**
 * Created by vinhdo on 3/27/15.
 */

public class HomeActivity extends Activity implements View.OnClickListener{

    private ListView mAnotherAppLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
    }

    private void initViews(){
        findViewById(R.id.book_in_home).setOnClickListener(this);
        findViewById(R.id.rate).setOnClickListener(this);
        initData();
    }

    private void initData(){
        mAnotherAppLv = (ListView) findViewById(R.id.another_book_lv);
        mAnotherAppLv.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.book_in_home:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.rate:
                rate();
                break;
        }
    }

    private void rate(){
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    private View getViewApp(String url, String image){
        final ImageView view = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                (int) ConvertUtil.convertDpToPixel(this, getResources().getDimension(R.dimen.height_image_home)),
                (int) ConvertUtil.convertDpToPixel(this, getResources().getDimension(R.dimen.height_image_home)));
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);

        return view;
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_another_app, null);
//            ImageView view = new ImageView(HomeActivity.this);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.height_image_home),
//                    (int) getResources().getDimension(R.dimen.height_image_home));
//            layoutParams.setMargins(10,0,10,0);
//            view.setLayoutParams(layoutParams);
//            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//            view.setBackgroundResource(R.drawable.logo_512);
            return view;
        }

    };
}
