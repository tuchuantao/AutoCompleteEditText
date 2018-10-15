package com.kevin.autocompleteedittext.util

import android.content.Context
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * Create by KevinTu on 2018/10/15
 */
object ModUtils {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    fun getWindowWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.width
    }

    /**
     * 关闭软件盘
     *
     * @param context
     * @param windowToken
     */
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    fun closeSoftInput(context: Context, windowToken: IBinder) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}