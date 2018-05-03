package com.vincent.ecg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.vincent.ecg.R;


/**
 * Created by Vincent on 2018/1/2.
 */

public class MyBG extends View {

    private Paint mPaint;
    //View的宽度
    private float view_width;
    //View的高度
    private float view_height;
    //基准线 在View的垂直居中的位置
    protected float baseLine;
    /**
     * 线的宽度
     */
    private float lineWidth = 1.0f;
    //小格子的宽度
    private int smallGrid = 20;
    //格子的线的颜色
    private int smallGridColor = Color.parseColor("#53bfed");
    private int baseLineColor = Color.parseColor("#FF0000");
    //View的背景颜色
    private int BGColor = Color.WHITE;

    private static final String TAG = MyBG.class.getSimpleName();

    public MyBG(Context context) {
        super(context);
        setBackgroundColor(BGColor);

    }

    public MyBG(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(BGColor);
        init(context, attrs);
    }

    public MyBG(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(BGColor);
        init(context, attrs);
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setAntiAlias(true);
        mPaint.setColor(smallGridColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        view_width = w;
        view_height = h;
        baseLine = view_height/2;//基准线位于view的一半位置
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawGridBackground(canvas);
        drawBaseLine(canvas);
    }

    private void drawBaseLine(Canvas canvas) {
        mPaint.setColor(baseLineColor);
        canvas.drawLine(0,baseLine,view_width,baseLine,mPaint);
    }


    /**
     * 绘制格子背景
     * @param canvas
     */
    private void drawGridBackground(Canvas canvas) {
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setAntiAlias(true);
        mPaint.setColor(smallGridColor);
        //画横线 上部分 表示为上半部分的横线根数
        float lineNumH =  view_height/2/smallGrid;
        for(int i = 1;i<lineNumH;i++){
            if(i % 5== 0){
                mPaint.setStrokeWidth(lineWidth * 2);
            }else {
                mPaint.setStrokeWidth(lineWidth);
            }
            canvas.drawLine(0,baseLine-i*smallGrid ,view_width,baseLine-i*smallGrid,mPaint);
        }
        /*画横线 下部分*/
        for (int i = 1;i<lineNumH;i++){
            if(i % 5== 0){
                mPaint.setStrokeWidth(lineWidth * 2);
            }else {
                mPaint.setStrokeWidth(lineWidth);
            }
            canvas.drawLine(0,baseLine+i*smallGrid ,view_width,baseLine+i*smallGrid,mPaint);
        }
        /*画竖线*/
        float lineNumZ = view_width/smallGrid;
        for (int i =0;i<lineNumZ;i++){
            if(i % 5== 0){
                mPaint.setStrokeWidth(lineWidth * 2);
            }else {
                mPaint.setStrokeWidth(lineWidth);
            }
            canvas.drawLine(smallGrid * i,0,smallGrid * i,view_height,mPaint);
        }
    }
}
