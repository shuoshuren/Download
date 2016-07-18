package com.example.xiao.download.entity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 文件进度信息
 * Created by xiao on 2016/7/18.
 */
public class FileProgressInfo implements Serializable {

    private long fileId;

    private HashMap<Long,ThreadProgressInfo> threads = new HashMap<>();

    public FileProgressInfo(long fileId, long threadId, long progress){
        FileProgressInfo instance = new FileProgressInfo();
        instance.addThreadProgress(threadId,progress);
    }

    public void addThreadProgress(long threadId, long progress){
        ThreadProgressInfo progressInfo = threads.get(threadId);
        if(progressInfo == null){
            progressInfo = new ThreadProgressInfo();
        }
        progressInfo.setThreadId(threadId);
        progressInfo.setProgress(progress);
        threads.put(threadId,progressInfo);
    }

    private FileProgressInfo() {
    }


    public long getProgress(){
        long progress = 0;
        for(ThreadProgressInfo progressInfo:threads.values()){
            progress = progressInfo.getProgress()+progress;
        }
        return progress;
    }
}
