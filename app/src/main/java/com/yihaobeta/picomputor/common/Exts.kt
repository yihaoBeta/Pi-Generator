package com.yihaobeta.picomputor.common

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat

/**
 * kotlin扩展方法
 */
fun Context.toast(str: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, str, duration).show()
}

fun AppCompatTextView.setTextFuture(charSequence: CharSequence) {
    this.setTextFuture(
        PrecomputedTextCompat.getTextFuture(
            charSequence,
            TextViewCompat.getTextMetricsParams(this),
            null
        )
    )
}

fun Any.log(msg: Any, tag: String = "TAG", level: Int = Log.DEBUG) {
    when (level) {
        Log.DEBUG -> {
            Log.d(tag, msg.toString())
        }
        Log.WARN -> {
            Log.w(tag, msg.toString())
        }
        Log.ERROR -> {
            Log.e(tag, msg.toString())
        }
        Log.INFO -> {
            Log.i(tag, msg.toString())
        }
        Log.VERBOSE -> {
            Log.v(tag, msg.toString())
        }
        else -> Log.d(tag, msg.toString())
    }
}