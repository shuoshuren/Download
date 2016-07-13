package com.example.xiao.download.db;

import com.example.xiao.download.entity.FileInfo;

import java.util.List;

/**
 * Created by xiao on 2016/7/12.
 */
public interface FileInfoDAO {

    /**
     * 插入文件信息
     * @param fileInfo
     * @return void
     */
    public void insertFileInfo(FileInfo fileInfo);

    /**
     * 删除文件信息
     * @param url
     * @return void
     */
    public void deleteFileInfo(String url);
    /**
     * 更新文件下载进度
     * @param url
     * @param file_id
     * @return void
     */
    public void updateFileInfo(String url, int file_id, int finished);
    /**
     * 查询文件
     * @param url
     * @return
     * @return List<FileInfo>
     */
    public List<FileInfo> getFiles(String url);

    /**
     * 查询所有的文件
     * @return
     */
    public List<FileInfo> getAllFileInfo();
    /**
     * 线程信息是否存在
     * @param url
     * @param file_id
     * @return
     * @return boolean
     */
    public boolean isExists(String url, long file_id);
}
