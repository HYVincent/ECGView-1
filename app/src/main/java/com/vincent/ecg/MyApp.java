package com.vincent.ecg;

import android.app.Application;

import com.vincent.mylibrary.MyLibrary;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name ECGView
 * @page com.vincent.ecg
 * @class describe
 * @date 2018/3/9 18:03
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MyLibrary.init(this);
    }
}
