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
 * 下载的service
 * Created by xiao on 2016/7/11.
 */
public class DownloadService extends Service {
    private String TAG = "DownloadService";
    //文件下载的存放目录
    public static final String DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    public static final String ACTION_RESTART = "ACTION_RESTART";
    public static final int MSG_INIT = 0;

    //管理下载Task的HashMap
    private HashMap<Long,DownloadTask> tasks = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开始一个新的文件下载
     * @param fileInfo
     */
    public void startDownload(FileInfo fileInfo){
        new InitThread(fileInfo).start();
    }

    /**
     * 停止该文件的下载
     * @param fileInfo
     */
    public void stopDownload(FileInfo fileInfo){
        DownloadTask task = tasks.get(fileInfo.getId());
        if(task!=null){
            task.isPause = true;
        }
    }

    /**
     * 停止所有的下载
     */
    public void stopAllDownload(){
        for(DownloadTask task : tasks.values()){
            if(task != null){
                task.isPause = true;
            }
        }
    }

    /**
     * 重新开始下载，从之前暂停的地方开始下载
     * @param fileInfo
     */
    public void restartDownload(FileInfo fileInfo){
        DownloadTask task = tasks.get(fileInfo.getId());
        if(task!=null){
            Log.i("xc","DownloadService 重新下载");
            task.isPause = false;
            task.download();
        }
    }

    /**
     * 文件下载完成后，根据下载文件的id删除对应的DownloadTask
     * @param fileId
     */
    public void downloadFinished(long fileId){
        if(tasks.containsKey(fileId)){
            tasks.remove(fileId);
        }
    }

    /**
     * 初始化文件的线程
     * 主要是得到下载文件的长度和创建文件夹目录和文件
     */
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
                case MSG_INIT: //主要用于初始化DownloadTask，并添加到tasks中进行管理
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
