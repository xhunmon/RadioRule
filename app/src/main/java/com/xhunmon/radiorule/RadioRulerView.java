package com.xhunmon.radiorule;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

/**
 * user: uidq0530 ,date: 2017-04-16.
 * description：收音机FM搜台尺子
 *
 * @author xhunmon
 */

public class RadioRulerView extends View {

    private static final String tag = "RadioRulerView";

    private int mHeight;    //view的高度
    private int mWidth;     //view的宽度
    private Paint mLinePaint;   //固定的尺子画笔
    private int mLineWidth;//尺子刻度线的宽
    private int mLineColor;//固定尺子刻度线的颜色
    private int mMoveLineColor;//移动尺子刻度线的颜色
    private float mDensity;
    private int mLineDivider;    //两条刻度线间的距离
    private float mLeftWidth;  //尺子离view左边的距离

    private int mMaxLineCount = 220; //总共要画多少条刻度
    private Paint mMoveLinePaint;   //移动尺子的画笔
    private int mValue;        //尺子被选中的值
    private float mMaxX;  //onTouch中能触摸的最大x值
    private float mMinX;    //onTouch中能触摸的最小x值

    private OnValueChangeListener mListener;

    private SparseArray<PointF> activePointers;
    private PointF xPoint;
    private int mPaddingBottom;
    private int mPaddingTop;
    private boolean mIsAuto = false;

    public RadioRulerView(Context context) {
        this(context,null);
    }

    public RadioRulerView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RadioRulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public RadioRulerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDensity = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadioRulerView, defStyleAttr, defStyleRes);
        mLineWidth = (int) a.getDimension(R.styleable.RadioRulerView_line_width,5*mDensity);
        mLineDivider = (int) a.getDimension(R.styleable.RadioRulerView_line_divider,15*mDensity);

        mLineColor = a.getColor(R.styleable.RadioRulerView_line_color,0xff888888);
        mMoveLineColor = a.getColor(R.styleable.RadioRulerView_move_line_color,0xffff0000);
        a.recycle();

        init();
    }


    private void init() {
        activePointers = new SparseArray<>();

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mMoveLinePaint = new Paint();
        mMoveLinePaint.setAntiAlias(true);
        mMoveLinePaint.setColor(mMoveLineColor);
        mMoveLinePaint.setStrokeWidth(mLineWidth);
        mMoveLinePaint.setStyle(Paint.Style.STROKE);
    }

    //此方法在view的尺寸确定后调用
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = getHeight();
        mWidth = getWidth();
        mPaddingBottom = getPaddingBottom();
        mPaddingTop = getPaddingTop();
        mLeftWidth = (mWidth - mMaxLineCount*(mLineWidth +mLineDivider))/2;
        mMaxX = mMaxLineCount*(mLineWidth +mLineDivider) + mLeftWidth;
        mMinX = mLeftWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);

        drawMoveLine(canvas);
    }

    /**
     * 画固定的尺子
     * @param canvas
     */
    private void drawLine(Canvas canvas) {
        canvas.save();
        int height = mHeight;
        int drawCount = 0;//已经画了刻度线的个数
        float xPosition;
        for(int i=0; drawCount<=mMaxLineCount; i++){
            xPosition = (mLineDivider*mDensity + mLineWidth)*drawCount + mLeftWidth;
            if(i%5 == 0 && i%10 != 0){//刻度为5的倍数，但同时不是10的倍数
                canvas.drawLine(xPosition,height*0.85f-mPaddingBottom,xPosition,height*0.15f+mPaddingTop,mLinePaint);
            }else if(i%10 == 0){//刻度为10的倍数
                canvas.drawLine(xPosition,height-mPaddingBottom,xPosition,mPaddingTop,mLinePaint);
            }else {//普通的刻度
                canvas.drawLine(xPosition,height*0.75f-mPaddingBottom,xPosition,height*0.25f+mPaddingTop,mLinePaint);
            }
            drawCount++;
        }
        canvas.restore();
    }


    /**
     * 搜索FM频道的刻度线
     * @param canvas
     */
    private void drawMoveLine(Canvas canvas) {
        canvas.save();
        xPoint = activePointers.valueAt(0);
        if (xPoint != null) {
            canvas.drawLine(xPoint.x,mHeight-mPaddingBottom, xPoint.x,mPaddingTop,mMoveLinePaint);
            setValue(eventXValue(xPoint.x));
        }else {
            canvas.drawLine(mMinX,mHeight-mPaddingBottom, mMinX,mPaddingTop,mMoveLinePaint);
        }
        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                float downX = event.getX(pointerIndex);
                if(downX > mMaxX || downX < mMinX) break;
                PointF position = new PointF(downX, event.getY(pointerIndex));
                activePointers.put(pointerId, position);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    PointF point = activePointers.get(event.getPointerId(i));
                    if (point == null) continue;
                    float moveX = event.getX(i);
                    if(moveX > mMaxX || moveX < mMinX) break;
                    point.x = event.getX(i);
                    point.y = event.getY(i);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                int pointerCount = event.getPointerCount();
                PointF point = activePointers.get(event.getPointerId(pointerCount-1));
                if (point == null)  break;
                float upX = event.getX(pointerCount-1);
                if(upX > mMaxX || upX < mMinX) break;
                point.x = eventXValue(event.getX(pointerCount-1));
                point.y = event.getY(pointerCount-1);
                break;
            }
        }
        invalidate();

        return true;
    }

    /**
     *作用：使得放手后MoveLine和Line重合；精确mValue
     * @param x onTouch中的event.getX()
     * @return
     */
    public int eventXValue(float x){
        mLineDivider = (int) (mLineDivider*mDensity);
        return (int) ((x-mLeftWidth)%(mLineWidth +mLineDivider)>((mLineWidth +mLineDivider)/2)
                        ? (((mLineWidth +mLineDivider)*((int)((x-mLeftWidth)/(mLineWidth +mLineDivider))+1))+mLeftWidth)
                        : (((mLineWidth +mLineDivider)*((int)((x-mLeftWidth)/(mLineWidth +mLineDivider))))+mLeftWidth));
    }

    /**
     * 设置最大刻度线个数
     * @param count
     */
    public void setMaxLineCount(int count) {
        mMaxLineCount = count;
    }

    /**
     * 设置是否启用自动搜索功能
     * @param isAuto
     */
    public void setAutoSearchFM(boolean isAuto){
        this.mIsAuto = isAuto;
    }

    /**
     * 开始自动搜台
     */
    public void startAutoSeachFM(){
        if(mIsAuto)
            new Thread(new SeachThread()).start();
    }

    /**
     * 搜台要在开启子线程
     */
    private class SeachThread implements Runnable{

        @Override
        public void run() {
            while(mIsAuto){
                xPoint = activePointers.valueAt(0);
                if(xPoint != null){
                    xPoint.x += (mLineWidth + mLineDivider);
                    if(xPoint.x > mMaxX) xPoint.x = mLeftWidth;
                }else {
                    PointF position = new PointF(mLeftWidth, mHeight);
                    activePointers.put(0, position);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                postInvalidate();
            }
        }
    }

    /*****************************值传递的回调*************************************/
    public interface OnValueChangeListener {
        void onValueChange(float value);
    }

    public void setOnValueChangeListener(OnValueChangeListener listener){
        mListener = listener;
    }

    private void setValue(float value) {
        if(mListener != null){
            mValue = (int) ((value - mLeftWidth)/(mLineDivider*mDensity + mLineWidth));
            //FM的范围从88.0 ~ 108.0
            mListener.onValueChange(mValue/10f + 88);
        }
    }

    /******************************************************************/
}
