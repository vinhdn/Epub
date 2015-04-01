package com.vinhblue.epub.database.sqlite;

import android.content.Context;

/**
 * Created by vinhdo on 3/29/15.
 */
public class Connection extends DBHelper{
    private static Connection connection;

    private Connection(Context context) {
        super(context);
    }

    public static synchronized Connection getConnection(Context context) {
        if (null == connection) {
            connection = new Connection(context);
        }
        return connection;
    }
}
