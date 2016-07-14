package com.example.xiao.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.xiao.download.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载线程的DAO层接口的实现
 * Created by xiao on 2016/7/11.
 */
public class ThreadDAOImpl implements ThreadDAO {

    private DBHelper mHelper = null;

    public ThreadDAOImpl(Context context){
        mHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = mHelper.openDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(),
                        threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});
        mHelper.closeDatabase();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db = mHelper.openDatabase();
        db.execSQL("delete from thread_info where url = ?",
                new Object[]{url});
        mHelper.closeDatabase();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, long finished) {
        SQLiteDatabase db = mHelper.openDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
                new Object[]{finished, url, thread_id});
        mHelper.closeDatabase();
    }

    @Override
    public synchronized List<ThreadInfo> getThreads(String url) {

        List<ThreadInfo> list = new ArrayList<ThreadInfo>();

        SQLiteDatabase db = mHelper.openDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
        while (cursor.moveToNext())
        {
            ThreadInfo threadInfo = new ThreadInfo();
            threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            threadInfo.setStart(cursor.getLong(cursor.getColumnIndex("start")));
            threadInfo.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
            threadInfo.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
            list.add(threadInfo);
        }
        cursor.close();
        mHelper.closeDatabase();
        return list;
    }

    @Override
    public synchronized boolean isExists(String url, int thread_id) {
        boolean exists =false;
        SQLiteDatabase db = mHelper.openDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?", new String[]{url, thread_id+""});
        if (cursor.getCount() > 0) {
            exists = true;
        }
        cursor.close();
        mHelper.closeDatabase();
        return exists;
    }
}
