package com.vincent.ecg;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.vincent.ecg.utils.DateUtils;
import com.vincent.ecg.utils.ReadAssetsFileUtils;
import com.vincent.ecg.utils.TimeUtils;
import com.vincent.ecg.view.ECGView;
import com.vincent.ecg.view.ECGView2;
import com.vincent.ecg.view.ECGView3;
import com.vincent.ecg.view.EcgData;
import com.vincent.ecg.view.EcgPointEntity;
import com.vincent.ecg.view.MyData;
import com.vincent.ecg.view.MyDataAll;
import com.vincent.ecg.view.MyEcgDataHasHead;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    public static List<EcgPointEntity> datas = new ArrayList<>();
    private static final String TAG = MainActivity.class.getSimpleName();
    private MyEcgDataHasHead myData;
    private int startValue = 0;
    private int getCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        changeData(ReadAssetsFileUtils.readAssetsTxt(this,"StarCareData"));
        changeData(ReadAssetsFileUtils.readAssetsTxt(this,"ecg_data"));
        myData = findViewById(R.id.ecgView);

        myData.changeModel();

        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this,TestActivity.class));
                Log.d(TAG, "start timer");
                myData.onDrawDone=true;
                myData.initDrawBuffer(myData.getWidth(),myData.getHeight());
                startTime(startValue, 0, 8, 1, new TimeUtils.TimeListener() {
                    @Override
                    public void doAction(final int index) {
//                        Log.d(TAG, "doAction: "+datas.size()+" index = "+index);
                        if(index == datas.size()){
                            cancelTimeTask();
                            startValue = 0;
                        }

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Log.d(TAG, "run: data-->"+datas.get(index).getData());
                               if(index == datas.size()){
                                   return;
                               }
                               myData.addData(datas.get(index));
                               if ((myData.onDrawDone==true) & (myData.datas.size()>1))
                               {
                                   myData.onDrawDone=false;
                                   myData.invalidView();
                               }
                           }
                       });
                    }
                });
            }
        });
    }

    private boolean isRed = false;

    private void changeData(String starCareData) {
        String num = "";
        try {
            //        String[] strDatas = starCareData.split("\n");
            String[] strDatas = starCareData.split(",");
            long time = System.currentTimeMillis();
            for (int i = 0;i<strDatas.length;i++){
                Log.e("strdata lenght","str data length"+strDatas.length);
                EcgPointEntity ecgData = new EcgPointEntity();
                num = strDatas[i];
                if(num.contains("[")){
                    num = num.substring(1);
                }
                if(num.contains("]")){
                    num = num.substring(0,num.length()-1);
                }
                if(TextUtils.equals(num,"0")){
                    ecgData.setData(0);
                }else {
                    Log.d(TAG, "changeData: "+num);
                    ecgData.setData(Integer.valueOf(num));
                }
                if(i % 125 == 0){
                    if(isRed){
                        isRed = false;
                    }else {
                        isRed = true;
                    }
                }
                if(i % 125 == 0){
                    //变化
                    time += 1000L;
                }
//            Log.d(TAG, "changeData: time = "+ DateUtils.getDateString(DateUtils.DATE_FORMAT_ALL,time));
                ecgData.setDate(new Date(time));
                ecgData.setRed(isRed);
                datas.add(ecgData);
            }
        }catch (Exception e){
            Log.d(TAG, "changeData: ....数据异常+"+num);
            e.printStackTrace();
        }
    }

    private ScheduledExecutorService scheduledExecutorService;

    public void startTime(int initValues,long delayTime,long interval,  int threadNum, final TimeUtils.TimeListener timeListener) {
        try {
            final AtomicInteger atomicInteger = new AtomicInteger(initValues);
            if (threadNum == 0) {
                threadNum = 1;
            }
            scheduledExecutorService = new ScheduledThreadPoolExecutor(threadNum);
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    timeListener.doAction(atomicInteger.incrementAndGet());
//                timeListener.doAction(1);
                }
            }, delayTime, interval, TimeUnit.MILLISECONDS);
        }catch (Exception e){

        }
    }

    public void cancelTimeTask(){
        if(scheduledExecutorService!= null){
            scheduledExecutorService.shutdownNow();
        }
    }

}
