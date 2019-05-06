package com.yihaobeta.picomputor.function

import android.os.Bundle
import android.os.Handler
import java.math.BigDecimal

class NativeHelper private constructor() {
    companion object {
        const val STATE_START = 0x01
        const val STATE_RUNNING = 0x02
        const val STATE_COMPLETE = 0x03
        const val STATE_ERROR = 0x04
        const val STATE = "state"
        const val RESULT = "result"
        const val COUNT = "count"
        const val MESSAGE_WHAT = 0x111

        //单例模式
        val instance: NativeHelper
            get() = InstanceHolder.instance
    }

    private var totalCount = BigDecimal.ZERO
    private var mainHandler: Handler? = null
    private var mSpeed = 3//[0,100]

    private object InstanceHolder {
        internal var instance = NativeHelper()
    }

    fun setHandler(handler: Handler) {
        this.mainHandler = handler
    }

    /**
     * 调用Native方法计算圆周率
     * @param n 要计算的位数
     */
    fun compute(n: Int): Int {
        return computePiByNative(n, object : PiComputeListener {
            //这个回调是NDK在新的线程中返回的，不是UI线程
            override fun onCompute(state: Int, result: Int) {
                mainHandler?.apply {
                    while (this.hasMessages(MESSAGE_WHAT)) {
                        //等待UI处理完之前的数据，NDK计算速度大于UI的处理速度，避免消息队列过于庞大
                        Thread.sleep(1)
                    }
                }

                //for speed[0,100]
                Thread.sleep((1.5 * mSpeed).toLong())

                handlerResult(state, result, totalCount.toEngineeringString())
                if (state == STATE_RUNNING) {
                    totalCount++
                }
                if (state == STATE_COMPLETE) {
                    totalCount = BigDecimal.ZERO
                }
            }
        })
    }

    /**
     * 处理NDK返回的数据，handler发送给UI线程
     * @param state 计算状态
     * @param result 计算结果，最新的一位数
     * @param curCount 当前的总位数
     *
     */
    private fun handlerResult(state: Int, result: Int, curCount: String) {
        mainHandler?.apply {
            val message = this.obtainMessage()
            message.what = MESSAGE_WHAT
            val bundle = Bundle()
            bundle.putInt(STATE, state)
            bundle.putString(RESULT, result.toString())
            bundle.putString(COUNT, curCount)
            message.data = bundle
            this.sendMessage(message)
        }
    }

    /**
     * 停止计算
     */
    fun stop() {
        stopCompute()
    }

    /**
     * 设置计算速度（其实是UI显示速度，显示速度过快会导致TextView频繁刷新，UI容易卡顿）
     * @param speed 速度[0,100]
     */
    fun setSpeed(speed: Int) {
        this.mSpeed = speed
    }

    /**
     * @param n 计算的位数
     * @param listener 回调接口
     */
    private external fun computePiByNative(n: Int, listener: PiComputeListener): Int

    private external fun stopCompute()
}
