package com.face.ethlinstener.ui.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ethernet.EthernetManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.face.ethlinstener.R;
import com.face.ethlinstener.ui.activity.EthLinstenerActivity;
import com.face_chtj.base_iotutils.FileUtils;
import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.SPUtils;
import com.face_chtj.base_iotutils.ShellUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EthLinstenerService extends Service {
    private static final String TAG=EthLinstenerService.class.getSimpleName();
    public static Disposable sDisposable;
    private NotificationManager manager = null;
    private Notification.Builder builder = null;
    private RemoteViews contentView = null;
    public static final String KEY_CYCLE_POSITION="cycleIntervalPosition";
    public static final String KEY_CYCLE="cycleInterval";
    private static final String DNS_CONFIG="/sdcard/DCIM/dns.config";
    private String [] DEFAULT_DNS=new String[]{
            "119.29.29.98","223.6.6.6"
    };

    @Override
    public void onCreate() {
        super.onCreate ( );
        manager = (NotificationManager) EthLinstenerService.this.getSystemService(NOTIFICATION_SERVICE);
        builder = new Notification.Builder(EthLinstenerService.this);
        Intent notificationIntent = new Intent(EthLinstenerService.this, EthLinstenerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentItent = PendingIntent.getActivity(EthLinstenerService.this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView = new RemoteViews(getApplication().getPackageName(), R.layout.activity_notification);
        contentView.setTextViewText(R.id.tvEthStatus, "当前以太网状态:正在加载...");
        builder.setContent(contentView);
        builder.setContentIntent(contentItent);
        builder.setSmallIcon(R.drawable.network_eth);  //小图标，在大图标右下角
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.network_eth)); //大图标，没有设置时小图标就是大图标
        builder.setOngoing(true);//滑动不能清除
        builder.setAutoCancel(false);   //点击的时候消失
        manager.notify(14, builder.build());  //参数一为ID，用来区分不同APP的Notification
        int time = SPUtils.getInt(KEY_CYCLE, 1);
        Log.e(TAG, "start task....，cycleInterval：" + time);
        sDisposable = Observable
                .interval(0, time, TimeUnit.MINUTES)
                //取消任务时取消定时唤醒
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        cancelJobAlarmSub();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long count) throws Exception {
                        try {
                            List<String> readDnsList= FileUtils.readLineToList (DNS_CONFIG);
                            String [] dnsList=null;
                            if (readDnsList.size ()>0){
                                dnsList=readDnsList.toArray(new String[0]);
                            }else{
                                dnsList=DEFAULT_DNS;
                            }
                            ShellUtils.CommandResult commandResult=ShellUtils.execCommand ("cat /sys/class/net/eth0/carrier",true);
                            int status=(commandResult.result==0&&commandResult.successMsg.contains ("1"))?1:0;
                            boolean pingResult = NetUtils.checkNetWork (dnsList,1,1);
                            Log.e(TAG, "eth1 connect status=" + status + ",pingResult="+pingResult);
                            if (!pingResult||status==0) {
                                if(!pingResult){
                                    contentView.setTextViewText(R.id.tvEthStatus, "当前以太网状态：网络异常");
                                    manager.notify(14, builder.build());
                                }
                                if(status==0/*||!isOn*/){
                                    contentView.setTextViewText(R.id.tvEthStatus, "当前以太网状态：以太网开关未启用");
                                    manager.notify(14, builder.build());
                                }
                                ShellUtils.execCommand ("ifconfig eth0 down",true);//关闭以太网
                                Log.e(TAG, "eth down" );
                                Thread.sleep(500);
                                ShellUtils.execCommand ("ifconfig eth0 up",true);//关闭以太网
                                Log.e(TAG, "eth up" );
                                pingResult = NetUtils.checkNetWork (dnsList,1,1);
                                Log.d (TAG, "accept: pingResult>>"+pingResult);
                                if (pingResult) {
                                    Log.e(TAG, "open Ethernet success！");
                                    contentView.setTextViewText(R.id.tvEthStatus, "当前以太网状态：重置成功");
                                    manager.notify(14, builder.build());
                                } else {
                                    contentView.setTextViewText(R.id.tvEthStatus, "当前以太网状态：重置失败");
                                    manager.notify(14, builder.build());
                                    Log.e(TAG, "open Ethernet failed！");
                                }
                            } else {
                                //如果以太网已经启用
                                contentView.setTextViewText(R.id.tvEthStatus, "当前以太网状态：正常");
                                manager.notify(14, builder.build());
                                Log.e(TAG, "Ethernet is successful");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            contentView.setTextViewText(R.id.tvEthStatus, "当前以太网状态：重置失败");
                            manager.notify(14, builder.build());
                        }
                    }
                });
    }

    private static void cancelJobAlarmSub() {
        if (sDisposable != null && !sDisposable.isDisposed()) {
            sDisposable.dispose();
            sDisposable = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy ( );
        cancelJobAlarmSub ();
    }
}
