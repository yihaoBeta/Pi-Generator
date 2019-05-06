package com.yihaobeta.picomputor.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.yihaobeta.picomputor.R
import com.yihaobeta.picomputor.common.Common
import com.yihaobeta.picomputor.common.PreferenceDelegate
import com.yihaobeta.picomputor.common.toast
import com.yihaobeta.picomputor.function.NativeHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private val nativeHelper: NativeHelper by lazy {
        NativeHelper.instance.apply {
            setHandler(mainHandler)
        }
    }

    private var infiniteMode: Boolean by PreferenceDelegate(
        this,
        Common.PREFERENCE_NAME,
        Common.MODE_KEY,
        false
    )
    private var speed: Int by PreferenceDelegate(
        this,
        Common.PREFERENCE_NAME,
        Common.SPEED_KEY,
        3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name_zhCN)
        //设置初始状态
        modeSwitch.isChecked = infiniteMode
        if(infiniteMode){
            countEt.isEnabled = false
        }
        seekBar.progress = speed
        nativeHelper.setSpeed(speed)

        initViews()
    }

    private fun initViews() {

        //速度调节
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speed = progress
                nativeHelper.setSpeed(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //无限模式开关
        modeSwitch.setOnCheckedChangeListener { _, isActive ->
            if (isActive) {
                countEt.isEnabled = false
                infiniteMode = true
            } else {
                countEt.isEnabled = true
                infiniteMode = false
            }
        }

        //开始/停止计算
        toggleButton.setOnCheckedChangeListener { v, isChecked ->
            nativeHelper.apply {
                if (isChecked) {
                    if (infiniteMode) {//无限模式
                        //用-1代表无限模式(不是真的无限，不过以现有手机能力，有限年内应该计算不完)
                        //如果真的长时间算下去，设备内存会消耗完毕
                        //TextView不可能无限显示，并且程序并没有做缓存
                        compute(-1)
                        countEt.isEnabled = false
                        modeSwitch.isEnabled = false
                    } else {//有限位数模式
                        countEt.text.let {
                            if (it.isNotBlank() && it.isNotEmpty()) {
                                var countToCalculate: Int
                                try {
                                    countToCalculate = it.toString().toInt()
                                } catch (e: Exception) {
                                    if (e is NumberFormatException) {
                                        toast(getString(R.string.tips_input_is_outof_int_range))
                                    } else {
                                        toast(getString(R.string.tips_error_input))
                                    }
                                    v.isChecked = false
                                    countEt.text.clear()
                                    return@let
                                }
                                if (countToCalculate <= 0) {
                                    toast(getString(R.string.tips_count_warning))
                                    v.isChecked = false
                                } else {
                                    compute(countToCalculate + 1)//从小数点后算起，所以计算的时候加上小数点前的3
                                    countEt.isEnabled = false
                                    modeSwitch.isEnabled = false
                                }
                            } else {
                                toast(getString(R.string.tips_no_input))
                                v.isChecked = false
                            }
                        }
                    }
                } else {//停止计算
                    stop()
                    if (!infiniteMode) {
                        countEt.isEnabled = true
                    }
                    modeSwitch.isEnabled = true
                }
            }
        }
    }

    /**
     * 消息处理，用来处理NDK返回的计算结果
     */
    private val mainHandler = Handler(Looper.getMainLooper()) {
        if (it.what == NativeHelper.MESSAGE_WHAT) {
            val bundle = it.data
            val state = bundle.getInt(NativeHelper.STATE)
            val result = bundle.getString(NativeHelper.RESULT)
            val count = bundle.getString(NativeHelper.COUNT)
            if (result == null) {//计算出错
                toast(getString(R.string.error_compute))
                nativeHelper.stop()
                countEt.isEnabled = true
                toggleButton.isChecked = false
                return@Handler true
            }
            when (state) {
                NativeHelper.STATE_START -> {//开始计算，清空之前的计算内容
                    toast(getString(R.string.start_compute))
                    scrollview.reset()
                }
                NativeHelper.STATE_RUNNING -> {//正在计算中..
                    //3.1415926...添加小数点
                    if (count == BigDecimal.ZERO.toEngineeringString() && result == "3") {
                        scrollview.setResult("3.")
                    } else {
                        scrollview.setResult(result)
                    }
                    //设置当前位数
                    totalCountTv.text = count
                    //自动滚动到底部
                    scrollview.fullScroll(ScrollView.FOCUS_DOWN)
                }

                NativeHelper.STATE_COMPLETE -> {//计算完成
                    toast(getString(R.string.complete_compute))
                    if (!infiniteMode)
                        countEt.isEnabled = true
                    toggleButton.isChecked = false
                }
            }
            true
        } else
            false
    }


    override fun onStop() {
        super.onStop()
        //避免后台无限计算...
        nativeHelper.stop()
    }

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
