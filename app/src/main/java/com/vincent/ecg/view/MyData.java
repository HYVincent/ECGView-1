package com.vincent.ecg.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;



import java.util.ArrayList;
import java.util.List;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name CustomView
 * @page com.example.vincent.customview
 * @class describe
 * @date 2018/1/2 18:08
 */

public class MyData extends View {

    private static final String TAG = MyData.class.getSimpleName();
    private List<EcgPointEntity> datas = new ArrayList<>();
    //正常颜色的数据画笔
    private Paint mPaint;
    //标红数据画笔
    private Path mPath;
    private int mColorData = Color.parseColor("#07aef5");
    private int mColorDataRed = Color.parseColor("#FF0000");
    private int view_width;
    private int view_height;
    //心电图的宽度
    private float line_width = 4f;
    //基线的位置
    private float baseLine;
    //小格子的宽度
    private float smailGridWith = 20f;
    private float bigGridWidth = smailGridWith * 5;
    //表示每个小格子放五个数据
    private int dataNumber = 5;
    //屏幕能够显示的所有的点的个数 注意这个值设置为int会导致这个数值不准确
    private float maxSize = 0;
    //是否绘制头部 凸状物 true 画  false 不画
    private boolean isDrawHead = true;
    //绘制头部画笔
    private Paint mHeadPaint;
    //头部路径
    private Path mHeadPath;
    //头部的颜色
    private int mColorHead = Color.parseColor("#07aef5");
    //头部的宽度
    private float headPathWidth = 8f;
    //头部的总宽度为两个大格子
    private float headWidth = bigGridWidth * 2;

    private float mark = -1.0f;
    private boolean orientation = false;

    //暂停绘制 true暂停  false绘制
    private boolean pause = false;

    /** 数据倒置
     * true -1.0f false 1.0f
     */
    public void setMarkOrder(){
        if(orientation){
            orientation = false;
            mark = -1.0f;
        }else {
            orientation = true;
            mark = 1.0f;
        }
    }

    public float getMaxSize() {
        if(isDrawHead){
            maxSize =  (view_width*(1.0f)-headWidth) / (smailGridWith / (dataNumber*1.0f));
        }else {
            maxSize =  view_width*(1.0f) / (smailGridWith / (dataNumber*1.0f));
        }
        if(maxSize <0){
            maxSize = maxSize * (-1);
        }
        return maxSize;
    }

    public MyData(Context context) {
        super(context);
        init();
        setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent));
    }
    public MyData(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent));
    }

    /**
     * 这个方法可以每隔格子放的数据数量
     * @param dataNumber
     */
    public void setDataNumber(int dataNumber) {
        this.dataNumber = dataNumber;
        onSizeChanged(view_width,view_height,view_width,view_height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //控件宽度
        view_width = w;
        //控件高度
        view_height = h;
        //基准线的位置
        baseLine = view_height /2;
        //125个数据占用为5个大格子，5个大格子有25个小格子，所以每个小格子放5个数据
        if(isDrawHead){
            maxSize =  (view_width*(1.0f)-headWidth) / (smailGridWith / (dataNumber*1.0f));
        }else {
            maxSize =  view_width*(1.0f) / (smailGridWith / (dataNumber*1.0f));
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画头部
        drawHead(canvas);
        //画数据
        drawData(canvas);
    }

    /**
     * 设置是否绘制头部
     * @param drawHead true 画 false 不画
     */
    public void setDrawHead(boolean drawHead) {
        isDrawHead = drawHead;
    }

    /**
     * 绘制数据
     * @param canvas
     */
    private void drawData(Canvas canvas) {
        //清除路径
        mPath.reset();
        if(datas != null && datas.size()>0){
            if(isDrawHead){
                //绘制头部
                switch ((int) headStart){
                    case 0:
                        dataNumber = 5;
                        break;
                    case 100:
                        dataNumber = 10;
                        break;
                    case 150:
                        dataNumber = 20;
                        break;
                    case 175:
                        dataNumber = 40;
                        break;
                    default:
                        dataNumber = 5;
                        break;
                }
                mPath.moveTo(headWidth,change(datas.get(0).getData()));
                mPath.lineTo(headWidth,change(datas.get(0).getData()));
                //1 s更新125个数据，125个数据占用为5个大格(25个小格)
                //1个小格子为5个数据 1小格的宽度为20 1个数据的宽度是20/5
                /*for (int i = 0;i<datas.size();i++){
                    if(datas.get(i).isRed()){
                        mPaint.setColor(mColorData);
                    }else {
                        mPaint.setColor(mColorDataRed);
                    }
                    mPath.lineTo(i * smailGridWith /dataNumber+headWidth,change(datas.get(i).getData()));
                }
                canvas.drawPath(mPath,mPaint);*/
                for (int i = 1;i<datas.size();i++) {
                    EcgPointEntity entity = datas.get(i);
                    EcgPointEntity lastEntity = datas.get(i-1);
                    if(lastEntity.isRed() != entity.isRed()){
                        //当前颜色值和上一个颜色值不一样
//                        Log.d(TAG, "drawData: 当前颜色值和上一个颜色值不一样 " + i);
                        canvas.drawPath(mPath,mPaint);
                        mPath.reset();
                        mPath.moveTo((i-1) * smailGridWith /dataNumber+headWidth,change(datas.get(i-1).getData()));
                        mPath.lineTo(i * smailGridWith /dataNumber+headWidth,change(entity.getData()));
                        if(entity.isRed()){
                            mPaint.setColor(mColorDataRed);
                        }else {
                            mPaint.setColor(mColorData);
                        }
                    }else {
                        //连续的点，颜色值是一样的，或者都是红色，或者都是蓝色
                        mPath.lineTo(i * smailGridWith /dataNumber+headWidth,change(entity.getData()));
                        if(entity.isRed()){
                            mPaint.setColor(mColorDataRed);
                        }else {
                            mPaint.setColor(mColorData);
                        }
                    }
                }
                canvas.drawPath(mPath,mPaint);
            }else {
                //不绘制头部
                mPath.moveTo(0,change(datas.get(0).getData()));
                //1 s更新125个数据，125个数据占用为5个大格(25个小格)
                //1个小格子为5个数据  1个数据为16/5小格 1小格的宽度为16 1个数据的宽度是16/5
               /* for (int i = 0;i<datas.size();i++){
                    if(datas.get(i).isRed()){
                        mPaint.setColor(mColorData);
                    }else {
                        mPaint.setColor(mColorDataRed);
                    }
                    mPath.lineTo(i * smailGridWith /dataNumber,change(datas.get(i).getData()));
                }
                canvas.drawPath(mPath,mPaint);*/
                for (int i = 1;i<datas.size();i++) {
                    EcgPointEntity entity = datas.get(i);
                    EcgPointEntity lastEntity = datas.get(i-1);
                    if(lastEntity.isRed() != entity.isRed()){
                        //当前颜色值和上一个颜色值不一样
//                        Log.d(TAG, "drawData: 当前颜色值和上一个颜色值不一样 " + i);
                        canvas.drawPath(mPath,mPaint);
                        mPath.reset();
                        mPath.moveTo((i-1) * smailGridWith /dataNumber,change(datas.get(i-1).getData()));
                        mPath.lineTo(i * smailGridWith /dataNumber,change(entity.getData()));
                        if(entity.isRed()){
                            mPaint.setColor(mColorDataRed);
                        }else {
                            mPaint.setColor(mColorData);
                        }
                    }else {
                        //连续的点，颜色值是一样的，或者都是红色，或者都是蓝色
                        mPath.lineTo(i * smailGridWith /dataNumber,change(entity.getData()));
                        if(entity.isRed()){
                            mPaint.setColor(mColorDataRed);
                        }else {
                            mPaint.setColor(mColorData);
                        }
                    }
                }
                canvas.drawPath(mPath,mPaint);
            }
        }
    }

    //头部起点位置 这个值有四个，0,100(smailGridWith * 5),150(smailGridWidth *5* 1.5),175(smailGridWidth * 5 * 1.75)
    //这里走速控制
    private float headStart = 0;
    //四个值 5 10 20 30
    private float addValues = 10;

    /**
     * 设置增益 这个值调节Y值 并且影响头部高度
     * @param addValues
     */
    public void setAddValues(float addValues) {
        this.addValues = addValues;
        invalidate();
    }

    /**
     * 设置头部的起点位置
     */
    public void setHeadStart(float start) {
        this.headStart = start;
        Log.d(TAG, "setHeadStart: "+headStart);
        invalidate();
    }

    /**
     * 绘制头部
     * @param canvas
     */
    private void drawHead(Canvas canvas) {
        //控制是否画头部
        if(isDrawHead){
            float value = 0;
            if(addValues == 5){
                value =  bigGridWidth * 1;
            }else if(addValues == 10){
                value =  bigGridWidth * 2;
            }else if ( addValues == 20){
                value =  bigGridWidth * 4;
            }else if(addValues == 30){
                value = bigGridWidth * 6;
            }
            mHeadPath.reset();
            //移动到基线的位置
            mHeadPath.moveTo(headStart,baseLine);
            for (float i = headStart;i<headWidth;i++){
                if(i > headStart &&i <(headWidth - headStart) * 0.25f + headStart){
                    mHeadPath.lineTo(i,baseLine);
                }else if(i > (headWidth - headStart) * 0.25f + headStart && i < headStart +(headWidth - headStart) * 0.75f){
                    mHeadPath.lineTo(i,baseLine - value);
                }else if(i > headStart +  (headWidth - headStart) * 0.75f && i<headWidth){
                    mHeadPath.lineTo(i,baseLine);
                }
            }
            canvas.drawPath(mHeadPath,mHeadPaint);
        }
    }

    /**
     * 添加数据
     * @param data
     */
    public void addData(EcgPointEntity data){
        maxSize = getMaxSize();
//        Log.d(TAG, "addData: max size = "+getMaxSize());
        datas.add(data);
        if(datas.size() > maxSize){
            //如果这个集合大于maxSize（即表示屏幕上所能显示的点的个数）个点，那么就把第一个点移除
//            Log.d(TAG, "addData: "+maxSize);
            datas.remove(0);
        }
        if(pause){
            return;
        }
        //这个方法会重新调用onDraw()方法 这个方法用在主线程
        invalidate();
        //这个方法用在异步线程
//        requestLayout();
    }


    /**
     * 是否暂停绘制
     * @param pause
     */
    public void onPauseDraw(boolean pause) {
        this.pause = pause;
    }

    public void addAllData(List<EcgPointEntity> datas){
        this.datas.addAll(datas);
        invalidate();
    }

    /**
     * 获取当前数据
     * @return
     */
    public List<EcgPointEntity> getDatas() {
        return datas;
    }

    /**
     * 把数据转化为对应的坐标  1大格表示的数据值为0.5毫伏，1毫伏= 200(数据) 1大格表示的数据 = 0.5 *200 1小格表示的数据 = 0.5*200/5 = 20
     * 1 小格的数据 表示为20 1小格的高度为16
     * @param data
     * @return
     */
    private float change(Integer data){
        float value = 0;
        if(addValues == 5){
            value =  (mark) * data/2+ baseLine;
        }else if(addValues == 10){
            value =  (mark) * data+ baseLine;
        }else if ( addValues == 20){
            value =  (mark) * data * 2+ baseLine;
        }else if(addValues == 30){
            value =  (mark) * data * 4+ baseLine;
        }
        return value;
    }


    /**
     * 设置头部数据颜色值
     * @param mColorData
     */
    public void setmColorData(int mColorData) {
        this.mColorData = mColorData;
        if(mPaint != null){
            mPaint.setColor(mColorData);
        }
    }

    private void init() {
        //心电图画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(line_width);

        //标红数据画笔
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColorDataRed);
        mPaint.setStrokeWidth(line_width);

        //心电图路径
        mPath = new Path();
        mHeadPath = new Path();

        //画心电图
        mHeadPaint = new Paint();
        mHeadPaint.setStyle(Paint.Style.STROKE);
        mHeadPaint.setAntiAlias(true);
        mHeadPaint.setColor(mColorHead);
        mHeadPaint.setStrokeWidth(headPathWidth);
    }

}
