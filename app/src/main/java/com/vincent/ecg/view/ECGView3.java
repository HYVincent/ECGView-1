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
public class ECGView3 extends View {

    private static final String TAG = ECGView3.class.getSimpleName();

    //View宽度
    private float viewWidth;
    //View高度
    private float viewHeight;
    //基线的位置
    private float baseLine;
    //ECG datas
    private List<EcgPointEntity> datas = new ArrayList<>();

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
    //表示每个小格子的点的个数
    private int gridDotNumber = 5;
    //ECG path
    private Path path;
    /**
     * 是否绘制头部 true 绘制  false 不绘制
     */
    private boolean isDrawableHead = true;

    /**
     * 点的 125个数据 = 5个大格子 = 25个小格子,一个小格子有5个数据
     * dotWidth 表示一个点在X轴的宽度,确切的说是两个点的间距
     */
    private float dotWidth = smallGridWidth/gridDotNumber;
    //x轴原点坐标
    private float xori;
    //滑动查看时，x坐标的变化
    private float x_change ;
    private float x_changed;
    //X轴的最大偏移量
    private float offset_x_max;
    //手指触碰屏幕的x坐标
    private float startX;
    //画时间轴的横线
    private Path linePath;
    private Paint mLinePaint;
    //画时间轴下面的时间文字
    private Paint mTimePaint;
    //表示屏幕上最多能有多少个点
    private float maxDot = 0.0f;
    //头部路径
    private Path mHeadPath;
    //头部画笔
    private Paint mHeadPaint;
    //头部线条宽度
    private float mHeadLineWidth = 8f;

    private MoveViewListener moveViewListener;



    public ECGView3(Context context) {
        super(context);
        init(context);
    }

    public void setMoveViewListener(MoveViewListener moveViewListener) {
        this.moveViewListener = moveViewListener;
    }

    public ECGView3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ECGView3(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWidth = w;
        viewHeight = h;
        //基线的位置为View高度的一半
        baseLine = viewHeight/2;
        xori = 0.0f;
        x_change = 0.0f;
        x_changed = 0.0f;
        //计算偏移量 因为画背景是先画的基线，然后向两边衍生
        lineNumberZ = (int)(viewWidth/smallGridWidth);
        offset_x_max = viewWidth - dotWidth * datas.size();
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

    /**
     * 设置偏移量
     * @param ratio 这个值并不是一个具体的偏移量的值，而是一个相对于最大偏移量的比例
     */
    public void setXChangedRatio(float ratio) {
        this.x_changed = offset_x_max * ratio;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        //画基准线
        drawBaseLine(canvas);
        //画头部
        if(isDrawableHead){
            drawHead(canvas);
        }
        //画数据
        drawData(canvas);
    }


    public void setDrawableHead(boolean drawableHead) {
        isDrawableHead = drawableHead;
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

    private Paint mPaint;


    //绘制数据
    private void drawData(Canvas canvas) {
        //清除路径
        path.reset();
        linePath.reset();
        x_changed += x_change;
        if (x_changed > xori){//防止向右滑动太多 超左边界
            x_changed = xori;
        }else if (x_changed < offset_x_max ){//防止向左滑动太多 超右边界
            x_changed = offset_x_max;
        }
        //此处 xori设置为0 ，未用上
        int iXor = 1;
        for (int i = 1 ; i < this.datas.size() ; i ++){
            float nnn = xori + dotWidth * i +  x_changed;//表示为偏移之后点的X轴坐标
            if (nnn >= 0 ){
                iXor = i;
                path.moveTo(nnn+smallGridWidth * 5 * 2, valuesToY(datas.get(i).getData()));
                linePath.moveTo(xori + dotWidth * i+x_changed+smallGridWidth * 5 * 2,viewHeight-smallGridWidth * 6);
                break;
            }
        }
        for (int i = iXor; i < this.datas.size(); i ++){
            float nnn = xori + dotWidth * i +  x_changed;
            if (nnn < viewWidth + dotWidth){
                EcgPointEntity entity = datas.get(i);
                EcgPointEntity lastEntity = datas.get(i-1);
                if(lastEntity.isRed() != entity.isRed()){
                    //当前颜色值和上一个颜色值不一样
                    canvas.drawPath(path,mPaint);
                    path.reset();
                    path.moveTo(xori + dotWidth * (i-1)+smallGridWidth * 5 * 2 +  x_changed,valuesToY(datas.get(i-1).getData()));
                    path.lineTo(xori + dotWidth * (i) +  x_changed+smallGridWidth * 5 * 2,valuesToY(entity.getData()));
                    if(entity.isRed()){
                        mPaint.setColor(mColorDataRed);
                    }else {
                        mPaint.setColor(mColorData);
                    }
                }else {
                    //连续的点，颜色值是一样的，或者都是红色，或者都是蓝色
                    path.lineTo(xori + dotWidth * (i) +  x_changed+smallGridWidth * 5 * 2,valuesToY(entity.getData()));
                    if(entity.isRed()){
                        mPaint.setColor(mColorDataRed);
                    }else {
                        mPaint.setColor(mColorData);
                    }
                }
                //画时间
                if(entity.getDate().getTime() != lastEntity.getDate().getTime()|| i == 1){
                    //两个时间值不一样
                    linePath.lineTo(xori + dotWidth * i +  x_changed+smallGridWidth * 5 * 2,viewHeight-smallGridWidth * 7);
                    linePath.lineTo(xori + dotWidth * i +  x_changed+smallGridWidth * 5 * 2,viewHeight-smallGridWidth * 6);
                    String time = DateUtils.getDateString(DateUtils.DATE_FORMAT_HMS,entity.getDate().getTime());
                    /*Rect rect = new Rect();
                    mTimePaint.getTextBounds(time, 0, time.length(), rect);
                    //文字的宽度
                    int textWidth = rect.width();*/
                    //时间文本的x轴坐标
                    float textX = xori + dotWidth * i +  x_changed+smallGridWidth * 5 * 2-smallGridWidth * 4;
                    //时间文本的y轴坐标
                    float textY = viewHeight-smallGridWidth * 3;
                    canvas.drawText(time,textX, textY,mTimePaint);
                }else {
                    //一样的时候
                    linePath.lineTo(xori + dotWidth * i +  x_changed+smallGridWidth * 5 * 2,viewHeight-smallGridWidth * 6);
                }
//                Log.d(TAG, "drawData: x_changed = "+String.valueOf(x_changed));
            }
        }
        canvas.drawPath(path,mPaint);
        canvas.drawPath(linePath,mLinePaint);
    }

    //手指按下的点为(x1,y1)  手指离开屏幕的点为(x2,y2);
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //除以2把滑动速度变慢点
                x_change = (event.getX() - startX)/10;
                if(moveViewListener != null){
                    moveViewListener.soffsetX(offset_x_max,x_changed);
                    invalidate();
                }else {
                    Log.e(TAG, "onTouchEvent:moveViewListener is null. " );
                }
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                if(y1 - y2 > 50) {
                    Log.d(TAG, "onTouchEvent: 向上滑");
                } else if(y2 - y1 > 50) {
                    Log.d(TAG, "onTouchEvent: 向下滑");
                } else if(x1 - x2 > 50) {
                    Log.d(TAG, "onTouchEvent: 向左滑");
                } else if(x2 - x1 > 50) {
                    Log.d(TAG, "onTouchEvent: 向右滑");
                }
                break;
            default:break;
        }
        return true;
    }



    /**
     * 这个的意思是两个大格子表示的值是200  200 = 2个大格子的高度 = 2 * 1个大格子的高度 = 2 * 5 个小格子的高度 一个小格子表示的值为20
     * 1 小格的数据 表示为20
     * @param data
     * @return
     */
    private float valuesToY(Integer data){
//        return  (-1.0f) * data /(100.0f) * (20.0f) + baseLine;
        return  data * (-1.0f) + baseLine;
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

    private void init(Context context) {

        //初始化心电图画笔
        mPaint = new Paint();
        mPaint.setStrokeWidth(ecgWidth);
        mPaint.setColor(Color.parseColor("#07aef5"));
        mPaint.setAntiAlias(true);

        //设置样式
        mPaint.setStyle(Paint.Style.STROKE);

        //初始化基线画笔
        mBaseLine = new Paint();
        mBaseLine.setStrokeWidth(mBaseLineWidth);
        mBaseLine.setAntiAlias(true);
        mBaseLine.setColor(mBaseLineColor);
        mBaseLine.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
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
        mLinePaint.setStrokeWidth(mBaseLineWidth);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        mHeadPaint = new Paint();
        mHeadPaint.setColor(mColorData);
        mHeadPaint.setStrokeWidth(mHeadLineWidth);
        mHeadPaint.setStyle(Paint.Style.STROKE);
        mHeadPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));

        mHeadPath = new Path();
    }
    /**
     * 绘制数据
     * @param datas
     */
    public void setDatas(List<EcgPointEntity> datas) {
        this.datas = datas;
        invalidate();
    }

    public interface MoveViewListener{
        void soffsetX(float maxOffsetX, float offsetX);
    }

}

