package com.watso.app.fragmentBaedal.BaedalAdd

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.constraintlayout.widget.ConstraintLayout
import com.watso.app.R

class DialogTimePicker (
    context: Context,
    private val timePickerClickListener: TimePickerClickListener
) : Dialog(context) {

    private lateinit var timePicker: TimePicker
    private lateinit var btnPositive: ConstraintLayout
    private lateinit var btnNegative: ConstraintLayout

    private var text: String? = null
    private var title: String? = null

    private var setHourValue: Int = 0
    private var setMinuteValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_timepicker)

        timePicker = findViewById(R.id.timePicker)
        btnPositive = findViewById(R.id.btn_positive)
        btnNegative = findViewById(R.id.btn_negative)

        timePicker.hour = setHourValue
        timePicker.minute = setMinuteValue
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            setHourValue = hourOfDay
            setMinuteValue = minute
        }

        btnPositive.setOnClickListener {
            timePickerClickListener.onPositiveClick(setHourValue, setMinuteValue)
            dismiss()
        }

        btnNegative.setOnClickListener {
            timePickerClickListener.onNegativeClick()
            dismiss()
        }
    }

    fun setText(text: String) {
        this.text = text
    }

    fun setHourValue(setHourValue: Int) {
        this.setHourValue = setHourValue
    }

    fun setMinuteValue(setMinuteValue: Int) {
        this.setMinuteValue = setMinuteValue
    }

    interface TimePickerClickListener {
        fun onPositiveClick(hour: Int, minute: Int)
        fun onNegativeClick()
    }

    companion object {
        private const val TAG = "MainActivity.TAG"
    }
}