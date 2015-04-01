package com.vinhblue.epub.database;

import android.annotation.SuppressLint;

import com.vinhblue.epub.EbookApplication;

/**
 * Created by vinhdo on 3/29/15.
 */
public class DBConfig {
    private static int DB_VERSION = 1;

    public static final String TABLE_BOOKMARK = "bookmark";
    public static final String COL_ID = "id";
    public static final String COL_URI = "uri";
    public static final String COL_NAME = "name";
    public static final String COL_TIME = "time";
    public static final String COL_SCROLLY = "scrollY";
    private static String DB_NAME = "ebook.sqlite";
    public static String getDatabaseName() {
        return DB_NAME;
    }

    @SuppressLint("SdCardPath")
    public static String getDBPath() {
        return "/data/data/" + EbookApplication.class.getPackage().getName()
                + "/databases/";
    }

    public static String getDBFullPath() {
        return getDBPath() + DB_NAME;
    }

    public static int getVersion() {
        return DB_VERSION;
    }
}
