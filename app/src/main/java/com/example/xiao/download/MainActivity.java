package com.example.xiao.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.xiao.download.entity.FileInfo;
import com.example.xiao.download.service.MyDownloadManager;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

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

        apkInfo = new FileInfo(System.currentTimeMillis(),url_downPApk,"test.apk",0,0);
        bundleInfo = new FileInfo(System.currentTimeMillis()+5,url_downBundle,"bundle.zip",0,0);
        logoInfo = new FileInfo(System.currentTimeMillis()+10,baidu_logo_url,"logo.gif",0,0);
        youdaoApkInfo = new FileInfo(System.currentTimeMillis()+15,youdao_apk_url,"youdao.apk",0,0);

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
                Log.i("xc","fileInfo="+fileId+" length="+length);

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

            @Override
            public void onNetError(long fileId){
                Log.i("xc","网络异常");
                Toast.makeText(mContext,"网络异常",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showNotification(long fileId,String fileName,long size,long finished) {
        NotificationManager notiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notifyBuilder = new Notification.Builder(mContext);

        RemoteViews rv = new RemoteViews(getPackageName(),R.layout.download_notification);
        rv.setTextViewText(R.id.file_name,"text");
        rv.setTextViewText(R.id.file_control,"暂停");
        rv.setTextViewText(R.id.file_size,"300k");
        rv.setTextViewText(R.id.file_finished,"200k");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notifyBuilder.setSmallIcon(R.mipmap.ic_launcher)
//                .setTicker("您有新任务了，请注意接收！")
                .setContentTitle("下载"+fileName)
//                .setContent(rv)
                .setContentText("文件大小："+size)
                .setProgress(100,(int)finished,false)
                .setContentIntent(pendingIntent);
        Notification notification = notifyBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        notification.when = System.currentTimeMillis();
        notiManager.notify((int)fileId, notification);

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
