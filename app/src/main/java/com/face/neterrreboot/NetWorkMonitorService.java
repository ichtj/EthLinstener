package com.face.neterrreboot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Create on 2019/9/27
 * author chtj
 * 以太网网络异常时 每1分钟去ping两次网络后还不行 自动重启系统
 * 网络正常时 不做任何改变
 * 其他网络时 不做任何改变
 * 一小时内只能执行一次重启
 * 例如：
 * ① 时间段检测
 * 11:46;
 * 失败 执行一次重启;
 * 而后 这个时间段内即使网络不正常也不执行重启操作
 * 成功 不做任何操作;
 * 12:46;
 * ②这个时间段范围过后网络依然异常 才执行重启操作 然后继续执行①
 */
public class NetWorkMonitorService extends Service {
    public static final String TAG = "NetWorkMonitorService";
    public static Disposable sDisposable;
    //系统通知
    private NotificationManager manager = null;
    private Notification.Builder builder = null;
    //自定义的系统通知视图
    private RemoteViews contentView = null;
    //监听停止该服务的广播
    public static final String ACTION_CLOSE_ALL = "com.close.service.and.notification";
    static NetWorkMonitorService netWorkMonitorService;

    //单例
    public static NetWorkMonitorService getInstance() {
        if (netWorkMonitorService == null) {
            netWorkMonitorService = new NetWorkMonitorService();
        }
        return netWorkMonitorService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        NET_WORK_TYPE types = NetUtils.getConnNetType(BaseIotUtils.getContext());
        String getSaveTime = SPUtils.getString(KeyGlobalValue.KEY_PAST_TIME, "");
        manager = (NotificationManager) BaseIotUtils.getContext().getSystemService(NOTIFICATION_SERVICE);
        builder = new Notification.Builder(BaseIotUtils.getContext());
        contentView = new RemoteViews(getApplication().getPackageName(), R.layout.activity_notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, new Intent(ACTION_CLOSE_ALL), PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setTextViewText(R.id.tvNetType, "网络类型:" + types.name());
        contentView.setTextViewText(R.id.tvExeuTime, "已执行时间:加载中..");
        contentView.setTextViewText(R.id.tvRebootTime, "上次重启时间:" + (getSaveTime.equals("")?"未重启":getSaveTime));
        contentView.setOnClickPendingIntent(R.id.btnClose, pendingIntent);
        builder.setContent(contentView);
        builder.setSmallIcon(R.mipmap.net_wrok_reboot);  //小图标，在大图标右下角
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.net_wrok_reboot)); //大图标，没有设置时小图标就是大图标
        builder.setOngoing(true);//滑动不能清除
        builder.setAutoCancel(false);   //点击的时候消失
        manager.notify(11, builder.build());  //参数一为ID，用来区分不同APP的Notification

        //每次启动前前
        //2分钟后
        //执行每分钟循环检测网络
        sDisposable = Observable
                .interval(1, 1, TimeUnit.MINUTES)
                //取消任务时取消定时唤醒
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long count) throws Exception {
                        Log.d(TAG, "来了>>>>>");
                        //先加载一些基本参数 然后发出通知到状态栏
                        NET_WORK_TYPE type = NetUtils.getConnNetType(BaseIotUtils.getContext());
                        String getSaveDateTime = SPUtils.getString(KeyGlobalValue.KEY_PAST_TIME, "");
                        //获得保存的时间的具体年月日 时分秒|//获得当前的时间的具体年月日 时分秒
                        Calendar saveCalendar, nowCalendar = null;
                        long nowTime = -1, saveTime = -1, cTime = -1, sTime = -1, mTime = -1, hTime = -1, dTime = -1;
                        if (!getSaveDateTime.equals("")) {
                            //只有当前KeyGlobalValue.KEY_PAST_TIME的值不为“未重启”时
                            //才能执行上次重启的时间与当前时间的比较差
                            //（获得保存的时间|当前的时间）的具体年月日 时分秒
                            saveCalendar = DateUtil.getCalendarByTime(getSaveDateTime);
                            nowCalendar = DateUtil.getCurrentCalendar();
                            //先判断天数的相差是否一致 再比较时间差是否大于一个小时
                            nowTime = nowCalendar.getTimeInMillis();
                            saveTime = saveCalendar.getTimeInMillis();
                            cTime = nowTime - saveTime;
                            sTime = cTime / 1000;//时间差，单位：秒
                            mTime = sTime / 60;
                            hTime = mTime / 60;
                            dTime = hTime / 24;
                        }
                        Log.d(TAG, "saveDateTime=" + (getSaveDateTime.equals("") ? "null" : getSaveDateTime) + " 当前未执行过重启");
                        //获取执行的时间
                        long exeuTime = mTime % 60;
                        contentView.setTextViewText(R.id.tvRebootTime, "上次重启时间:" + (getSaveDateTime.equals("")?"未重启":getSaveDateTime));
                        contentView.setTextViewText(R.id.tvNetType, "网络类型:" + type.name());
                        contentView.setTextViewText(R.id.tvExeuTime, "已执行时间:" + (exeuTime == -1 ? 0 : exeuTime) + "分钟");
                        manager.notify(11, builder.build());  //参数一为ID，用来区分不同APP的Notification
                        if (dTime == 0) {
                            //为0天   那么就是当天
                            //如果当天的上一次重启的时间与当前的时间超过一个小时则重启
                            if (hTime % 24 >= 1) {
                                Log.d(TAG, "已经超过一小时现在开始执行重启的命令");
                                startSaveReboot();
                            } else {
                                //否则反之
                                Log.d(TAG, "一小时内已经重启过,保存的时间为：" + getSaveDateTime + ",已执行了：" + mTime % 60 + "分钟");
                            }
                        } else if (dTime > 0) {
                            //为>0天  超过了当天 重启
                            Log.d(TAG, "相差的天数为" + dTime + " 执行重启");
                            startSaveReboot();
                        } else {
                            //为-1天  没有对时间差进行对比 需要重启
                            //这里的情况一般是第一次启动
                            //如果当前为以太网
                            if (type == NET_WORK_TYPE.ETH) {
                                Log.d(TAG, "当前以太网已连接 下一步检测是否连接正常");
                                boolean isConnSuccessful1 = NetUtils.pingIp("223.5.5.5");
                                if (!isConnSuccessful1) {
                                    Log.e(TAG, "经过第一次网络连接测试 并未成功");
                                    boolean isConnSuccessful2 = NetUtils.pingIp("www.baidu.com");
                                    if (!isConnSuccessful2) {
                                        Log.e(TAG, "经过第二次网络连接测试 并未成功");
                                        startSaveReboot();
                                    } else {
                                        Log.e(TAG, "经过第二次网络连接测试 连接成功");
                                    }
                                } else {
                                    Log.e(TAG, "经过第一次网络连接测试 连接成功");
                                }
                            } else if (type == NET_WORK_TYPE.OTHER) {
                                Log.e(TAG, "现在连接的是其他网络,什么都不做");
                            } else {
                                /*executeReboot();*/
                                Log.e(TAG, "现在无任何网络连接,什么都不做");
                            }
                        }
                    }
                });
    }

    /**
     * START_STICKY
     * Service运行的进程被Android系统强制杀掉之后，
     * Android系统会将该Service依然设置为started状态（即运行状态），
     * 但是不再保存onStartCommand方法传入的intent对象，然后Android系统会尝试再次重新创建该Service，
     * 并执行onStartCommand回调方法，
     * 但是onStartCommand回调方法的Intent参数为null，也就是onStartCommand方法虽然会执行但是获取不到intent信息
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: START_STICKY");
        return START_STICKY;
    }

    /**
     * 执行保存后
     * 关机
     */
    public void startSaveReboot() {
        //保存当前的时间
        String saveDateTime = DateUtil.getCurrentTimeYMDHMS();
        Log.d(TAG, "当前执行关机的时间为：" + saveDateTime + ",下次将用于判断是否在一小时内执行过重启");
        SPUtils.putString(KeyGlobalValue.KEY_PAST_TIME, saveDateTime);
        executeReboot();
    }

    /**
     * 执行重启
     */
    public void executeReboot() {
        Log.e(TAG, "当前以太网连接异常 即将执行系统重启...");
        String command = "adb shell reboot";
        ShellUtils.CommandResult result = ShellUtils.execCommand(command, true);
        if (result.result == 0) {
            Log.e(TAG, "执行系统重启成功");
        } else {
            Log.e(TAG, "adb执行系统重启失败 已撤销保存的时间 请检查是否有权限？ errMeg=" + result.errorMsg);
            SPUtils.putString(KeyGlobalValue.KEY_PAST_TIME, "");
        }
    }


    /**
     * 关闭相关Servie内容
     */
    public void stopService() {
        stopSelf();
        if (sDisposable != null) {
            sDisposable.dispose();
        }
        //关闭通知
        if (manager != null) {
            manager.cancel(11);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (sDisposable != null) {
            sDisposable.dispose();
        }
        //关闭通知
        if (manager != null) {
            manager.cancel(11);
        }
    }
}
