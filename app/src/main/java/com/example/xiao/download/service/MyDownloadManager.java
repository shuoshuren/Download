package com.example.xiao.download.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.xiao.download.db.FileInfoDAO;
import com.example.xiao.download.db.FileInfoDAOImpl;
import com.example.xiao.download.entity.FileInfo;

import java.util.List;

/**
 * Created by xiao on 2016/7/11.
 */
public class MyDownloadManager {

    private static MyDownloadManager manager;
    private Context mContext;

    private DownloadService downloadService;

    private boolean isDownloadUnFinished = false;

    private FileInfoDAO fileInfoDao;

    public boolean isDownloadUnFinished() {
        return isDownloadUnFinished;
    }

    public void setDownloadUnFinished(boolean downloadUnFinished) {
        isDownloadUnFinished = downloadUnFinished;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            DownloadService.MyBinder mBinder = (DownloadService.MyBinder) service;
            if(mBinder!= null){
                Log.i("xc","downloadService connected");
                downloadService = mBinder.getDownloadService();
                if(isDownloadUnFinished){
                    downloadUnFinished();
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private MyDownloadManager(Context context){
        this.mContext = context;
        fileInfoDao = new FileInfoDAOImpl(context);
        initDownloadService();

    }

    public static MyDownloadManager getInstance(Context context){
        if(manager == null){
            manager = new MyDownloadManager(context);
        }
        return manager;
    }

    private void initDownloadService() {
        Intent intent = new Intent(mContext,DownloadService.class);
        mContext.bindService(intent,connection,Context.BIND_AUTO_CREATE);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        mContext.registerReceiver(mReceiver, filter);

    }

    public void destroy(){
        Log.i("xc","manager destroy");
        if(mReceiver != null){
            mContext.unregisterReceiver(mReceiver);
        }
        if(downloadService != null){
            downloadService.stopAllDownload();
            mContext.unbindService(connection);
            connection = null;
        }

    }


    private void downloadUnFinished() {
        List<FileInfo> fileList =  fileInfoDao.getAllFileInfo();
        Log.i("xc","downloadUnFinished size="+fileList.size());
        for(FileInfo fileInfo:fileList){
            startDownload(fileInfo);
        }
    }

    /**
     * 开始下载
     * @param fileInfo
     */
    public void startDownload(FileInfo fileInfo){
        if(downloadService != null){
            Log.i("xc","开始下载");
            downloadService.startDownload(fileInfo);
        }
    }

    /**
     * 停止下载
     * @param fileInfo
     */
    public void stopDownload(FileInfo fileInfo){
        if(downloadService != null){
            Log.i("xc","停止下载");
            downloadService.stopDownload(fileInfo);
        }
    }

    /**
     * 重新下载
     * @param fileInfo
     */
    public void restartDownload(FileInfo fileInfo){
        if(downloadService != null){
            Log.i("xc","重新开始下载");
            downloadService.restartDownload(fileInfo);
        }
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action  = intent.getAction();
            if(action.equals(DownloadService.ACTION_UPDATE)){
                long fileId = intent.getLongExtra("id", -1);
                int progress =  intent.getIntExtra("finished", 0);
                long threadId = intent.getLongExtra("threadId",-1);
                if(listener != null){
                    listener.onProgressUpdate(fileId,threadId,progress);

                }
            }else if(action.equals(DownloadService.ACTION_FINISHED)){
                long fileId = intent.getLongExtra("id",-1);
                if(listener != null){
                    listener.onFinished(fileId);
                }
                if(downloadService!=null){
                    downloadService.downloadFinished(fileId);
                }
            }
        }
    };

    public interface DownloadListener{
        void onProgressUpdate(long fileId,long threadId,int progress);
        void onFinished(long fileId);
    }

    private DownloadListener listener;

    public void setDownloadListener(DownloadListener listener){
        this.listener = listener;
    }

}
