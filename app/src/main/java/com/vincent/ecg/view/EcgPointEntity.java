package com.vincent.ecg.view;

import java.util.Date;
import java.util.List;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name StartKangMedical_Android
 * @page com.toncentsoft.starkangmedical_android.entity
 * @class describe
 * @date 2018/2/1 15:32
 */

public class EcgPointEntity implements Cloneable{
    //记录一个时间，这个时间是数据产生的时间
    private long time;

    //设置为滤波后的数据
    private Integer data;

    //首页显示的数据
    private Integer xinLv;

    private boolean isRed;
    //是否监测到pace
    private boolean isPace;
    //qrs 打点的坐标
    private List<Integer> qrsList;
    // 是否打点
    private boolean isQrs;
    //是否改变心率值
    private boolean isChange;
    //导联状态 0:脱落   1:正常
    private int linkStatus;
    //时间戳
    private Date date;

    public boolean isQrs() {
        return isQrs;
    }

    public void setQrs(boolean qrs) {
        isQrs = qrs;
    }

    public List<Integer> getQrsList() {
        return qrsList;
    }

    public void setQrsList(List<Integer> qrsList) {
        this.qrsList = qrsList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getXinLv() {
        return xinLv;
    }

    public void setXinLv(Integer xinLv) {
        this.xinLv = xinLv;
    }

    public int getLinkStatus() {
        return linkStatus;
    }

    public void setLinkStatus(int linkStatus) {
        this.linkStatus = linkStatus;
    }

    public boolean isChange() {
        return isChange;
    }

    public void setChange(boolean change) {
        isChange = change;
    }

    public boolean isPace() {
        return isPace;
    }

    public void setPace(boolean pace) {
        isPace = pace;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

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

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
