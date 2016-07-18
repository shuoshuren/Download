package com.example.xiao.download.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.example.xiao.download.db.FileInfoDAO;
import com.example.xiao.download.db.FileInfoDAOImpl;
import com.example.xiao.download.db.ThreadDAO;
import com.example.xiao.download.db.ThreadDAOImpl;
import com.example.xiao.download.entity.FileInfo;

import java.util.List;

/**
 * 下载的manager
 * Created by xiao on 2016/7/11.
 */
public class MyDownloadManager {

    private static MyDownloadManager manager;
    private Context mContext;

    private DownloadService downloadService;

    private boolean isDownloadUnFinished = false; //是否自动开始以前的未完成的任务

    private FileInfoDAO fileInfoDao;
    private ThreadDAO threadDao;

    public boolean isDownloadUnFinished() {
        return isDownloadUnFinished;
    }

    public void setDownloadUnFinished(boolean downloadUnFinished) {
        isDownloadUnFinished = downloadUnFinished;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //当service绑定连接成功后获取到downloadService
            DownloadService.MyBinder mBinder = (DownloadService.MyBinder) service;
            if (mBinder != null) {
                Log.i("xc", "downloadService connected");
                downloadService = mBinder.getDownloadService();

                //如果支持下载未完成
                if (isDownloadUnFinished) {
                    downloadUnFinished();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private MyDownloadManager(Context context) {
        this.mContext = context;
        fileInfoDao = new FileInfoDAOImpl(context);
        threadDao = new ThreadDAOImpl(context);
        initDownloadService();

    }

    public static MyDownloadManager getInstance(Context context) {
        if (manager == null) {
            manager = new MyDownloadManager(context);
        }
        return manager;
    }

    private void initDownloadService() {

        //绑定service
        Intent intent = new Intent(mContext, DownloadService.class);
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_START);
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        filter.addAction(DownloadService.ACTION_FILE_NOT_FIND);
        filter.addAction(DownloadService.ACTION_NET_ERROR);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mReceiver, filter);

    }

    /**
     * 当activity的生命周期进入到onDestroy()调用该方法，
     * 1.解除注册的BroadcastReceiver
     * 2.停止所有的下载任务
     * 3.解除绑定的DownloadService
     */
    public void destroy() {
        Log.i("xc", "manager destroy");
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
        if (downloadService != null) {
            downloadService.stopAllDownload();
            mContext.unbindService(connection);
            connection = null;
        }else{
            throw new RuntimeException("DownloadService 没有被绑定");
        }

    }


    /**
     * 开始下载未完成的
     */
    private void downloadUnFinished() {
        List<FileInfo> fileList = fileInfoDao.getAllFileInfo();
        Log.i("xc", "downloadUnFinished size=" + fileList.size());
        for (FileInfo fileInfo : fileList) {
            if (downloadService != null) {
                downloadService.startDownload(fileInfo, false);
            }
        }
    }

    /**
     * 开始下载
     *
     * @param fileInfo
     */
    public void startDownload(FileInfo fileInfo) {
        if (downloadService != null) {
            Log.i("xc", "开始下载");
            downloadService.startDownload(fileInfo, true);
        }else{
            throw new RuntimeException("DownloadService 没有被绑定");
        }
    }

    /**
     * 停止下载
     *
     * @param fileInfo
     */
    public void stopDownload(FileInfo fileInfo) {
        if (downloadService != null) {
            Log.i("xc", "停止下载");
            downloadService.stopDownload(fileInfo);
        }else{
            throw new RuntimeException("DownloadService 没有被绑定");
        }
    }

    /**
     * 停止下载文件
     *
     * @param fileId
     */
    public void stopDownload(long fileId) {
        if (downloadService != null) {
            Log.i("xc", "停止下载");
            downloadService.stopDownload(fileId);
        }else{
            throw new RuntimeException("DownloadService 没有被绑定");
        }
    }

    /**
     * 重新下载该文件，从之前暂停的地方开始下载
     *
     * @param fileInfo
     */
    public void restartDownload(FileInfo fileInfo) {
        if (downloadService != null) {
            Log.i("xc", "重新开始下载");
            downloadService.restartDownload(fileInfo);
        }else{
            throw new RuntimeException("DownloadService 没有被绑定");
        }
    }

    /**
     * 停止所有的下载
     */
    public void stopAllDownload() {
        if (downloadService != null) {
            Log.i("xc", "停止所有的下载");
            downloadService.stopAllDownload();
        }else{
            throw new RuntimeException("DownloadService 没有被绑定");
        }
    }

    /**
     * 获取下载文件信息
     *
     * @param fileId
     * @return
     */
    public FileInfo getDownloadFileInfo(long fileId) {
        if (downloadService != null) {
            return downloadService.getDownloadFileInfo(fileId);
        }
        return null;
    }


    /**
     * 接收广播
     * 主要接收每个线程下载的的进度通知和下载完成后的通知
     */
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadService.ACTION_START)) {
                long fileId = intent.getLongExtra("id", -1);
                long length = intent.getLongExtra("length", -1);
                if (listener != null) {
                    listener.onStart(fileId, length);
                }

            } else if (action.equals(DownloadService.ACTION_UPDATE)) {
                long fileId = intent.getLongExtra("id", -1);
                long progress = intent.getLongExtra("finished", 0);
                long threadId = intent.getLongExtra("threadId", -1);
                if (listener != null) {
                    listener.onProgressUpdate(fileId, threadId, progress);

                }
            } else if (action.equals(DownloadService.ACTION_FINISHED)) {
                long fileId = intent.getLongExtra("id", -1);
                if (listener != null) {
                    listener.onFinished(fileId);
                }
                if (downloadService != null) {
                    downloadService.downloadFinished(fileId);
                }
            } else if (action.equals(DownloadService.ACTION_FILE_NOT_FIND)) {
                long fileId = intent.getLongExtra("id", -1);
                String url = intent.getStringExtra("url");
                if (listener != null) {
                    listener.onFileNotFind(fileId);
                }
                if (fileInfoDao.isExists(url, fileId)) {
                    fileInfoDao.deleteFileInfo(url);
                }
                if (threadDao.getThreads(url).size() > 0) {
                    threadDao.deleteThread(url);
                }
            } else if (action.equals(DownloadService.ACTION_NET_ERROR)) {
                long fileId = intent.getLongExtra("id", -1);
                if (listener != null) {
                    listener.onNetError(fileId);
                }
                stopDownload(fileId);
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo activeInfo = connManager.getActiveNetworkInfo();
                if (activeInfo != null) {
                    Log.i("xc", "wifiInfo=" + wifiInfo.isConnected() + " mobileInfo=" + mobileInfo.isConnected() + " activeInfo=" + activeInfo.getTypeName());
                    downloadUnFinished();
                } else {
                    Log.i("xc", "无网络");
                    stopAllDownload();
                }
            }
        }
    };

    /**
     * 下载过程中的监听器
     */
    public interface DownloadListener {

        void onStart(long fileId, long length);

        /**
         * 当进度变化的时候
         *
         * @param fileId   文件的id
         * @param threadId 下载的线程的id
         * @param progress 当前线程的进度
         */
        void onProgressUpdate(long fileId, long threadId, long progress);

        /**
         * 当文件下载完成后
         *
         * @param fileId 下载文件的id
         */
        void onFinished(long fileId);

        /**
         * 文件不存在
         *
         * @param fileId
         */
        void onFileNotFind(long fileId);

        /**
         * 当网络出现异常时
         *
         * @param fileId
         */
        void onNetError(long fileId);
    }

    private DownloadListener listener;

    public void setDownloadListener(DownloadListener listener) {
        this.listener = listener;
    }

}
