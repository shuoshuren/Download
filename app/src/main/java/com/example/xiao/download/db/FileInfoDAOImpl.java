package com.example.xiao.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.xiao.download.entity.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiao on 2016/7/12.
 */
public class FileInfoDAOImpl implements FileInfoDAO {

    private DBHelper mHelper = null;

    public FileInfoDAOImpl(Context context){
        mHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertFileInfo(FileInfo fileInfo) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("insert into file_info(file_id,url,file_name,length,finished) values(?,?,?,?,?)",
                new Object[]{fileInfo.getId(), fileInfo.getUrl(),
                        fileInfo.getFileName(), fileInfo.getLength(), fileInfo.getFinished()});
        db.close();
    }

    @Override
    public synchronized void deleteFileInfo(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from file_info where url = ?",
                new Object[]{url});
        db.close();
    }

    @Override
    public synchronized void updateFileInfo(String url, int file_id, int finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update file_info set finished = ? where url = ? and file_id = ?",
                new Object[]{finished, url, file_id});
        db.close();
    }

    @Override
    public synchronized List<FileInfo> getFiles(String url) {
        List<FileInfo> list = new ArrayList<FileInfo>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from file_info where url = ?", new String[]{url});
        while (cursor.moveToNext())
        {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(cursor.getLong(cursor.getColumnIndex("file_id")));
            fileInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            fileInfo.setFileName(cursor.getString(cursor.getColumnIndex("file_name")));
            fileInfo.setLength(cursor.getInt(cursor.getColumnIndex("length")));
            fileInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
            list.add(fileInfo);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public synchronized List<FileInfo> getAllFileInfo() {
        List<FileInfo> list = new ArrayList<FileInfo>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from file_info where 1 = 1", null);
        while (cursor.moveToNext())
        {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(cursor.getLong(cursor.getColumnIndex("file_id")));
            fileInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            fileInfo.setFileName(cursor.getString(cursor.getColumnIndex("file_name")));
            fileInfo.setLength(cursor.getInt(cursor.getColumnIndex("length")));
            fileInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
            list.add(fileInfo);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public synchronized boolean isExists(String url, long file_id) {
        boolean isExists = false;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from file_info where url = ? and file_id = ?", new String[]{url, file_id+""});
        if(cursor.getCount()>0){
            isExists = true;
        }
        cursor.close();
        db.close();
        return isExists;
    }
}
