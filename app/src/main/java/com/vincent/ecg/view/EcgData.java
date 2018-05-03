package com.vincent.ecg.view;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name ECGView
 * @page com.vincent.ecg.view
 * @class describe
 * @date 2018/1/30 11:48
 */

public class EcgData {
    //心电图数据
    private Integer data;
    //是否为红色
    private boolean isRed;

    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
        this.data = data;
    }

    public boolean isRed() {
        return isRed;
    }

    public void setRed(boolean red) {
        isRed = red;
    }
}
