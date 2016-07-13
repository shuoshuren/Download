package com.example.xiao.download.entity;

import java.io.Serializable;

/**
 * 下载文件的信息
 * Created by xiao on 2016/7/11.
 */
public class FileInfo implements Serializable{

    private long id; //id

    private String url; // 下载的路径

    private String fileName; // 下载文件名

    private int length; // 文件长度

    private int finished; // 已完成的

    public FileInfo() {
    }

    public FileInfo(long id, String url, String fileName, int length, int finished) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.length = length;
        this.finished = finished;
    }

    public FileInfo(String url,String fileName){
//        this.id =
        this.url = url;
        this.fileName = fileName;
        this.length = 0;
        this.finished = 0;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
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
        return fileName != null ? fileName.equals(fileInfo.fileName) : fileInfo.fileName == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + length;
        result = 31 * result + finished;
        return result;
    }
}
