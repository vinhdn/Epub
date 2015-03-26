package com.dteviot.epubviewer;

import android.app.Application;

import com.dteviot.epubviewer.epub.Book;

/**
 * Created by vinhdo on 3/26/15.
 */
public class EbookApplication extends Application {
    private Book mBook;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /*
         * Book to show
         */
    public void setBook(String fileName) {
        // if book already loaded, don't load again
        if ((mBook == null) || !mBook.getFileName().equals(fileName)) {
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
}
