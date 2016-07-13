package com.example.xiao.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库的操作类
 * Created by xiao on 2016/7/11.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";
    private static final int VERSION = 1;
    private static final String SQL_CREATE_THREAD_INFO = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer, url text, start integer, end integer, finished integer)";
    private static final String SQL_DROP_THREAD_INFO = "drop table if exists thread_info";

    private static final String SQL_CREATE_FILE_INFO = "create table file_info(_id integer primary key autoincrement,"+
            "file_id integer, url text,file_name text,length integer,finished integer)";

    private static final String SQL_DROP_FILE_INFO = "drop table if exists thread_info";

    private static DBHelper sDbHelper = null;

    private DBHelper(Context context){
        super(context,DB_NAME,null,VERSION);
    }

    public static DBHelper getInstance(Context context){
        if(sDbHelper == null){
            sDbHelper = new DBHelper(context);
        }
        return sDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_THREAD_INFO);
        db.execSQL(SQL_CREATE_FILE_INFO);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_THREAD_INFO);
        db.execSQL(SQL_DROP_FILE_INFO);
        db.execSQL(SQL_CREATE_THREAD_INFO);
        db.execSQL(SQL_CREATE_FILE_INFO);
    }
}
