package com.face.ethlinstener.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chtj.base_iotutils.SPUtils;
import com.chtj.base_iotutils.keepservice.BaseIotUtils;
import com.face.ethlinstener.R;
import com.face.ethlinstener.ui.service.EthLinstenerService;

import java.util.Arrays;
/**
 * Created by goldze on 2018/6/21.
 */

public class EthLinstenerActivity extends AppCompatActivity {
    String[] cycleIntervalInfo = null;//网络检测间隔时间
    Spinner spCycleInterval;
    public int cycleIntervalPosition =0;//网络检查间隔时间 对应的下标
    public int cycleIntervalNum = 1;//网络检查间隔的时间 分钟
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethlinstener);
        cycleIntervalInfo=getResources().getStringArray(R.array.ethlinstener_cycleInterval);
        spCycleInterval=findViewById(R.id.sp_cycle_interval);
        TestArrayAdapter testArrayAdapter1 = new TestArrayAdapter(this, Arrays.asList(cycleIntervalInfo));
        spCycleInterval.setAdapter(testArrayAdapter1);
        cycleIntervalPosition = SPUtils.getInt("cycleIntervalPosition", 0);
        spCycleInterval.setSelection(cycleIntervalPosition);

        //循环间隔选择
        spCycleInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cycleIntervalNum=Integer.parseInt(cycleIntervalInfo[position]);
                cycleIntervalPosition=position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //①初始化后台保活Service
        BaseIotUtils.initSerice(EthLinstenerService.class, BaseIotUtils.DEFAULT_WAKE_UP_INTERVAL);
        EthLinstenerService.sShouldStopService = false;
        BaseIotUtils.startServiceMayBind(EthLinstenerService.class);
    }

    public void startServiers(View view){
        SPUtils.putInt("cycleInterval", cycleIntervalNum);
        SPUtils.putInt("cycleIntervalPosition", cycleIntervalPosition);
        EthLinstenerService.stopService();
        //①初始化后台保活Service
        BaseIotUtils.initSerice(EthLinstenerService.class, BaseIotUtils.DEFAULT_WAKE_UP_INTERVAL);
        EthLinstenerService.sShouldStopService = false;
        BaseIotUtils.startServiceMayBind(EthLinstenerService.class);
    }
}

