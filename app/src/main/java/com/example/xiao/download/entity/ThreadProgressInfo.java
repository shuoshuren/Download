package com.example.xiao.download.entity;

import java.io.Serializable;

/**
 * 线程进度信息
 * Created by xiao on 2016/7/18.
 */
public class ThreadProgressInfo implements Serializable {

    private long threadId;

    private long progress;

    public ThreadProgressInfo() {
    }

    public ThreadProgressInfo(long threadId, long progress) {
        this.threadId = threadId;
        this.progress = progress;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThreadProgressInfo that = (ThreadProgressInfo) o;

        if (threadId != that.threadId) return false;
        return progress == that.progress;

    }

    @Override
    public int hashCode() {
        int result = (int) (threadId ^ (threadId >>> 32));
        result = 31 * result + (int) (progress ^ (progress >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ThreadProgressInfo{" +
                "threadId=" + threadId +
                ", progress=" + progress +
                '}';
    }
}
