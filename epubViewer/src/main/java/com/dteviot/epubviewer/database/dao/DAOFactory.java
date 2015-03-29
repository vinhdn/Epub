package com.dteviot.epubviewer.database.dao;

import android.content.Context;

/**
 * Created by vinhdo on 3/29/15.
 */
public abstract class DAOFactory {

    // List of storage types supported by the factory
    public static final int SQLITEDATABASES = 1;
    public static final int SHAREDPREFERENCES = 2;
    public static final int INTERNALSTORAGE = 3;
    public static final int EXTERNALSTORAGE = 4;
    public static final int NETWORKCONNECTION = 5;

    public DAOFactory(Context context) {
    }

    /***
     *
     * @param context
     * @param whichFactory
     * @return
     */
    public static DAOFactory getDAOFactory(Context context, int whichFactory) {

        switch (whichFactory) {
            case SQLITEDATABASES:
                return new MyDAOFactory(context);
            default:
                return null;
        }
    }
}

