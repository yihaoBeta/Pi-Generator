package com.yihaobeta.picomputor.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView


/**
 * 自定义的scrollview，可以动态的添加新的view
 * 单个的TextView当其中的内容过多时，会造成卡顿
 * 这个类可以在一个TextView内容达到一定程度时，
 * 动态的添加一个新的TextView
 * 并且可以保证最后一行完整的显示，在UI上来看就像一个
 * TextView一样
 */
class AutoAdjustScrollView : ScrollView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    companion object {
        //一个TextView最多显示的行数
        private const val MAX_LINE = 50
        //当计算出错时，一个TextView最多可以显示的行数，
        //用来保证在计算出现莫名错误时，不会用一个TextView无限
        //的显示下去
        private const val DEFAULT_MAX_LINE_COUNT = 100
    }

    private val linearLayout: LinearLayout by lazy {
        LinearLayout(context).apply {
            this.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            this.orientation = LinearLayout.VERTICAL
        }
    }

    private var curTextView: TextView
    //每行最大的字符数
    private var maxPerLineCount = 0

    init {
        addView(linearLayout)
        curTextView = addNewTextView("")
    }

    fun setResult(s: String) {
        /**超过最大字符个数或者最大行数，增加一个item继续显示，
         **用来优化单个TextView显示字数过多时带来的卡顿
         **/
        if (shouldSkipToNextItem() ||
            (maxPerLineCount > 0 && curTextView.text.length
                    > DEFAULT_MAX_LINE_COUNT * maxPerLineCount)
        ) {//当前TextView已经达到最大的行数，添加新的TextView
            curTextView = addNewTextView(s)
        } else {//继续在当前的TextView追加内容
            curTextView.append(s)
        }
    }

    //动态生成一个TextView，并添加到父控件
    private fun addNewTextView(s: String): TextView {
        val newView = TextView(context)
        newView.setSingleLine(false)
        newView.typeface = Typeface.MONOSPACE
        newView.textSize = 16f
        newView.setPadding(5, 2, 5, 2)
        newView.text = s
        newView.gravity = Gravity.START
        linearLayout.addView(newView)
        requestLayout()
        return newView
    }

    /**
     * 用来计算TextView是否达到了最大行数
     */
    private fun shouldSkipToNextItem(): Boolean {
        var shouldSkip = false
        //取最大行数的上一行的字数，用来判断最后一行是否已经填满整行
        //使用">="是为了避免某一行无法达到上一行的字数就因为宽度问题换行，从而导致的无法
        //新增item的问题
        val lineCount = curTextView.lineCount
        if (MAX_LINE in 3..lineCount) {
            val startLastLine = curTextView.layout.getLineStart(MAX_LINE - 2)
            val endLastLine = curTextView.layout.getLineEnd(MAX_LINE - 2)
            val start = curTextView.layout.getLineStart(MAX_LINE - 1)
            val end = curTextView.layout.getLineEnd(MAX_LINE - 1)
            val lastLineCount = endLastLine - startLastLine
            val curLineCount = end - start
            maxPerLineCount = if (maxPerLineCount > lastLineCount) maxPerLineCount else lastLineCount
            //log("lineCount=$lineCount,lastLine=:$lastLineCount,curLine=$curLineCount")

            if (curLineCount == lastLineCount) {//与上一行字数相同，跳转
                shouldSkip = true
            }
        }
        return shouldSkip
    }

    /**
     * 清空内容，用来开始新的计算任务
     */
    fun reset() {
        linearLayout.removeAllViews()
        curTextView = addNewTextView("")
    }
}