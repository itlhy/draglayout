package com.lhy.draglayout;

import android.content.Context;
import android.widget.Toast;

/**
 * 创 建 人: 路好营
 * 创建日期: 2017/3/28 10:55
 * 添加备注:
 */

public class ToastUtil {

    private static Toast toast;

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        toast.setText(msg);
        toast.show();
    }
}
