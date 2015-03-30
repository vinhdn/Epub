package com.dteviot.epubviewer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Patterns;

import com.dteviot.epubviewer.epub.Book;

/**
 * Created by vinhdo on 3/26/15.
 */
public class EbookApplication extends Application {
    private Book mBook;
    public SharedPreferences mPref;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /*
         * Book to show
         */
    public void setBook(String fileName) {
        // if book already loaded, don't load again
        if ((mBook == null)) {
            try {
                mBook = new Book(fileName,this);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public Book getBook() {
        return mBook;
    }

    public Bookmark getLastReading(){
        Bookmark bookmark = new Bookmark();
        String url = mPref.getString("last_uri","");
        if(url != null && url.startsWith("http://localhost:1025/")) {
            int scrollY = mPref.getInt("last_scroll", 0);
            bookmark.setResourceUri(url);
            bookmark.setScrollY(scrollY);
            return bookmark;
        }
        setLastReading("",0);
        return bookmark;
    }

    public void setLastReading(String url,int scrollY){
        if(url != null && url.startsWith("http://localhost:1025/")) {
            mPref.edit().putString("last_uri",url).commit();
            mPref.edit().putInt("last_scroll", scrollY).commit();
        }
    }
}
