package com.face.neterrreboot;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Create on 2019/9/27
 * author chtj
 * 网络异常时 每1分钟去ping两次网络后还不行 自动重启系统
 * 网络正常时 不做任何改变
 */
public class NetWorkMonitorService extends Service {
    public static final String TAG = "NetWorkMonitorService";

    public static Disposable sDisposable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //每次启动前前
        //2分钟后执行每分钟循环检测网络
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
                        Log.e(TAG, "来了>>>>>");
                        NET_WORK_TYPE type = NetUtils.isIntenetConnected(NetWorkMonitorService.this);
                        //如果当前为以太网
                        if (type == NET_WORK_TYPE.ETH) {
                            Log.e(TAG, "当前以太网已连接 下一步检测是否连接正常");
                            boolean isConnSuccessful1 = NetUtils.pingIp("223.5.5.5");
                            if (!isConnSuccessful1) {
                                Log.e(TAG, "经过第一次网络连接测试 并未成功");
                                boolean isConnSuccessful2 = NetUtils.pingIp("www.baidu.com");
                                if (!isConnSuccessful2) {
                                    Log.e(TAG, "经过第二次网络连接测试 并未成功");
                                    executeReboot();
                                } else {
                                    Log.e(TAG, "经过第二次网络连接测试 连接成功");
                                }
                            } else {
                                Log.e(TAG, "经过第一次网络连接测试 连接成功");
                            }
                        } else if (type == NET_WORK_TYPE.OTHER) {
                            Log.e(TAG,"现在连接的是其他网络");
                        } else {
                            executeReboot();
                        }
                    }
                });
    }

    /**
     * 执行重启
     */
    public void executeReboot() {
        Log.e(TAG, "当前以太网连接异常 即将执行系统重启...");
        String command = "adb shell reboot";
        ShellUtils.CommandResult result = ShellUtils.execCommand(command, false);
        if (result.result == 0) {
            Log.e(TAG, "执行系统重启成功");
        } else {
            Log.e(TAG, "执行系统重启失败 errMeg=" + result.errorMsg);
        }
    }
}
