package com.example.xiao.download;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.xiao.download.entity.FileInfo;
import com.example.xiao.download.service.MyDownloadManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startButton,stopButton,restartButton;
    private Context mContext = MainActivity.this;
    private FileInfo apkInfo = null;
    private FileInfo bundleInfo = null;
    private FileInfo logoInfo = null;
    private FileInfo youdaoApkInfo = null;
    private MyDownloadManager manager;

    private static final String local_author =  "http://192.168.0.114:20000";
    private static final String url_downPApk = local_author + "/apk/downloads" + "?type=apk";
    private static final String url_downBundle = local_author+"/apk/downloads" + "?type=attach";

    private static final String baidu_logo_url = "http://www.baidu.com/img/bdlogo.gif";
    private static final String youdao_apk_url = "http://wap.apk.anzhi.com/data2/apk/201607/01/14c8103c8f28e175f45724b9cba5f930_87799500.apk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apkInfo = new FileInfo(System.currentTimeMillis(),url_downPApk,"test.apk",0,0);
        bundleInfo = new FileInfo(System.currentTimeMillis()+5,url_downBundle,"bundle.zip",0,0);
        logoInfo = new FileInfo(System.currentTimeMillis(),baidu_logo_url,"logo.gif",0,0);
        youdaoApkInfo = new FileInfo(System.currentTimeMillis(),youdao_apk_url,"youdao.apk",0,0);

        manager = MyDownloadManager.getInstance(mContext);
        manager.setDownloadUnFinished(true);
        initView();
        initEvent();

    }

    private void initView() {
        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);
        restartButton = (Button) findViewById(R.id.restart);

    }
    private void initEvent() {
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);

        manager.setDownloadListener(new MyDownloadManager.DownloadListener() {

            @Override
            public void onStart(long fileId,long length){
                Log.i("xc","fileId="+fileId+" 长度="+length);
            }

            @Override
            public void onProgressUpdate(long fileId,long threadId,long progress) {
                Log.i("xc","进度 fileid="+fileId +" 线程="+threadId +" progress="+progress);
            }

            @Override
            public void onFinished(long fileId) {
                Log.i("xc","已完成 fileId="+fileId);
                Toast.makeText(mContext,"已经完成",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFileNotFind(long fileId){
                Log.i("xc","文件不存在");
                Toast.makeText(mContext,"文件不存在",Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                manager.startDownload(apkInfo);
//                manager.startDownload(bundleInfo);
//                manager.startDownload(logoInfo);
//                manager.startDownload(youdaoApkInfo);
                break;

            case R.id.stop:
                manager.stopDownload(apkInfo);
//                manager.stopDownload(bundleInfo);
//                manager.stopDownload(logoInfo);
//                manager.stopDownload(youdaoApkInfo);
                break;

            case R.id.restart:
                manager.restartDownload(apkInfo);
//                manager.restartDownload(bundleInfo);
//                manager.restartDownload(logoInfo);
//                manager.restartDownload(youdaoApkInfo);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        manager.destroy();
        super.onDestroy();
    }

}
