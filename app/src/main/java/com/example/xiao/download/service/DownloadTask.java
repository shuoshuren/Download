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

    public DownloadTask(Context context, FileInfo fileInfo, int threadCount) {
        this.mContext = context;
        this.mFileInfo = fileInfo;
        this.mThreadCount = threadCount;
        threadDAO = new ThreadDAOImpl(context);
        fileInfoDAO = new FileInfoDAOImpl(context);


    }

    public void download() {
        Log.i("xc", "downloadTask download");
        List<ThreadInfo> threads = threadDAO.getThreads(mFileInfo.getUrl());
        Log.i("xc", "threads size=" + threads.size());
        int len = mFileInfo.getLength() / mThreadCount;
        if (threads.size() == 0) {
            for (int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), len * i, (i + 1) * len - 1, 0);
                if (mThreadCount - 1 == i) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                threads.add(threadInfo);
                threadDAO.insertThread(threadInfo);
                if (!fileInfoDAO.isExists(mFileInfo.getUrl(), mFileInfo.getId())) {
                    fileInfoDAO.insertFileInfo(mFileInfo);
                }
            }
        }

        mDownloadThreadList = new ArrayList<>();
        for (ThreadInfo info : threads) {
            DownloadThread downloadThread = new DownloadThread(info);
            Log.i("xc","threadInfo="+info.toString());
            downloadThread.start();
            mDownloadThreadList.add(downloadThread);
        }
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

                long start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                Log.i("xc","start="+start);
                long end = mThreadInfo.getEnd();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                Log.i("xc", "start="+start+" mFinished=" + mThreadInfo.getFinished());
                raf.seek(start);

                Intent intent = new Intent();
                intent.setAction(DownloadService.ACTION_UPDATE);

                inputStream = connection.getInputStream();
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                long time = System.currentTimeMillis();
                while ((len = inputStream.read(buffer)) != -1) {
                    // 在下载暂停时，保存下载进度
                    if (isPause) {
                        Log.i("xc", "pause finish=" + mThreadInfo.getFinished());
                        threadDAO.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                        return;
                    }

                    raf.write(buffer, 0, len);
                    mThreadInfo.setFinished(mThreadInfo.getFinished()+len);
                    //更新进度
                    if (System.currentTimeMillis() - time >= 1000) {
                        time = System.currentTimeMillis();
                        int f = mThreadInfo.getFinished() * 100 / mFileInfo.getLength();
                        if (f > mFileInfo.getFinished()) {
//                            Log.i("xc", "当前线程" + currentThread().getId() + "已完成= " + mFinished + " 总共= " + mFileInfo.getLength());
                            intent.putExtra("finished", f);
                            intent.putExtra("id", mFileInfo.getId());
                            intent.putExtra("threadId",currentThread().getId());
                            mContext.sendBroadcast(intent);
                        }
                    }
                }
                Log.i("xc","完成了 ThreadInfo="+mThreadInfo.toString());
                setFinished(true);
                checkAllFinished();
            } catch (Exception e) {
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
