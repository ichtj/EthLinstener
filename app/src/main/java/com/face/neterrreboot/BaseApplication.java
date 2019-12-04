package com.face.neterrreboot;

import android.app.Application;

/**
 * Create on 2019/10/29
 * author chtj
 * desc
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BaseIotUtils.instance().create(this);
    }
}
