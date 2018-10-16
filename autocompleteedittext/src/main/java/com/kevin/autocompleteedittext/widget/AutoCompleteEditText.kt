package com.kevin.autocompleteedittext.widget

import java.util.HashMap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.kevin.autocompleteedittext.R
import com.kevin.autocompleteedittext.util.ModUtils

/**
 * 自动补全邮箱，当内容太长时缩小字体
 *
 * Create by KevinTu on 2018/10/15
 */
class AutoCompleteEditText : AppCompatEditText, TextWatcher, View.OnFocusChangeListener {

    private lateinit var mContext: Context
    private var textChangerListener: TextChangeListener? = null

    private lateinit var mBitmap: Bitmap
    private var mHeight: Int = 0
    private var mWidth: Int = 0
    private lateinit var mPaint: Paint
    private var mBaseLine: Int = 0
    private lateinit var mCanvas: Canvas
    private var mDrawable: BitmapDrawable? = null
    private var notifyText = ""

    private var notifyTextColor: Int = Color.BLUE
    private var normalTextSize: Float = 0F
    private var minTextSize: Float = 0F
    private var currentTextSize: Float = 0F
    // 是否开启自动补全
    private var openAutoComplete = true
    // 是否开启大小自动改变
    private var autoChangeSize = true

    /**
     * 常用的邮箱
     */
    private var mAutoData: HashMap<String, String>? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        addTextChangedListener(this)
        onFocusChangeListener = this

        notifyTextColor = Color.GRAY
        normalTextSize = textSize
        currentTextSize = textSize
        minTextSize = context.resources.getDimension(R.dimen.text_size_17)

        // 初始化画笔
        mPaint = Paint()
        mPaint.color = notifyTextColor
        mPaint.isAntiAlias = true// 去除锯齿
        mPaint.isFilterBitmap = true// 对位图进行滤波处理
        mPaint.textSize = currentTextSize
    }

    /**
     * 初始化常用的邮箱
     */
    fun initEmail(mAutoData: HashMap<String, String>) {
        this.mAutoData = mAutoData
    }

    /**
     * 设置提醒的字体颜色
     */
    fun setNotifyTextColor(notifyTextColor: Int) {
        this.notifyTextColor = notifyTextColor
    }

    /**
     * 设置缩放时的字体大小
     */
    fun setMinTextSize(minTextSize: Float) {
        if (minTextSize < textSize) {
            this.minTextSize = minTextSize
            this.autoChangeSize = true
        } else {
            this.autoChangeSize = false
        }
    }

    /**
     * 设置是否开启自动提醒
     */
    fun setAutoCompleteState(openAutoComplete: Boolean) {
        this.openAutoComplete = openAutoComplete
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        mAutoData?.let {
            val content = s.toString()
            val iterator = it.entries.iterator()
            // 遍历常用邮箱
            notifyText = ""
            if (openAutoComplete && isEmail(content)) {
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (content.endsWith(entry.key)) {
                        notifyText = entry.value
                        break
                    }
                }
            }

            if (openAutoComplete && !refreshTextSize()) { // 如果字体大小没有改变时，直接画补全，字体大小改变时，得在控件布局改变后再去画
                drawAddedText(notifyText)
            }

            if (null != textChangerListener) {
                textChangerListener!!.emailAfterTextChanged(s)
            }
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (!focused) {
            append(notifyText)
            setCompoundDrawables(null, null, null, null)
            ModUtils.closeSoftInput(mContext, applicationWindowToken)
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    /**
     * 判断用户输入的邮箱是否具备展现提示的资格
     * @param emailStr
     * @return
     */
    private fun isEmail(emailStr: String): Boolean {
        var result = emailStr.contains("@")
        if (result) { // 判断是否存在两个@符号
            val firstIndex = emailStr.indexOf("@")
            val lastIndex = emailStr.lastIndexOf("@")
            result = firstIndex == lastIndex
        }
        return result
    }

    private fun refreshTextSize(): Boolean {
        var isChangeTextSize = false
        mPaint.textSize = normalTextSize
        val notifyTextWidth = (mPaint.measureText(notifyText) + 1).toInt()
        val inputTextSize = mPaint.measureText(text.toString()).toInt()
        if (currentTextSize == normalTextSize) { // 判断是否需要将字体缩小
            if (mWidth > 0 && notifyTextWidth + inputTextSize > mWidth || mWidth == 0 && notifyTextWidth + inputTextSize > ModUtils.getWindowWidth(mContext) - ModUtils.dip2px(mContext, 68f)) { // 兼容View刚绘画时width为0的情况
                setCompoundDrawables(null, null, null, null)
                currentTextSize = minTextSize
                isChangeTextSize = true
            }
        } else { // 判断是否需要将字体放大
            if (notifyTextWidth + inputTextSize <= mWidth) {
                currentTextSize = normalTextSize
                isChangeTextSize = true
            }
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize)
        mPaint.textSize = currentTextSize
        return isChangeTextSize
    }

    /**
     * 画出后缀字符串
     *
     * @param addedText
     */
    private fun drawAddedText(addedText: String) {
        // 如果字符串为空，画空
        if (addedText == "") {
            setCompoundDrawables(null, null, null, null)
            return
        }

        // 计算baseLine
        val rect = Rect()
        val baseLineLocation = getLineBounds(0, rect)
        mBaseLine = baseLineLocation - rect.top

        // 添加的字符穿的长度
        var notifyTextWidth = (mPaint.measureText(addedText) + 1).toInt()
        // 用户已经输入的字符串长度
        val inputTextWidth = mPaint.measureText(text.toString())

        if (notifyTextWidth + inputTextWidth > mWidth) {
            notifyTextWidth = mWidth - inputTextWidth.toInt()
            if (notifyTextWidth <= 0) {
                setCompoundDrawables(null, null, null, null)
                return
            }
        }
        // 创建bitmap
        mBitmap = Bitmap.createBitmap(notifyTextWidth, mHeight, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        // 绘制后缀字符串
        mCanvas.drawText(addedText, 0f, mBaseLine.toFloat(), mPaint)

        // 计算后缀字符串在输入框中的位置
        val addedTextLeft = (inputTextWidth - mWidth + notifyTextWidth).toInt()
        val addedTextRight = addedTextLeft + notifyTextWidth
        val addedTextBottom = mHeight
        mDrawable = BitmapDrawable(mBitmap)
        // 设置后缀字符串位置
        mDrawable!!.setBounds(addedTextLeft, 0, addedTextRight, addedTextBottom)
        // 显示后缀字符串
        setCompoundDrawables(null, null, mDrawable, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val lastHeight = mHeight
        mHeight = h
        mWidth = w
        if (lastHeight != h && lastHeight != 0) { // 改变了字体大小
            drawAddedText(notifyText)
        }
    }

    fun addTextChangeListener(listener: TextChangeListener) {
        this.textChangerListener = listener
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (!hasFocus && !TextUtils.isEmpty(notifyText)) {
            setCompoundDrawables(null, null, null, null)
            setText(text.toString() + notifyText)
        }
    }

    interface TextChangeListener {
        fun emailAfterTextChanged(s: Editable)
    }
}
