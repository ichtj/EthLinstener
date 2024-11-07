package com.face.ethlinstener.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.face.ethlinstener.R;
import com.face.ethlinstener.ui.service.EthLinstenerService;
import com.face_chtj.base_iotutils.SPUtils;

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
        cycleIntervalPosition = SPUtils.getInt(EthLinstenerService.KEY_CYCLE_POSITION, 0);
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

        startService(new Intent (this, EthLinstenerService.class ));
    }

    public void startServiers(View view){
        SPUtils.putInt(EthLinstenerService.KEY_CYCLE, cycleIntervalNum);
        SPUtils.putInt(EthLinstenerService.KEY_CYCLE_POSITION, cycleIntervalPosition);
        stopService (new Intent ( this,EthLinstenerService.class ));
        startService(new Intent (this, EthLinstenerService.class ));
    }
}

