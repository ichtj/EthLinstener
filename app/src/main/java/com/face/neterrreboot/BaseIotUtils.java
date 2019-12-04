package com.face.neterrreboot;

import android.app.Application;
import android.content.Context;

/**
 * Create on 2019/10/29
 * author chtj
 * desc
 */
public class BaseIotUtils  {
    private static BaseIotUtils sInstance;
    private static Context sApp;
    //单例模式
    public static BaseIotUtils instance() {
        if (sInstance == null) {
            synchronized (BaseIotUtils.class) {
                if (sInstance == null) {
                    sInstance = new BaseIotUtils();
                }
            }
        }
        return sInstance;
    }
    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (sApp != null) {
            return sApp;
        }
        throw new NullPointerException("should be initialized in application");
    }

    public void create(Application application){
        BaseIotUtils.sApp= application.getApplicationContext();
    }

}
