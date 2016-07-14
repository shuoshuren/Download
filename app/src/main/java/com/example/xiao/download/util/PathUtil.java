package com.example.xiao.download.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Created by xiao on 2016/7/14.
 */
public class PathUtil {

    //文件下载的存放默认目录
    public static final String DEFAULT_DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/downloads/";

    /**
     * 设置下载文件的路径
     *
     * @param context
     * @param downloadPath
     */
    public static void setDownloadPath(Context context, String downloadPath){
        SharedPreferences sp = context.getSharedPreferences("download", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("download_path", downloadPath);
        editor.commit();
    }

    /**
     * 得到下载文件路径
     *
     * @param context
     * @return
     */
    public static String getDownloadPath(Context context) {
        SharedPreferences sp = context.getSharedPreferences("download", Context.MODE_PRIVATE);
        String downloadPath = sp.getString("download_path", DEFAULT_DOWNLOAD_PATH);
        return downloadPath;
    }


}
