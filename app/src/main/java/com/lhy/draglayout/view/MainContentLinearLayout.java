package com.lhy.draglayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 创 建 人: 路好营
 * 创建日期: 2017/3/28 11:22
 * 添加备注: 自定义的LinearLayout(也可以是RelativeLayout),重写onInterceptTouchEvent和onTouchEvent判断是否拦截触摸事件
 */

public class MainContentLinearLayout extends LinearLayout {
    private DragLayout dragLayout;
    private float rawDownX;
    private float rawDownY;
    private float rawUpX;
    private float rawUpY;

    public MainContentLinearLayout(Context context) {
        super(context);
    }

    public MainContentLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainContentLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (dragLayout != null && dragLayout.getStatus() != DragLayout.Status.Close) {
            //不是关闭状态,直接拦截,不往下传递了,防止打开左面版后,主面板的内容还能点击
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (dragLayout != null && dragLayout.getStatus() != DragLayout.Status.Close) {
            //如果手指抬起,判断是否执行关闭左面版
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    rawDownX = event.getRawX();
                    rawDownY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    rawUpX = event.getRawX();
                    rawUpY = event.getRawY();
                    float dy = rawUpY - rawDownY;
                    float dx = rawUpX - rawDownX;
                    if (Math.abs(dx) == 0 && Math.abs(dy) == 0) {//只是点击一下,并未移动
                        dragLayout.closeLeftContent(true);
                    }
                    break;
            }
            //不是关闭的状态,直接拦截,不往下传递了,防止打开左面版后,主面板的内容还能点击
            return true;
        } else {
            return super.onTouchEvent(event);
        }

    }

    public void setDragLayout(DragLayout dragLayout) {
        this.dragLayout = dragLayout;
    }
}
