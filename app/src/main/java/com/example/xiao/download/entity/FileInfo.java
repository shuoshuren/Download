package com.example.xiao.download.entity;

import java.io.Serializable;

/**
 * 下载文件的信息
 * Created by xiao on 2016/7/11.
 */
public class FileInfo implements Serializable{

    private long id; //id

    private String url; // 下载的路径

    private String filePath;// 下载文件的目录

    private String fileName; // 下载文件名

    private long length; // 文件长度

    private long finished; // 已完成的

    public FileInfo() {
    }

    public FileInfo(long id, String url, String filePath, String fileName, long length, long finished) {
        this.id = id;
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
        this.length = length;
        this.finished = finished;
    }

    public FileInfo(long id, String url, String filePath, String fileName) {
        this.id = id;
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", length=" + length +
                ", finished=" + finished +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        if (id != fileInfo.id) return false;
        if (length != fileInfo.length) return false;
        if (finished != fileInfo.finished) return false;
        if (url != null ? !url.equals(fileInfo.url) : fileInfo.url != null) return false;
        if (filePath != null ? !filePath.equals(fileInfo.filePath) : fileInfo.filePath != null)
            return false;
        return fileName != null ? fileName.equals(fileInfo.fileName) : fileInfo.fileName == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (int) (length ^ (length >>> 32));
        result = 31 * result + (int) (finished ^ (finished >>> 32));
        return result;
    }
}
