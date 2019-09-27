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
    public NetWorkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, NetWorkMonitorService.class);
            Log.e(TAG,"即将执行NetErrReboot app程序");
            context.startService(i);
        }
    }
}
