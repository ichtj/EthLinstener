package com.face.neterrreboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Create on 2019/9/27
 * author chtj
 */
public class NetWorkReceiver extends BroadcastReceiver {
    public static final String TAG=NetWorkReceiver.class.getSimpleName();
    public static String ACTION_BOOT="android.intent.action.BOOT_COMPLETED";

    public NetWorkReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: "+intent.getAction());
        if (intent.getAction().equals(ACTION_BOOT)) {
            Intent i = new Intent(context, NetWorkMonitorService.class);
            Log.d(TAG,"即将执行NetErrReboot app程序");
            context.startService(i);
        }else if(intent.getAction().equals(NetWorkMonitorService.ACTION_CLOSE_ALL)){
            NetWorkMonitorService.getInstance().stopService();
            context.stopService(new Intent(context,NetWorkMonitorService.class));
        }
    }
}
