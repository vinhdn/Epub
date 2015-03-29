package com.dteviot.epubviewer.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dteviot.epubviewer.MainActivity;
import com.dteviot.epubviewer.R;

/**
 * Created by vinhdo on 3/27/15.
 */

public class HomeActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
    }

    private void initViews(){
        findViewById(R.id.book_in_home).setOnClickListener(this);
        findViewById(R.id.rate).setOnClickListener(this);
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

    }
}
