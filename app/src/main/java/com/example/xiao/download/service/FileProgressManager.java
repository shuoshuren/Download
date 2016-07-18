package com.example.xiao.download.service;

import android.util.Log;

import com.example.xiao.download.entity.FileProgressInfo;

import java.util.HashMap;

/**
 * 进度管理类
 * Created by xiao on 2016/7/18.
 */
public class FileProgressManager {

    private static HashMap<Long,FileProgressInfo> fileMap = new HashMap<>();

    private static FileProgressManager instance;

    public static FileProgressManager getInstance(long fileId, long threadId, long progress){
        if(instance == null){
            instance = new FileProgressManager();
        }
        FileProgressInfo progressInfo = fileMap.get(fileId);
        if(progressInfo == null){
            progressInfo = new FileProgressInfo(fileId,threadId,progress);
            Log.i("xc","progressInfo="+progressInfo.hashCode());
        }
        progressInfo.addThreadProgress(threadId,progress);
        fileMap.put(fileId,progressInfo);
        return instance;
    }

    private FileProgressManager(){

    }

    public long getProgress(long fileId){
        FileProgressInfo progressInfo = fileMap.get(fileId);
        return progressInfo.getProgress();
    }

}
