package com.face.neterrreboot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;


/**
 * 网络状态类型
 */
public class NetUtils {
    public static final String TAG = "NetUtils";

    public static enum NetType {
        WIFI, CMNET, CMWAP, NONE, ETH
    }

    public static enum NetTypeDetail {
        NET_STATE_2G_CONNECTED, NET_STATE_3G_CONNECTED, NET_STATE_4G_CONNECTED, NET_STATE_UNKNOWN
    }

    //Ping
    public static boolean pingIp(String urlAddr) {
        boolean isConnect = false;
        try {
            String command = "ping -c 2 -w 5 " + urlAddr;
            //KLog.e(TAG,"正在对地址:"+urlAddr+"进行访问,执行的命令为："+command);
            //代表ping 2 次 超时时间为5秒
            Process p = Runtime.getRuntime().exec(command);//ping2次
            int status = p.waitFor();
            if (status == 0) {
                isConnect = true;
                //代表成功
            } else {
                //代表失败
                isConnect = false;
            }
        } catch (Exception e) {
            isConnect = false;
            Log.e(TAG, e.getMessage());
        }
        return isConnect;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    public static NetType getAPNType(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return NetType.NONE;
        }
        int nType = networkInfo.getType();

        if (nType == ConnectivityManager.TYPE_MOBILE) {
            if (networkInfo.getExtraInfo().toLowerCase(Locale.getDefault()).equals("cmnet")) {
                return NetType.CMNET;
            } else {
                return NetType.CMWAP;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            return NetType.WIFI;
        }
        return NetType.NONE;
    }

    /**
     * 网络判断
     *
     * @param context
     * @return
     */
    public static NET_WORK_TYPE isIntenetConnected(Context context)  {
        NET_WORK_TYPE netType = NET_WORK_TYPE.OTHER;
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mInternetNetWorkInfo != null && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable()) {
            int type = mInternetNetWorkInfo.getType();
            if (type == ConnectivityManager.TYPE_ETHERNET) {
                netType = NET_WORK_TYPE.ETH;
            } else {
                netType = NET_WORK_TYPE.OTHER;
            }
        }else{
            netType = NET_WORK_TYPE.NONE;
        }
        return netType;
    }

    /**
     * 判断当前网络是否为4G
     *
     * @param context
     * @return 是否正确
     */
    public static boolean isNet4GConnted(Context context) {
        boolean isMobileNet, isMobile4Gnet = false;
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mMobileNetworkInfo != null) {
            isMobileNet = mMobileNetworkInfo.isAvailable();
        } else {
            isMobileNet = false;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephonyManager.getNetworkType();
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                //return NetTypeDetail.NET_STATE_2G_CONNECTED;
                isMobile4Gnet = false;
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                //return NetTypeDetail.NET_STATE_3G_CONNECTED;
                isMobile4Gnet = false;
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                //return NetTypeDetail.NET_STATE_4G_CONNECTED;
                isMobile4Gnet = true;
                break;
            default:
                //return NetTypeDetail.NET_STATE_UNKNOWN;
                isMobile4Gnet = false;
                break;
        }
        return (isMobileNet && isMobile4Gnet);
    }

    /**
     * 开启关闭移动网络
     *
     * @param context
     * @param isMobileDataEnabled
     */
    public static void setTurnOffNetWork(Context context, boolean isMobileDataEnabled) {
        TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(telephonyService, isMobileDataEnabled);
            }
        } catch (Exception e) {
            Log.v(TAG, "Error setting" + ((InvocationTargetException) e).getTargetException() + telephonyService);
        }
    }

    public boolean getMobileDataState(Context cxt) {
        TelephonyManager telephonyService = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
            if (null != getMobileDataEnabledMethod) {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
                return mobileDataEnabled;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting" + ((InvocationTargetException) e).getTargetException() + telephonyService);
        }

        return false;
    }

}
