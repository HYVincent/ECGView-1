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
public class ECGViewBgAndHead extends View {

    private static final String TAG = ECGViewBgAndHead.class.getSimpleName();

    //View宽度
    private float viewWidth;
    //View高度
    private float viewHeight;
    //基线的位置
    private float baseLine;

    private int mColorData = Color.parseColor("#07aef5");
    private int mColorDataRed = Color.parseColor("#FF0000");

    //小格子的颜色
    private int bgColor = Color.parseColor("#53bfed");
    //小格子线条宽度
    private float bgLineWidth = 0.5f;
    //纵向有多少条线
    private int lineNumberZ;

    //基线画笔
    private Paint mBaseLine;
    //基线的宽度
    private float mBaseLineWidth = 4f;
    //基准线的颜色
    private int mBaseLineColor = Color.RED;
    //心电图的线的宽度
    private float ecgWidth = 2f;
    //小格子的宽度
    private float smallGridWidth = 20;

    //头部路径
    private Path mHeadPath;
    //头部画笔
    private Paint mHeadPaint;
    //头部线条宽度
    private float mHeadLineWidth = 8f;





    public ECGViewBgAndHead(Context context) {
        super(context);
        init();
    }

    public ECGViewBgAndHead(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ECGViewBgAndHead(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWidth = w;
        viewHeight = h;
        //基线的位置为View高度的一半
        baseLine = viewHeight/2;
        //计算偏移量 因为画背景是先画的基线，然后向两边衍生
        lineNumberZ = (int)(viewWidth/smallGridWidth);
        super.onSizeChanged(w, h, oldw, oldh);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        //画基准线
        drawBaseLine(canvas);
        //画头部
        drawHead(canvas);
    }

    /**
     * 画头部
     * @param canvas
     */
    private void drawHead(Canvas canvas) {
        mHeadPath.moveTo(smallGridWidth*5,viewHeight/2);
        for (float i = smallGridWidth * 5;i<smallGridWidth * 5 * 2;i++){
            if(i <= smallGridWidth * 5 + smallGridWidth * 5* 0.25){
                mHeadPath.lineTo(i,viewHeight/2);
            }else if(i > smallGridWidth * 5 + smallGridWidth * 5* 0.25 && i<smallGridWidth * 5 + smallGridWidth * 5* 0.75){
                mHeadPath.lineTo(i,viewHeight/2-smallGridWidth * 5 * 2);
            }else if( i>smallGridWidth * 5 + smallGridWidth * 5* 0.75 && i<smallGridWidth * 5 * 2){
                mHeadPath.lineTo(i,viewHeight/2);
            }
        }
        canvas.drawPath(mHeadPath,mHeadPaint);
    }



    /**
     * 绘制基线
     * @param canvas
     */
    private void drawBaseLine(Canvas canvas) {
        //画基线
        canvas.drawLine(0,baseLine,viewWidth,baseLine,mBaseLine);
    }

    /**
     * 绘制背景颜色
     * @param canvas
     */
    private void drawBg(Canvas canvas) {
        /*初始化背景画笔*/
        Paint mBgPaint = new Paint();
        //抗锯齿
        mBgPaint.setAntiAlias(true);
        /*背景颜色*/
        mBgPaint.setColor(bgColor);
        /*宽度*/
        mBgPaint.setStrokeWidth(bgLineWidth);

        //先画横向的线 画上部分
        for (float i = baseLine;i>0;i--){
            if(i % 5 == 0){
                mBgPaint.setStrokeWidth(bgLineWidth * 2);
            }else {
                mBgPaint.setStrokeWidth(bgLineWidth);
            }
            canvas.drawLine(0,baseLine - smallGridWidth * i,viewWidth,baseLine - smallGridWidth * i,mBgPaint);
        }
        //画下部分
        for (float i = 0;i<viewHeight;i++){
            if(i % 5 == 0){
                mBgPaint.setStrokeWidth(bgLineWidth * 2);
            }else {
                mBgPaint.setStrokeWidth(bgLineWidth);
            }
            canvas.drawLine(0,baseLine + smallGridWidth * i,viewWidth,baseLine + smallGridWidth * i,mBgPaint);
        }
        //画纵向的线 从零开始就可以了
        for(int i = 0; i<lineNumberZ;i++){
            if(i % 5 == 0){
                mBgPaint.setStrokeWidth(bgLineWidth * 2);
            }else {
                mBgPaint.setStrokeWidth(bgLineWidth);
            }
            canvas.drawLine(i * smallGridWidth,0,i * smallGridWidth,viewHeight,mBgPaint);
        }
        canvas.save();
    }

    private void init() {

        //初始化基线画笔
        mBaseLine = new Paint();
        mBaseLine.setStrokeWidth(mBaseLineWidth);
        mBaseLine.setAntiAlias(true);
        mBaseLine.setColor(mBaseLineColor);
        mBaseLine.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));


        mHeadPaint = new Paint();
        mHeadPaint.setColor(mColorData);
        mHeadPaint.setStrokeWidth(mHeadLineWidth);
        mHeadPaint.setStyle(Paint.Style.STROKE);
        mHeadPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        mHeadPath = new Path();
    }
}

