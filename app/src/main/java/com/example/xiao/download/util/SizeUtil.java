package com.example.xiao.download.util;

/**
 * Created by xiao on 2016/7/18.
 */
public class SizeUtil {

    private static final long kSize = 1024;

    private static final long mSize = 1024*1024;

    public static String getSize(long size){
        if(size<0){
            throw new RuntimeException("size 不能小于0");
        }
        if(size > 1024 && size<1024*1024){
            float length = size / 1024.00f;
            return (float)(Math.round(length*100))/100 +"K";
        }else if(size > 1024*1024){
            float length = size / 1048576.00f;
            return (float)(Math.round(length*100))/100 +"M";
        }
        return null;
    }
}
