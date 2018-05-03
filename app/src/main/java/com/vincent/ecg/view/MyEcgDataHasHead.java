package com.vincent.ecg.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.vincent.mylibrary.util.DpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name CustomView
 * @page com.example.vincent.customview
 * @class describe
 * @date 2018/1/2 18:08
 */

public class MyEcgDataHasHead extends View {
    private Context mContext;
    private final String TAG = MyEcgDataHasHead.class.getSimpleName();
    public List<EcgPointEntity> datas = new ArrayList<>();
    //正常颜色的数据画笔
    private Paint mPaint;
    //标红数据画笔
    private Path mPath;
    private int mColorData = Color.parseColor("#1e6dd8");
    private int mColorDataRed = Color.parseColor("#ff3030");
    private int view_width;
    private int view_height;
    //心电图的宽度
    private float line_width = 4f;
    //基线的位置
    private float baseLine;
    //小格子的宽度
    private float smailGridWith = 4f;
    private float bigGridWidth ;

    //表示每个小格子放五个数据
    private int dataNumber = 5;
    //屏幕能够显示的所有的点的个数 注意这个值设置为int会导致这个数值不准确
    private float maxSize = 0;
    //是否绘制头部 凸状物 true 画  false 不画
//    private boolean isDrawHead = true;
    //绘制头部画笔
    private Paint mHeadPaint;
    //头部路径
    private Path mHeadPath;
    //头部的颜色
    private int mColorHead = Color.parseColor("#1e6dd8");
    //头部的宽度
    private float headPathWidth = 1f;
    //头部的总宽度为两个大格子
    private float headWidth;

    private float mark = -1.0f;
    private boolean orientation = false;
    // pace检测  0 不支持 1 打开 2 关闭 起搏
    private boolean isDrawPace = false;
    //绘制pace
    private Paint mPacePaint;
    //绘制Qrs
    private Paint mQrsPaint;
    // qrs 打点的颜色值
    private int qrsColor = Color.parseColor("#333333");

    //暂停绘制 true暂停  false绘制
    private boolean pause = false;

    public boolean onDrawDone=false;
    private int startDrawPoint = 1; //当前画点的位置
    private int LastDrawPoint = 1; //最后一个画点的位置
    private Bitmap mBufferBitmap ;
    private Canvas mCanvas;
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
        clearAllData();
    }



    /**
     * 获取屏幕显示的最大的点个数
     * @return
     */
    public float getMaxSize() {
        maxSize =  (view_width*(1.0f)-headWidth) / (smailGridWith / (dataNumber*1.0f));
        if(maxSize <0){
            maxSize = maxSize * (-1);
        }
        return maxSize;
    }

    public MyEcgDataHasHead(Context context) {
        super(context);
        init(context);
        setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent));
    }
    public MyEcgDataHasHead(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
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


    /**
     * 调整Y轴比例
     */
    private float radio = 1;

    /**
     * 比例调整 调整Y点的比值
     * @param ratio
     */
    public void valueRatio(float ratio){
        this.radio = ratio;
    }

    /**
     * 设置所有的点为红色
     */
    public void setAllPointIsRed(){
        for (int i = 0;i<datas.size();i++){
            datas.get(i).setRed(true);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //控件宽度
        view_width = w;
        //控件高度
        view_height = h;
        //基准线的位置 中间向上挪两个格子
        baseLine = view_height /2 - bigGridWidth * 2;
        //125个数据占用为5个大格子，5个大格子有25个小格子，所以每个小格子放5个数据
        maxSize =  (view_width*(1.0f)-headWidth) / (smailGridWith / (dataNumber*1.0f));
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout: 。。。。。。。。。。 "+String.valueOf(System.currentTimeMillis()-lastTime)+"  "+datas.size());
        super.onLayout(changed, left, top, right, bottom);
    }

    private long lastTime;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: 。。。。。。。。。。 "+String.valueOf(System.currentTimeMillis()-lastTime)+"  "+datas.size());
        lastTime = System.currentTimeMillis();
        if (onDrawDone==false) {
            if (mCanvas==null) {}else {
                //mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //画头部
                drawHead(mCanvas);
                //画数据
                drawData(mCanvas);
                canvas.drawBitmap(mBufferBitmap, 0, 0, mPaint);
                onDrawDone = true;
            }
        }
        Log.d(TAG, "Draw finish: 。。。。。。。。。。 "+String.valueOf(System.currentTimeMillis()-lastTime)+"  "+startDrawPoint+":"+datas.size());
    }

    private long startDraw;

    /**
     * 绘制数据
     * @param canvas
     */
    private void drawData(Canvas canvas) {
        //清除路径
        mPath.reset();
        if(datas != null && datas.size()>0){
            //绘制头部
            switch (start){
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
            //点的宽度
            //startDrawPoint=1;
            float pointWidth = (float) 1.0 * smailGridWith/dataNumber;
//            Log.d(TAG, "drawData: 点的宽度 "+String.valueOf(pointWidth)+" dataNumber="+String.valueOf(dataNumber)+" smailGridWith="+String.valueOf(smailGridWith));

            int count;
            count=datas.size();
            Log.d(TAG, "count: "+count);

            startDrawPoint=(int)maxSize-count;

            float marginLeft;
            marginLeft=(float)count;
            marginLeft=(marginLeft*pointWidth)*1.0f;
            canvas.drawBitmap(mBufferBitmap, -marginLeft , 0, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawRect(startDrawPoint*pointWidth+headWidth-1,0,maxSize * pointWidth+headWidth,view_height,mPaint);


            mPath.moveTo(startDrawPoint * pointWidth+headWidth,change(datas.get(0).getData()));
            mPath.lineTo(startDrawPoint * pointWidth+headWidth,change(datas.get(0).getData()));
            for (int i =(int)maxSize;i>(maxSize-datas.size()+1) ;i--) {
                EcgPointEntity entity = datas.get((int)maxSize-i+1);
                EcgPointEntity lastEntity = datas.get((int)maxSize-i);
                int x;
                int lastPointX;
                x = i;
                lastPointX = i-1;
                if(lastEntity.isRed() != entity.isRed()){
                    //当前颜色值和上一个颜色值不一样
//                        Log.d(TAG, "drawData: 当前颜色值和上一个颜色值不一样 " + i);
                    canvas.drawPath(mPath,mPaint);
                    mPath.reset();
                    mPath.moveTo(lastPointX * pointWidth+headWidth,change(datas.get((int)maxSize-i).getData()));
                    mPath.lineTo(x * pointWidth+headWidth,change(entity.getData()));

                    if(isDrawPace && entity.isPace()){
                        //绘制pace标记
                        mPaint.setColor(mColorDataRed);
                        mPath.moveTo(x * pointWidth+headWidth,change(entity.getData())- DpUtil.dp2px(mContext,10));
                        mPath.lineTo(x * pointWidth,change(entity.getData())+ DpUtil.dp2px(mContext,10));
                        mPath.moveTo(x * pointWidth,change(entity.getData()));
                    }
                    if(entity.isRed()){
                        mPaint.setColor(mColorDataRed);
                    }else {
                        mPaint.setColor(mColorData);
                    }
                }else {
                    //连续的点，颜色值是一样的，或者都是红色，或者都是蓝色
                    if (x == 0){
                        mPath.reset();
                        mPath.moveTo(headWidth,change(datas.get((int)maxSize-i+1).getData()));
                        mPath.lineTo(headWidth,change(datas.get((int)maxSize-i+1).getData()));
                    }else {
                        if(entity.getData() != ecgTag){
                            if (lastEntity.getData() == ecgTag){
                                mPath.moveTo(x * pointWidth+headWidth,change(entity.getData()));
                            }
                            mPath.lineTo(x * pointWidth+headWidth,change(entity.getData()));

                            if(isDrawPace && entity.isPace()){
                                //绘制pace标记
                                canvas.drawLine(x * pointWidth+headWidth,change(entity.getData())- DpUtil.dp2px(mContext,10),
                                        x * pointWidth+headWidth,change(entity.getData())+ DpUtil.dp2px(mContext,10),mPacePaint );
                            }
                            if(entity.isQrs()){
                                canvas.drawLine(x * pointWidth+headWidth,view_height- DpUtil.dp2px(mContext,44+51+5),
                                        x * pointWidth+headWidth,view_height- DpUtil.dp2px(mContext,44+51),mQrsPaint);
                            }
                        }else {
                            startDraw = System.currentTimeMillis();
                            canvas.drawPath(mPath,mPaint);
                            //Log.d(TAG, "drawData: - "+String.valueOf(System.currentTimeMillis()-startDraw));
                            mPath.reset();
                        }
                    }
                    if(entity.isRed()){
                        mPaint.setColor(mColorDataRed);
                    }else {
                        mPaint.setColor(mColorData);
                    }
                }
            }
            if (count>1) {
                for (int t = 0; t < count; t++) {
                    datas.remove(0);
                }
            }
            canvas.drawPath(mPath,mPaint);
        }


    }


    //四个值 5 10 20 30
    private float addValues = 10;

    /**
     * 设置增益 这个值调节Y值 并且影响头部高度
     * @param addValues
     */
    public void setAddValues(float addValues) {
        this.addValues = addValues;
        clearAllData();
        invalidate();
    }

    //头部起点位置 这个值有四个，0,100(smailGridWith * 5),150(smailGridWidth *5* 1.5),175(smailGridWidth * 5 * 1.75)
    private float headStart = 0;
    private int start = 0;
    /**
     * 设置头部的起点位置
     */
    public void setHeadStart(int start) {
        this.start = start;
        switch (start){
            case 0:
                headStart = 0;
                break;
            case 100:
                headStart = bigGridWidth;
                break;
            case 150:
                headStart = (float) (bigGridWidth * 1.5);
                break;
            case 175:
                headStart = (float)(bigGridWidth * 1.75);
                break;
            default:headStart=0;
        }
        clearAllData();
        Log.d(TAG, "setHeadStart: "+headStart);
        //invalidate();
    }

    /**
     * 绘制头部
     * @param canvas
     */
    private void drawHead(Canvas canvas) {
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

    //当前点的位置下标
    private int index = 0;

    // 模式2 true 点刷新  false  平移
    private boolean model2 = true;

    /**
     * 更改模式
     */
    public void changeModel() {
        if(model2){
            model2 = false;
        }else {
            model2 = true;
        }
        clearAllData();
        invalidate();
    }
    //这是一个标记数据，绘制的时候遇到这个数据就跳过
    private int ecgTag = 100000;

    /**
     * 添加数据
     * @param data
     */
    public void addData(EcgPointEntity data){
        checkPace();
        maxSize = getMaxSize();
//        Log.d(TAG, "addData: max size -->"+String.valueOf(maxSize));
        if(datas != null && datas.size() == 0){
            index = 0;
        }
        if(model2){
            //点刷新
            if(index > maxSize-1){
                index = 0;
            }else {
                index ++;
            }
            if(datas.size() > maxSize){
                //如果这个集合大于maxSize（即表示屏幕上所能显示的点的个数）个点，那么就把第一个点移
                datas.set(index,data);
                int index1 = index +1;
                int index2 = index1 +1;
                int index3 = index2 +1;
                int index4 = index3 +1;
                if(index1 < datas.size()){
                    EcgPointEntity entity1 = datas.get(index1);
                    entity1.setData(ecgTag);
                }
                if(index2 < datas.size()){
                    EcgPointEntity entity2 = datas.get(index2);
                    entity2.setData(ecgTag);
                }
                if(index3 < datas.size()){
                    EcgPointEntity entity3 = datas.get(index3);
                    entity3.setData(ecgTag);
                }
                if(index4 < datas.size()){
                    EcgPointEntity entity4 = datas.get(index4);
                    entity4.setData(ecgTag);
                }
            }else {
                datas.add(data);
            }
        }else {
            //平移
            datas.add(data);

            if(datas.size() > maxSize){
                //datas.remove(0);

                startDrawPoint=startDrawPoint+1;
            }
        }
        if(pause){
            return;
        }
//        Log.d(TAG, "invalidate"+datas.get(index).getData());
        //invalidate();
    }


    public void invalidView() {



        invalidate();



    }
    /**
     * 检查是否绘制pase标记
     */
    private void checkPace() {
        int pace = 0;
        if(pace != 1){
            isDrawPace = false;
        }else {
            //打开
            isDrawPace = true;
        }
    }


    /**
     * 是否暂停绘制
     * @param pause true 暂停  false 不暂停
     */
    public void onPauseDraw(boolean pause) {
        this.pause = pause;
    }

    public void clearAllData(){
        if(datas != null && datas.size()>0){
            datas.clear();
        }
    }

    private int i = 0;

    public void addAllData(List<EcgPointEntity> datas){
        this.datas.addAll(datas);
        maxSize = getMaxSize();
        if(this.datas.size()>maxSize){
            for(int i = 0;i<this.datas.size() - maxSize;i++){
                this.datas.remove(0);
            }
        }
        i ++;
        invalidate();
        Log.d(TAG, "addAllData: ------------------------ "+i +"         "+datas.size());
    }

    /**
     * 获取当前数据
     * @return
     */
    public List<EcgPointEntity> getDatas() {
        return datas;
    }

    private float maxValue;
    private float minValue;

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
        float v = value * radio;
        if(v>view_height-maxValue){
            v = view_height - maxValue;
        }
        if(v < minValue){
            v = minValue;
        }
        return v;
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

    private void init(Context mContext) {
        this.mContext = mContext;
        smailGridWith = DpUtil.dp2px(mContext,smailGridWith);
        headPathWidth = DpUtil.dp2px(mContext,headPathWidth);
        maxSize = DpUtil.dp2px(mContext,120);
        minValue = DpUtil.dp2px(mContext,30);
        bigGridWidth = smailGridWith * 5;
        headWidth = bigGridWidth * 2;

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

        mPacePaint = new Paint();
        mPacePaint.setStyle(Paint.Style.STROKE);
        mPacePaint.setAntiAlias(true);
        mPacePaint.setColor(mColorDataRed);
        mPacePaint.setStrokeWidth(DpUtil.dp2px(mContext,1));

        //QRS 打点画笔
        mQrsPaint = new Paint();
        mQrsPaint.setStrokeWidth(DpUtil.dp2px(mContext,3)/2);
        mQrsPaint.setStyle(Paint.Style.STROKE);
        mQrsPaint.setAntiAlias(true);
        mQrsPaint.setColor(qrsColor);




    }

    public void initDrawBuffer(int width,int height) {
        mBufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas();
        mCanvas.setBitmap(mBufferBitmap);
    }





}
