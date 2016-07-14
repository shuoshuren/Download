package com.example.xiao.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库的操作类
 * Created by xiao on 2016/7/11.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";
    private static final int VERSION = 1;
    private static final String SQL_CREATE_THREAD_INFO = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer, url text, start long, end long, finished long)";
    private static final String SQL_DROP_THREAD_INFO = "drop table if exists thread_info";

    private static final String SQL_CREATE_FILE_INFO = "create table file_info(_id integer primary key autoincrement,"+
            "file_id integer, url text,file_name text,length long,finished long)";

    private static final String SQL_DROP_FILE_INFO = "drop table if exists thread_info";

    private static DBHelper sDbHelper = null;

    private AtomicInteger mOpenCounter = new AtomicInteger();

    private SQLiteDatabase mDatabase;

    private DBHelper(Context context){
        super(context,DB_NAME,null,VERSION);
    }

    public static DBHelper getInstance(Context context){
        if(sDbHelper == null){
            synchronized (DBHelper.class){
                if(sDbHelper == null){
                    sDbHelper = new DBHelper(context);
                }
            }
        }
        return sDbHelper;
    }

    //打开数据库方法
    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {//incrementAndGet会让mOpenCounter自动增长1
            // Opening new database
            try {
                mDatabase = sDbHelper.getWritableDatabase();
            } catch (Exception e) {
                mDatabase = sDbHelper.getReadableDatabase();
            }
        }
        return mDatabase;
    }

    //关闭数据库方法
    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {//decrementAndGet会让mOpenCounter自动减1
            // Closing database
            mDatabase.close();
        }
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
