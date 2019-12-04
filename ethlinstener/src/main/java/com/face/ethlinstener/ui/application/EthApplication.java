package com.face.ethlinstener.ui.application;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;
import com.chtj.base_iotutils.KLog;
import com.chtj.base_iotutils.keepservice.BaseIotUtils;


/**
 * Create on 2019/11/7
 * author chtj
 * desc
 */
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
