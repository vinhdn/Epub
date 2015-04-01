package com.vinhblue.epub.database.dao;

import android.content.Context;

import com.vinhblue.epub.database.BookmarkDAO;

/**
 * Created by vinhdo on 3/29/15.
 */
public class MyDAOFactory extends DAOFactory {

    public static MyDAOFactory instance;
    private BookmarkDAO bookmarkDAO;

    public MyDAOFactory(Context context) {
        super(context);
        bookmarkDAO = new BookmarkDAO(context);
    }

    public static MyDAOFactory instance(Context context) {
        if (instance == null) {
            instance = new MyDAOFactory(context);
        }
        return instance;
    }

    public BookmarkDAO getBookmarkDAO(){
        return this.bookmarkDAO;
    }
}
