package com.face.ethlinstener.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.face.ethlinstener.ui.service.EthLinstenerService;

/**
 * 开机启动
 */
public class MyReceiver extends BroadcastReceiver {
    public static final String TAG= MyReceiver.class.getSimpleName();
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.e(TAG,"EthLinstenerService start");
            //①初始化后台保活Service
            context.startService(new Intent (context, EthLinstenerService.class ));
        }
    }
}
