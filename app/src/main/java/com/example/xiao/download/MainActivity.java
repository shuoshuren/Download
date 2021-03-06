package com.example.xiao.download;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.xiao.download.entity.FileInfo;
import com.example.xiao.download.service.MyDownloadManager;
import com.example.xiao.download.util.PathUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startButton,stopButton,restartButton;
    private Context mContext = MainActivity.this;
    private FileInfo apkInfo = null;
    private FileInfo bundleInfo = null;
    private FileInfo logoInfo = null;
    private FileInfo youdaoApkInfo = null;
    private MyDownloadManager manager;

    private static final String remote_author =  "http://219.153.20.235:22000";//192.168.0.114:20000
    private static final String local_author = "http://192.168.0.114:20000";
    private static final String author = local_author;
    private static final String url_downPApk = author + "/apk/downloads" + "?type=apk";
    private static final String url_downBundle = author+"/apk/downloads" + "?type=attach";

    private static final String baidu_logo_url = "http://www.baidu.com/img/bdlogo.gif";
    private static final String youdao_apk_url = "http://wap.apk.anzhi.com/data2/apk/201607/01/14c8103c8f28e175f45724b9cba5f930_87799500.apk";

    private boolean debug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(debug){
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            );
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apkInfo = new FileInfo(System.currentTimeMillis(),url_downPApk, PathUtil.getDownloadPath(mContext),"test.apk",0,0);
        bundleInfo = new FileInfo(System.currentTimeMillis()+5,url_downBundle,PathUtil.getDownloadPath(mContext),"bundle.zip",0,0);
        logoInfo = new FileInfo(System.currentTimeMillis()+10,baidu_logo_url,PathUtil.getDownloadPath(mContext),"logo.gif",0,0);
        youdaoApkInfo = new FileInfo(System.currentTimeMillis()+15,youdao_apk_url,PathUtil.getDownloadPath(mContext),"youdao.apk",0,0);

        manager = MyDownloadManager.getInstance(mContext);
        manager.setDownloadUnFinished(true);
        manager.setShowNotification(true);
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
                Log.i("xc","fileInfo="+fileId+" length="+length);

            }

            @Override
            public void onProgressUpdate(long fileId,long threadId,long progress) {
//                Log.i("xc","进度 fileid="+fileId +" 线程="+threadId +" progress="+progress);
                FileInfo fileInfo = manager.getDownloadFileInfo(fileId);
//                long allProgress = 0;
//                FileProgressManager manager = FileProgressManager.getInstance(fileId,threadId,progress);
//                allProgress = manager.getProgress(fileId);
//                showNotification(fileId,fileInfo.getFileName(),fileInfo.getLength(),allProgress);

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

            @Override
            public void onNetError(long fileId){
                Log.i("xc","网络异常");
                Toast.makeText(mContext,"网络异常",Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                manager.startDownload(apkInfo);
//                manager.startDownload(bundleInfo);
                manager.startDownload(logoInfo);
                manager.startDownload(youdaoApkInfo);
                break;

            case R.id.stop:
                manager.stopDownload(apkInfo);
//                manager.stopDownload(bundleInfo);
                manager.stopDownload(logoInfo);
                manager.stopDownload(youdaoApkInfo);
                break;

            case R.id.restart:
                manager.restartDownload(apkInfo);
//                manager.restartDownload(bundleInfo);
                manager.restartDownload(logoInfo);
                manager.restartDownload(youdaoApkInfo);
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
