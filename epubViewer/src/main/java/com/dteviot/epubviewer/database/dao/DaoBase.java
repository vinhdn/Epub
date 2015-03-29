package com.dteviot.epubviewer.database.dao;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.dteviot.epubviewer.database.sqlite.Connection;

public class DaoBase{

    protected String tag = "DaoBase";
    protected Connection dbConnection;
    protected Context context;

    /**
     * constructor
     */
    public DaoBase(Context context) {
        this.context = context;
        dbConnection = Connection.getConnection(context);
        tag = getClass().getSimpleName();
    }

    protected int deleteAll(String table) {
        int res = -1;
        if (!dbConnection.openSession()) {
            return res;
        }
        res = dbConnection.delete(table, null, null);
        dbConnection.closeSession();
        return res;
    }

    protected int deleteAll(String table, String whereClause, String[] args) {
        int res = -1;
        if (!dbConnection.openSession())
            return res;
        res = dbConnection.delete(table, whereClause, args);
        dbConnection.closeSession();
        return res;
    }

    protected long insertRow(String table, ContentValues values) {
        if (!dbConnection.openSession())
            return -1;
        Log.d(tag, "inserting row");
        long insertId = dbConnection.insert(table, null, values);
        dbConnection.closeSession();
        return insertId;
    }

    protected long insertCollection(String table, List<ContentValues> values) {
        if (!dbConnection.openSession())
            return -1;
        Log.d(tag, "inserting collection");
        long insertId = dbConnection.insert(table, null, values);
        dbConnection.closeSession();
        return insertId;
    }

}
