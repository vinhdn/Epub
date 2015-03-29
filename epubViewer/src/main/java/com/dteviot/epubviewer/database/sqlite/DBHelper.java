package com.dteviot.epubviewer.database.sqlite;

import java.io.IOException;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dteviot.epubviewer.database.DBConfig;

public abstract class DBHelper extends SQLiteOpenHelper {

    private SQLiteDatabase sqLiteDatabase;

    private String sqlCreateTableBookmark = "CREATE TABLE "
            + DBConfig.TABLE_BOOKMARK + " ( "
            + DBConfig.COL_ID + " INTEGER PRIMARY KEY NOT NULL,"
            + DBConfig.COL_NAME + " TEXT,"
            + DBConfig.COL_URI + " TEXT,"
            + DBConfig.COL_TIME + " TEXT,"
            + DBConfig.COL_SCROLLY + " FLOAT"
            + ");";

    /**
     *
     * @param context
     */
    public DBHelper(Context context) {
        super(context, DBConfig.getDatabaseName(), null, DBConfig.getVersion());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            createDatabase(db);
        } catch (IOException exception) {
            Log.e("","err create database" + exception.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void alterTableAddColumn(SQLiteDatabase db, String query){
        try{
            db.execSQL(query);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /***
     *
     * @throws IOException
     */
    private void createDatabase(SQLiteDatabase db) throws IOException {
        try {
            db.beginTransaction();
            db.execSQL(sqlCreateTableBookmark);
            db.setTransactionSuccessful();
            Log.i("", "created database");
        } catch (Exception e) {
            Log.e("","error create db " + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    /**
     *
     * @return
     */
    public boolean truncateTable(SQLiteDatabase db, String tableName) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            return true;
        } catch (SQLException sqlException) {
            return false;
        }

    }

    /**
     *
     * @return
     */
    public boolean truncateTable(String tableName) {
        try {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
            return true;
        } catch (SQLException sqlException) {
            return false;
        }

    }

    /***
     *
     * @return
     */
    public boolean openSession() {
        try {
            sqLiteDatabase = getWritableDatabase();
            return true;
        } catch (RuntimeException runtimeException) {
            return false;
        }
    }

    /**
     *
     * @param query
     * @return
     */
    public int makeQuery(String query) {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.execSQL(query);
            sqLiteDatabase.setTransactionSuccessful();
            return 1;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return 0;
        } finally {
            sqLiteDatabase.endTransaction();
        }

    }

    /**
     *
     * @param query
     * @return
     */
    public Cursor getCursor(String query) {
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    /**
     * closes the database
     */
    public void closeSession() {
        try {
            sqLiteDatabase.close();
        } catch (RuntimeException runtimeException) {
            runtimeException.printStackTrace();
        }
    }

    /**
     * insert a row to desire table
     *
     * @param table
     *            the table to insert the row into optional; may be null. SQL
     *            doesn't allow inserting a completely empty row without naming
     *            at least one column name. If your provided values is empty, no
     *            column names are known
     * @param nullColumnHack
     *            and an empty row can't be inserted. If not set to null, the
     *            nullColumnHack parameter provides the name of nullable column
     *            name to explicitly insert a NULL into in the case where your
     *            values is empty.
     * @param values
     *            this map contains the initial column values for the row. The
     *            keys should be the column names and the values the column
     *            values
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        sqLiteDatabase.beginTransaction();
        long insertId = -1;
        try {
            insertId = sqLiteDatabase.insertWithOnConflict(table, nullColumnHack, values,SQLiteDatabase.CONFLICT_REPLACE);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            insertId = -1;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return insertId;
    }

    /**
     * insert a row collection to desire table
     *
     * @param table
     *            the table to insert the row into optional; may be null. SQL
     *            doesn't allow inserting a completely empty row without naming
     *            at least one column name. If your provided values is empty, no
     *            column names are known
     * @param nullColumnHack
     *            and an empty row can't be inserted. If not set to null, the
     *            nullColumnHack parameter provides the name of nullable column
     *            name to explicitly insert a NULL into in the case where your
     *            values is empty.
     * @param values
     *            row collection. The keys should be the column names and the
     *            values the column values
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(String table, String nullColumnHack,
                       List<ContentValues> values) {
        sqLiteDatabase.beginTransaction();
        long insertId = -1;
        try {
            for (ContentValues value : values) {
                insertId = sqLiteDatabase.insert(table, nullColumnHack, value);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            insertId = -1;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return insertId;
    }

    /**
     * Convenience method for deleting rows in the database.x
     *
     * @param table
     * @param whereClause
     *            the optional WHERE clause to apply when deleting. Passing null
     *            will delete all rows.
     * @param whereArgs
     * @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        sqLiteDatabase.beginTransaction();
        int res = -1;
        try {
            res = sqLiteDatabase.delete(table, whereClause, whereArgs);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            res = -1;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return res;
    }

    /**
     * Query the given table, returning a Cursor over the result set.
     *
     * @param table
     * @param columns
     *            A list of which columns to return. Passing null will return
     *            all columns, which is discouraged to prevent reading data from
     *            storage that isn't going to be used.
     * @param selection
     *            A filter declaring which rows to return, formatted as an SQL
     *            WHERE clause (excluding the WHERE itself). Passing null will
     *            return all rows for the given table.
     * @param selectionArgs
     *            You may include ?s in selection, which will be replaced by the
     *            values from selectionArgs, in order that they appear in the
     *            selection. The values will be bound as Strings.
     * @param groupBy
     *            A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @param having
     *            A filter declare which row groups to include in the cursor, if
     *            row grouping is being used, formatted as an SQL HAVING clause
     *            (excluding the HAVING itself). Passing null will cause all row
     *            groups to be included, and is required when row grouping is
     *            not being used.
     * @param orderBy
     *            How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @return A Cursor object, which is positioned before the first entry. Note
     *         that Cursors are not synchronized, see the documentation for more
     *         details.
     */
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {

        sqLiteDatabase.beginTransaction();
        Cursor res = null;
        try {
            res = sqLiteDatabase.query(table, columns, selection,
                    selectionArgs, groupBy, having, orderBy);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            res = null;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return res;
    }

    /**
     * Query the given table, returning a Cursor over the result set.
     *
     * @param sql
     *            raw sql statement
     * @param selectionArgs
     * @return
     */
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        sqLiteDatabase.beginTransaction();
        Cursor res = null;
        try {
            res = sqLiteDatabase.rawQuery(sql, selectionArgs);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            res = null;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return res;
    }

    /**
     * Update a row in desired table
     *
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int update(String table, ContentValues values, String whereClause,
                      String[] whereArgs) {
        sqLiteDatabase.beginTransaction();
        int res = -1;
        try {
            res = sqLiteDatabase.update(table, values, whereClause, whereArgs);
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            res = 0;
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return res;
    }

}
