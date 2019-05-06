package com.yihaobeta.picomputor.function

/**
 * 回调接口
 */
interface PiComputeListener {
    fun onCompute(state: Int, result: Int)
}