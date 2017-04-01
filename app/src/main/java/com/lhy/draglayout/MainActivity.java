package com.lhy.draglayout;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lhy.draglayout.view.DragLayout;
import com.lhy.draglayout.view.MainContentLinearLayout;

import java.util.Random;

public class MainActivity extends Activity {

    private android.widget.ImageView ivHeader;
    private android.widget.ListView lvMain;
    private android.widget.ListView lvLeft;
    private com.lhy.draglayout.view.DragLayout dragLayout;
    private com.lhy.draglayout.view.MainContentLinearLayout mainContentLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        lvLeft.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, Cheeses.sCheeseStrings) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }
        });
        lvMain.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, Cheeses.NAMES));

        dragLayout.setOnDragChangeListener(new DragLayout.OnDragChangeListener() {
            @Override
            public void OnClose() {
                ToastUtil.showToast(getApplicationContext(), "关闭了");
                ObjectAnimator animator = ObjectAnimator.ofFloat(ivHeader, "translationX", 15);
                animator.setInterpolator(new CycleInterpolator(3));//设置环形差值器,来回移动4圈
                animator.setDuration(600);
                animator.start();
            }

            @Override
            public void OnOpen() {
                ToastUtil.showToast(getApplicationContext(), "打开了");
                lvLeft.smoothScrollToPosition(new Random().nextInt(50));
            }

            @Override
            public void OnDraging(float percent) {
//                ToastUtil.showToast(getApplicationContext(), "正在拖拽:" + percent);
                ivHeader.setAlpha(1 - percent);
            }
        });

        ivHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dragLayout.openLeftContent(true);//点击头像打开左面版
            }
        });
        mainContentLinearLayout.setDragLayout(dragLayout);
    }

    private void initView() {
        this.lvLeft = (ListView) findViewById(R.id.lv_left);
        this.lvMain = (ListView) findViewById(R.id.lv_main);
        this.ivHeader = (ImageView) findViewById(R.id.iv_header);
        this.dragLayout = (DragLayout) findViewById(R.id.drag_layout);
        this.mainContentLinearLayout = (MainContentLinearLayout) findViewById(R.id.ll_main);
    }
}
