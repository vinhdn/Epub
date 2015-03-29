package com.dteviot.epubviewer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.dteviot.epubviewer.Bookmark;
import com.dteviot.epubviewer.database.dao.DaoBase;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by vinhdo on 3/29/15.
 */
public class BookmarkDAO extends DaoBase{
    /**
     * constructor
     *
     * @param context
     */
    public BookmarkDAO(Context context) {
        super(context);
    }

    private ContentValues createContentValueFromRow(Bookmark row){
        ContentValues values = new ContentValues();
        values.put(DBConfig.COL_ID,row.getId());
        values.put(DBConfig.COL_NAME, row.getName());
        values.put(DBConfig.COL_URI,row.getResourceUri().toString());
        values.put(DBConfig.COL_SCROLLY, row.getScrollY());
        values.put(DBConfig.COL_TIME,System.currentTimeMillis() + "");
        return values;
    }

    private Bookmark createRowFromCusor(Cursor cs){
        Bookmark bookmark = new Bookmark();
        bookmark.setId(cs.getInt(cs.getColumnIndex(DBConfig.COL_ID)));
        bookmark.setName(cs.getString(cs.getColumnIndex(DBConfig.COL_NAME)));
        bookmark.setResourceUri(cs.getString(cs.getColumnIndex(DBConfig.COL_URI)));
        bookmark.setScrollY(cs.getFloat(cs.getColumnIndex(DBConfig.COL_SCROLLY)));
        return bookmark;
    }

    /**
     * insert row to db
     *
     * @param row
     *            true if row is made by user false if row is made by sync
     *            dropbox
     * @return
     */
    public long insertRow(Bookmark row) {
        if (!dbConnection.openSession())
            return -1;
        ContentValues values = createContentValueFromRow(row);
        long insertId = dbConnection.insert(DBConfig.TABLE_BOOKMARK, null,
                values);
        dbConnection.closeSession();
        return insertId;
    }

    public int deleteRow(int ChapterID){
        int res = -1;
        if (!dbConnection.openSession())
            return res;
        res = dbConnection.delete(DBConfig.TABLE_BOOKMARK,DBConfig.COL_ID + " = ?",new String[]{ChapterID + ""});
        dbConnection.closeSession();
        return res;
    }

    public ArrayList<Bookmark> selectAllRow(){
        ArrayList<Bookmark> data = new ArrayList<>();
        try{
            Cursor cursor = null;
            if(!dbConnection.openSession()) {
                Log.d(this.getClass().getName(),"db Bookmark open failed");
                return data;
            }
            cursor = dbConnection.query(DBConfig.TABLE_BOOKMARK,null,null,null,null,null,DBConfig.COL_ID);
            if(cursor != null){
                if(cursor.moveToFirst()){
                    Bookmark bookmark;
                    do{
                        bookmark = createRowFromCusor(cursor);
                        data.add(bookmark);
                    }while (cursor.moveToNext());
                }
            }
            cursor.close();
            dbConnection.closeSession();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }

    public boolean isSaved(int id){
        try{
            Cursor cursor = null;
            if(!dbConnection.openSession())
                return false;
            cursor = dbConnection.query(DBConfig.TABLE_BOOKMARK,null,DBConfig.COL_ID + "=?",new String[]{id + ""},null,null,null);
            if(cursor == null){
                return false;
            }else {
                if(cursor.moveToFirst())
                    return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return false;
    }
}
