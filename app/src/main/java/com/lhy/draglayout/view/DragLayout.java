package com.lhy.draglayout.view;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static com.lhy.draglayout.view.DragLayout.Status.Close;
import static com.lhy.draglayout.view.DragLayout.Status.Open;

/**
 * 创 建 人: 路好营
 * 创建日期: 2017/3/25 09:42
 * 添加备注: 侧滑面板
 */

public class DragLayout extends FrameLayout {


    private ViewDragHelper viewDragHelper;
    private ViewGroup mLeftContent;
    private ViewGroup mMainContent;
    private int screenWidth;//屏幕宽度
    private int screenHeight;//屏幕高度
    private int leftScreenRange;//左面版的宽度范围

    public static enum Status {
        Close, Open, Draging
    }

    private Status status = Close;

    public interface OnDragChangeListener {
        void OnClose();

        void OnOpen();

        void OnDraging(float percent);

    }

    private OnDragChangeListener onDragChangeListener;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OnDragChangeListener getOnDragChangeListener() {
        return onDragChangeListener;
    }

    public void setOnDragChangeListener(OnDragChangeListener onDragChangeListener) {
        this.onDragChangeListener = onDragChangeListener;
    }

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //1,创建拖拽辅助类 sensitivity:灵敏度;
        viewDragHelper = ViewDragHelper.create(this, 0.3f, callback);
    }

    //3接收由viewDragHelper处理事件的结果
    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         *
         * @param child 被用户拖拽的孩纸
         * @param pointerId 多点触摸的手指ID
         * @return 是否可以被拖拽
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {

//            return child == mMainContent;//只有拖动主面板的时候才让它移动
            return true;//两个面板都能拖动
        }

        /**
         * 修正View水平方向的位置,此时还没有发生真正的拖拽,预设
         * @param child 被用户拖拽的孩纸
         * @param left 建议水平移动的位置
         * @param dx 每次拖拽时和老的位置的差值
         * @return 返回值决定了View将会移动到的位置
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mMainContent) {//判断的是主面板向右滑动的范围
                left = fixLeft(left);
            }
            return left;
        }

        /**
         *
         * @param child 被用户拖拽的孩纸
         * @return 返回拖拽的范围, 返回一个大于0的值, 决定了侧滑动画的执行时长和水平方向是否可以被滑开
         * 当子view内部也有触摸事件(比如ListView)的时候,若不大于0,则(比如ListView)会抢占焦点,导致滑动不了主面板
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return leftScreenRange;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        /**
         * 当控件位置发生变化时调用,可以在这里做伴随动画,状态更新,事件的回调等
         * 如果 "return child == mMainContent;//只有拖动主面板的时候才让它移动"
         *      则滑动左面版的时候不会调用此方法,左面版也就不能做伴随动画了
         * @param changedView
         * @param left 最新的位置
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mLeftContent) {
                //不让左面版移动,固定死左面版的位置
                mLeftContent.layout(0, 0, screenWidth, screenHeight);
                //把左面版的变化量传给主面板,设置主面板的位置
                int newLeft = mMainContent.getLeft() + dx;
                newLeft = fixLeft(newLeft);
                mMainContent.layout(newLeft, 0, screenWidth + newLeft, screenHeight);
            }
            dispatchDragEvent();
            invalidate();//为了兼容低版本,手动重绘内容
        }

        /**
         * 松手后执行的方法,结束动画
         * @param releasedChild 被释放的孩子
         * @param xvel 水平方向的速度,向右滑动为+,向左为-
         * @param yvel 竖直方向的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel == 0) {
                if (mMainContent.getLeft() > leftScreenRange * 0.5f) {//速度是0并且主面板的左边位置已经超过可移动范围的一半,则打开左面版
                    openLeftContent(true);
                } else {
                    closeLeftContent(true);
                }
            } else if (xvel > 0) {//速度大于0的时候,判断手指活动的角度,打开左面版
                openLeftContent(true);
            } else {//速度为-的时候,合上左面版
                closeLeftContent(true);
            }
        }
    };

    /**
     * 分发拖拽事件,伴随动画,更新状态等
     */
    private void dispatchDragEvent() {
        float leftScreenRangePercent = mMainContent.getLeft() * 1.0f / leftScreenRange;//左面版显示的宽度百分比0.0-->1.0
        animationViews(leftScreenRangePercent);
        updateStatus(leftScreenRangePercent);
    }

    /**
     * 更新状态
     */
    private void updateStatus(float leftScreenRangePercent) {
        if (onDragChangeListener != null) {
            onDragChangeListener.OnDraging(leftScreenRangePercent);
            Status lastStatus = status;
            status = getStatus(leftScreenRangePercent);
            if (lastStatus != status) {
                if (status == Close) {
                    onDragChangeListener.OnClose();
                } else if (status == Open) {
                    onDragChangeListener.OnOpen();
                }
            }
        }
    }

    /**
     * 获取最新状态
     *
     * @return 状态值
     */
    private Status getStatus(float leftScreenRangePercent) {
        Status lastStatus = status;
        if (leftScreenRangePercent == 0) {
            lastStatus = Status.Close;
        } else if (leftScreenRangePercent == 1) {
            lastStatus = Status.Open;
        }
        return lastStatus;
    }


    private void animationViews(float leftScreenRangePercent) {
        FloatEvaluator floatEvaluator = new FloatEvaluator();//获取位置的算法
        mLeftContentAnimation(leftScreenRangePercent, floatEvaluator);
        mMainContentAnimation(leftScreenRangePercent, floatEvaluator);
        //容器背景色由黑色渐变为透明色
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        getBackground().setColorFilter((Integer) argbEvaluator.evaluate(leftScreenRangePercent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    /**
     * 主面版伴随动画
     *
     * @param leftScreenRangePercent
     * @param floatEvaluator
     */
    private void mMainContentAnimation(float leftScreenRangePercent, FloatEvaluator floatEvaluator) {
        //缩放动画                                       参数:依赖的百分比,开始的百分比,结束的百分比
        mMainContent.setScaleX(floatEvaluator.evaluate(leftScreenRangePercent, 1.0f, 0.8f));
        mMainContent.setScaleY(floatEvaluator.evaluate(leftScreenRangePercent, 1.0f, 0.8f));
    }

    /**
     * 左面版伴随动画
     *
     * @param leftScreenRangePercent
     * @param floatEvaluator
     */
    private void mLeftContentAnimation(float leftScreenRangePercent, FloatEvaluator floatEvaluator) {
        //缩放动画                                       参数:依赖的百分比,开始的百分比,结束的百分比
        mLeftContent.setScaleX(floatEvaluator.evaluate(leftScreenRangePercent, 0.5f, 1.0f));
        mLeftContent.setScaleY(floatEvaluator.evaluate(leftScreenRangePercent, 0.5f, 1.0f));

        //平移动画
        mLeftContent.setTranslationX(floatEvaluator.evaluate(leftScreenRangePercent, -leftScreenRange / 2.0f, 0));

        //透明度动画
        mLeftContent.setAlpha(floatEvaluator.evaluate(leftScreenRangePercent, 0.2f, 1.0f));
    }

    /**
     * 关闭左面版,也即是主面板回到原来的位置
     *
     * @param isSmooth 是否平滑的关闭
     */
    public void closeLeftContent(boolean isSmooth) {
        if (isSmooth) {//触发一个平滑的动画
            if (viewDragHelper.smoothSlideViewTo(mMainContent, 0, 0)) {
                //v4包下的方法,用来排队重绘内容,防止漏帧
                ViewCompat.postInvalidateOnAnimation(this);//参数一定要传子view所在的容器
            }
        } else {
            mMainContent.layout(0, 0, screenWidth, screenHeight);
        }
    }

    /**
     * 打开左面版,也即是主面板向右滑开
     *
     * @param isSmooth 是否平滑的打开
     */
    public void openLeftContent(boolean isSmooth) {
        if (isSmooth) {//触发一个平滑的动画
            if (viewDragHelper.smoothSlideViewTo(mMainContent, leftScreenRange, 0)) {
                //v4包下的方法,用来排队重绘内容,防止漏帧
                ViewCompat.postInvalidateOnAnimation(this);//参数一定要传子view所在的容器
            }
        } else {
            mMainContent.layout(leftScreenRange, 0, screenWidth + leftScreenRange, screenHeight);
        }
    }

    /**
     * 每次绘制内容之前执行的方法,用来计算是否还要继续绘制下一帧
     * 会被高频率的的调用
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);//参数一定要传子view所在的容器
        }
    }

    /**
     * 修正向右拖动的范围,限制宽度
     *
     * @param left
     * @return
     */
    private int fixLeft(int left) {
        if (left < 0) {//向左拖拽,不动
            return 0;
        } else if (left > leftScreenRange) {//向右拖拽超过了范围,也不要再动了
            return leftScreenRange;
        }
        return left;
    }

    //2,转交触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //由viewDragHelper判断事件是否该拦截
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            //由viewDragHelper处理事件
            viewDragHelper.processTouchEvent(event);
        } catch (Exception e) {//防止多点触控异常
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 在onMeasure测量之后,如果控件尺寸发生变化的时候调用
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取屏幕宽度
        screenWidth = getMeasuredWidth();
        screenHeight = getMeasuredHeight();
        leftScreenRange = (int) (screenWidth * 0.6f);
    }

    /**
     * view添加完成后调用,获取主面板和左面版
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            throw new IllegalStateException("至少需要两个子View控件!(Need at least two child View!)");
        }
        if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("孩子必须是ViewGroup的子类!(The child must be a subclass of ViewGroup!)");
        }
        mLeftContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);
    }
}
