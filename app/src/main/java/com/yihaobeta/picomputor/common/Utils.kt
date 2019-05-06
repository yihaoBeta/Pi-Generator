package com.yihaobeta.picomputor.common

import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView


/**
 * 一些通用方法
 */

fun getTextWidth(text: String, textSize: Float): Float {
    val paint = Paint()
    paint.textSize = textSize
    return paint.measureText(text)
}

fun getSingleLineMaxCount(textView: AppCompatTextView): Int {
    val textSize = textView.textSize
    val width = textView.width
    val textWith = getTextWidth("8", textSize)
    return (width / textWith).toInt()
}

fun getTextViewLines(textView: TextView, textViewWidth: Int): Int {
    val width = textViewWidth - textView.compoundPaddingLeft - textView.compoundPaddingRight
    val staticLayout: StaticLayout
    staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getStaticLayout23(textView, width)
    } else {
        getStaticLayout(textView, width)
    }
    val lines = staticLayout.lineCount
    val maxLines = textView.maxLines
    return if (maxLines > lines) {
        lines
    } else maxLines
}

/**
 * sdk>=23
 */
@RequiresApi(api = Build.VERSION_CODES.M)
private fun getStaticLayout23(textView: TextView, width: Int): StaticLayout {
    val builder = StaticLayout.Builder.obtain(
        textView.text,
        0, textView.text.length, textView.paint, width
    )
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
        .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
        .setIncludePad(textView.includeFontPadding)
        .setBreakStrategy(textView.breakStrategy)
        .setHyphenationFrequency(textView.hyphenationFrequency)
        .setMaxLines(if (textView.maxLines == -1) Integer.MAX_VALUE else textView.maxLines)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        builder.setJustificationMode(textView.justificationMode)
    }
    if (textView.ellipsize != null && textView.keyListener == null) {
        builder.setEllipsize(textView.ellipsize)
            .setEllipsizedWidth(width)
    }
    return builder.build()
}

/**
 * sdk<23
 */
private fun getStaticLayout(textView: TextView, width: Int): StaticLayout {
    return StaticLayout(
        textView.text,
        0, textView.text.length,
        textView.paint, width, Layout.Alignment.ALIGN_NORMAL,
        textView.lineSpacingMultiplier,
        textView.lineSpacingExtra, textView.includeFontPadding, textView.ellipsize,
        width
    )
}