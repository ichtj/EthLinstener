package com.face.ethlinstener.ui.application;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.face_chtj.base_iotutils.BaseIotUtils;
import com.face_chtj.base_iotutils.KLog;


public class EthApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        KLog.init(true);
        BaseIotUtils.instance().create(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
