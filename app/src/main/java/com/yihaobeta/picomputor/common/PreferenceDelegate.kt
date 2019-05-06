package com.yihaobeta.picomputor.common

import android.content.Context
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferenceDelegate<T>(context: Context, val name: String, val key: String, private val default: T) :
    ReadWriteProperty<Any?, T> {

    private val sharedPreference by lazy {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (default) {
            is Boolean -> {
                sharedPreference.getBoolean(key, default) as T
            }
            is String -> {
                sharedPreference.getString(key, default) as T
            }
            is Int -> {
                sharedPreference.getInt(key, default) as T
            }
            else -> throw IllegalArgumentException("不支持当前类型")
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        log("setValue:$value")
        when (value) {
            is Boolean -> {
                sharedPreference.edit().putBoolean(key, value as Boolean).apply()
            }

            is String -> {
                sharedPreference.edit().putString(key, value as String).apply()
            }
            is Int -> {
                sharedPreference.edit().putInt(key, value as Int).apply()
            }
            else -> {
                throw IllegalArgumentException("不支持当前类型")
            }
        }
    }

}