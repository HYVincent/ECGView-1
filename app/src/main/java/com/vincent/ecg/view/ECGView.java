package com.vincent.ecg.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.vincent.ecg.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name ECGView
 * @page com.vincent.ecg.view
 * @class describe
 * @date 2018/1/19 18:58
 */
public class ECGView extends View {

    private static final String TAG = ECGView.class.getSimpleName();

    //View宽度
    private float viewWidth;
    //View高度
    private float viewHeight;

    //ECG datas
    private List<EcgPointEntity> datas = new ArrayList<>();

    private int mColorData = Color.parseColor("#07aef5");
    private int mColorDataRed = Color.parseColor("#FF0000");

    //心电图的线的宽度
    private float ecgWidth = 2f;
    //小格子的宽度
    private float smallGridWidth = 20;
    //表示每个小格子的点的个数
    private int gridDotNumber = 5;
    //ECG path
    private Path path;

    /**
     * 点的 125个数据 = 5个大格子 = 25个小格子,一个小格子有5个数据
     * dotWidth 表示一个点在X轴的宽度,确切的说是两个点的间距
     */
    private float dotWidth = smallGridWidth/gridDotNumber;
    //x轴原点坐标
    private float xori;
    //画时间轴的横线
    private Path linePath;
    private Paint mLinePaint;
    //画时间轴下面的时间文字
    private Paint mTimePaint;
    //表示屏幕上最多能有多少个点
    private float maxDot = 0.0f;


    public ECGView(Context context) {
        super(context);
        init();
    }

    public ECGView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ECGView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWidth = w;
        viewHeight = h;
        xori = 0.0f;
        super.onSizeChanged(w, h, oldw, oldh);
        maxDot = viewWidth/(smallGridWidth/gridDotNumber);
    }

    /**
     * 获取控件宽度上能显示的最大的点
     * @return
     */
    public float getMaxDot() {
        return maxDot;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawData(canvas);
    }

    private Paint mPaint;


    //绘制数据
    private void drawData(Canvas canvas) {
        //清除路径
        path.reset();
        linePath.reset();
        //此处 xori设置为0 ，未用上
        int iXor = 0;
        path.moveTo(0,valuesToY(datas.get(0).getData()));
        path.lineTo(0,valuesToY(datas.get(0).getData()));


        //不绘制头部
        path.moveTo(0,valuesToY(datas.get(0).getData()));
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
                canvas.drawPath(path,mPaint);
                path.reset();
                path.moveTo((i-1) * smallGridWidth /gridDotNumber,valuesToY(datas.get(i-1).getData()));
                path.lineTo(i * smallGridWidth /gridDotNumber,valuesToY(entity.getData()));
                if(entity.isRed()){
                    mPaint.setColor(mColorDataRed);
                }else {
                    mPaint.setColor(mColorData);
                }
            }else {
                //连续的点，颜色值是一样的，或者都是红色，或者都是蓝色
                path.lineTo(i * smallGridWidth /gridDotNumber,valuesToY(entity.getData()));
                if(entity.isRed()){
                    mPaint.setColor(mColorDataRed);
                }else {
                    mPaint.setColor(mColorData);
                }
            }
        }
        canvas.drawPath(path,mPaint);
    }


    /**
     * 这个的意思是两个大格子表示的值是200  200 = 2个大格子的高度 = 2 * 1个大格子的高度 = 2 * 5 个小格子的高度 一个小格子表示的值为20
     * 1 小格的数据 表示为20
     * @param data
     * @return
     */
    private float valuesToY(Integer data){
//        return  (-1.0f) * data /(100.0f) * (20.0f) + baseLine;
        return  data * (-1.0f) + viewHeight/2;
    }





    private void init() {

        //初始化心电图画笔
        mPaint = new Paint();
        mPaint.setStrokeWidth(ecgWidth);
        mPaint.setColor(Color.parseColor("#07aef5"));
        mPaint.setAntiAlias(true);

        //设置样式
        mPaint.setStyle(Paint.Style.STROKE);

        path = new Path();

        //这是下面的线
        linePath = new Path();
        //初始化时间文字画笔
        mTimePaint = new Paint();
        mTimePaint.setColor(mColorDataRed);
        mTimePaint.setAntiAlias(true);
        mTimePaint.setStyle(Paint.Style.STROKE);
        mTimePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        mTimePaint.setTextSize(48);

        mLinePaint = new Paint();
        mLinePaint.setColor(mColorDataRed);
        mLinePaint.setStrokeWidth(4f);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
    }
    /**
     * 绘制数据
     * @param datas
     */
    public void setDatas(List<EcgPointEntity> datas) {
        this.datas = datas;
        invalidate();
    }



}

