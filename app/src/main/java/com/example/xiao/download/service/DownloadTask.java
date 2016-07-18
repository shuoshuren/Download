package com.example.xiao.download.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.xiao.download.db.FileInfoDAO;
import com.example.xiao.download.db.FileInfoDAOImpl;
import com.example.xiao.download.db.ThreadDAO;
import com.example.xiao.download.db.ThreadDAOImpl;
import com.example.xiao.download.entity.FileInfo;
import com.example.xiao.download.entity.ThreadInfo;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载文件的Task
 * Created by xiao on 2016/7/11.
 */
public class DownloadTask {

    private Context mContext;
    private FileInfo mFileInfo;
    private int mThreadCount = 1;
    private ThreadDAO threadDAO;
    private FileInfoDAO fileInfoDAO;
    private List<DownloadThread> mDownloadThreadList = null;
    public boolean isPause = false;

    /**
     *
     * @param context 上下文
     * @param fileInfo 下载文件的信息
     * @param threadCount 下载线程的个数
     */
    public DownloadTask(Context context, FileInfo fileInfo, int threadCount) {
        this.mContext = context;
        this.mFileInfo = fileInfo;
        this.mThreadCount = threadCount;
        threadDAO = new ThreadDAOImpl(context);
        fileInfoDAO = new FileInfoDAOImpl(context);

    }

    public void download() {
        Log.i("xc", "downloadTask download");
        if(!checkFileExists()){
            return;
        }
        List<ThreadInfo> threads = threadDAO.getThreads(mFileInfo.getUrl());
        Log.i("xc", "threads size=" + threads.size());
        if (threads.size() == 0) { //如果是新的下载，等分分割文件
            long len = mFileInfo.getLength() / mThreadCount;
            if(len>0){ //如果文件有确定的长度
                for (int i = 0; i < mThreadCount; i++) {
                    ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), len * i, (i + 1) * len - 1, 0);
                    if (mThreadCount - 1 == i) { //如果文件长度不是等分的，则将最后一个线程的结束设置为文件的长度
                        threadInfo.setEnd(mFileInfo.getLength());
                    }
                    threads.add(threadInfo);
                    threadDAO.insertThread(threadInfo); //向数据库中插入线程信息
                    if (!fileInfoDAO.isExists(mFileInfo.getUrl(), mFileInfo.getId())) {
                        fileInfoDAO.insertFileInfo(mFileInfo); //向数据库中插入下载文件的信息
                    }
                }
            }else{ //如果文件长度未知
                ThreadInfo threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, 0, 0);
                threads.add(threadInfo);
                threadDAO.insertThread(threadInfo); //向数据库中插入线程信息
                if (!fileInfoDAO.isExists(mFileInfo.getUrl(), mFileInfo.getId())) {
                    fileInfoDAO.insertFileInfo(mFileInfo); //向数据库中插入下载文件的信息
                }
            }

        }

        //根据线程信息初始化下载线程，并添加到mDownloadThreadList中进行管理
        mDownloadThreadList = new ArrayList<>();
        for (ThreadInfo info : threads) {
            DownloadThread downloadThread = new DownloadThread(info);
            Log.i("xc","threadInfo="+info.toString());
            downloadThread.start();
            mDownloadThreadList.add(downloadThread);
        }
    }

    /**
     * 判断文件是否存在
     */
    private boolean checkFileExists() {
        File file = new File(mFileInfo.getFilePath(), mFileInfo.getFileName());
        if(!file.exists()){
            Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_FILE_NOT_FIND);
            intent.putExtra("id",mFileInfo.getId());
            intent.putExtra("url",mFileInfo.getUrl());
            mContext.sendBroadcast(intent);
            return false;
        }
        return true;
    }

    //下载线程
    private class DownloadThread extends Thread {

        private ThreadInfo mThreadInfo;
        private boolean isFinished = false;  // 线程是否执行完毕

        public boolean isFinished() {
            return isFinished;
        }

        public void setFinished(boolean finished) {
            isFinished = finished;
        }

        public DownloadThread(ThreadInfo threadInfo) {
            this.mThreadInfo = threadInfo;
        }

        @Override
        public void run() {
            Log.i("xc", "downloadThread start");
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);

                //设置下载的开始和结束位置，向请求头中设置参数，服务器就会从设置的开始和结束位置传输文件
                long start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                long end = mThreadInfo.getEnd();
                if(end>0){
                    connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
                }else{
                    connection.setRequestProperty("Range","bytes="+start+"-");
                }

                //通过RandomAccessFile进行随机文件的读写操作
                File file = new File(mFileInfo.getFilePath(), mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                //通过intent发送进度通知的broadcast
                Intent intent = new Intent();
                intent.setAction(DownloadService.ACTION_UPDATE);

                inputStream = connection.getInputStream();
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                long time = System.currentTimeMillis();
                while ((len = inputStream.read(buffer)) != -1) {


                    raf.write(buffer, 0, len);
                    mThreadInfo.setFinished(mThreadInfo.getFinished()+len);
                    //更新进度
                    if (System.currentTimeMillis() - time >= 1000) {
                        time = System.currentTimeMillis();
                        long f = mThreadInfo.getFinished();
//                        if (f > mFileInfo.getFinished()) {
                            intent.putExtra("finished", f);
                            intent.putExtra("id", mFileInfo.getId());
                            intent.putExtra("threadId",currentThread().getId());
                            mContext.sendBroadcast(intent);
                        threadDAO.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
//                        }
                    }
                    // 在下载暂停时，保存下载进度，退出下载线程
                    if (isPause) {
                        Log.i("xc", "pause finish=" + mThreadInfo.getFinished());
                        threadDAO.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                        return;
                    }
                }
                //文件下载完成
                Log.i("xc","完成了 ThreadInfo="+mThreadInfo.toString());
                setFinished(true);
                checkAllFinished();
            } catch (Exception e) {
                sendNetError();
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DownloadThread that = (DownloadThread) o;

            return mThreadInfo != null ? mThreadInfo.equals(that.mThreadInfo) : that.mThreadInfo == null;

        }

        @Override
        public int hashCode() {
            return mThreadInfo != null ? mThreadInfo.hashCode() : 0;
        }
    }

    /**
     * 发送网络错误的广播
     */
    private void sendNetError(){
        Intent intent = new Intent();
        intent.setAction(DownloadService.ACTION_NET_ERROR);
        intent.putExtra("id",mFileInfo.getId());
        mContext.sendBroadcast(intent);
    }

    /**
     * 判断下载该文件的所有线程是否下载完成,如果下载完成就发送下载完成的广播和删除对应数据库中的信息
     */
    public void checkAllFinished() {
        boolean allFinished = true;
        for (DownloadThread thread : mDownloadThreadList) {
            if (!thread.isFinished()) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            //删除下载记录
            threadDAO.deleteThread(mFileInfo.getUrl());
            fileInfoDAO.deleteFileInfo(mFileInfo.getUrl());
            Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_FINISHED);
            intent.putExtra("id", mFileInfo.getId());
            mContext.sendBroadcast(intent);
        }

    }
}
