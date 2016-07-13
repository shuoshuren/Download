package com.example.xiao.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.xiao.download.entity.FileInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by xiao on 2016/7/11.
 */
public class DownloadService extends Service {
    public static final String DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final String ACTION_RESTART = "ACTION_RESTART";
    public static final int MSG_INIT = 0;
    private String TAG = "DownloadService";
    private HashMap<Long,DownloadTask> tasks = new HashMap<>();



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    public void startDownload(FileInfo fileInfo){
        new InitThread(fileInfo).start();
    }

    public void stopDownload(FileInfo fileInfo){
        DownloadTask task = tasks.get(fileInfo.getId());
        if(task!=null){
            task.isPause = true;
        }
    }

    public void stopAllDownload(){
        for(DownloadTask task : tasks.values()){
            if(task != null){
                task.isPause = true;
            }
        }
    }

    public void restartDownload(FileInfo fileInfo){
        DownloadTask task = tasks.get(fileInfo.getId());
        if(task!=null){
            Log.i("xc","DownloadService 重新下载");
            task.isPause = false;
            task.download();
        }
    }

    public void downloadFinished(long fileId){
        if(tasks.containsKey(fileId)){
            tasks.remove(fileId);
        }
    }

    private class InitThread extends Thread{
        private FileInfo mFileInfo = null;

        public InitThread(FileInfo fileInfo){
            this.mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            Log.i("xc","initThread start");
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            try{
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                int length = -1;
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    length = connection.getContentLength();
                }
                if(length <=0){
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                // 在本地创建文件
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                // 设置文件长度
                Log.i("xc", "length=" + length);
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try{
                    if(connection != null){
                        connection.disconnect();
                    }
                    if(raf!= null){
                        raf.close();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    DownloadTask task = new DownloadTask(DownloadService.this,fileInfo,3);
                    task.download();
                    tasks.put(fileInfo.getId(),task);
                    break;

                default:
                    break;
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private MyBinder mBinder = new MyBinder();

    public class MyBinder extends Binder {

        public DownloadService getDownloadService(){
            return DownloadService.this;
        }
    }
}
