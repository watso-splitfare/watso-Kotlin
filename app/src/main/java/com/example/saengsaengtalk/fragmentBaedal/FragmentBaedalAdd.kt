package com.example.saengsaengtalk.fragmentBaedal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragBaedalAddBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalAdd :Fragment() {
    //var dateString = ""
    //var timeString = ""
    //var orderTime: LocalDateTime? = null
    var dec = DecimalFormat("00")

    private var mBinding: FragBaedalAddBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalAddBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        val now = LocalDateTime.now()
        binding.tvOrderTime.text = getDateTimeString(now)

        binding.lytTime.setOnClickListener { showCalendar() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showCalendar() {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            run {
                var dateString = "${year}-${dec.format(month + 1)}-${dec.format(dayOfMonth)}T"

                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        run {
                            var timeString = "${dec.format(hourOfDay)}:${dec.format(minute)}:00"
                            var orderTime = LocalDateTime.parse(dateString+timeString)
                            binding.tvOrderTime.text = getDateTimeString(orderTime)
                        }
                    }

                TimePickerDialog(requireContext(), timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
            }
        }

        val dpd = DatePickerDialog(requireContext(), dateSetListener,
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dpd.datePicker.minDate = System.currentTimeMillis() - 1000;
        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateTimeString(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern(
            "MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko")))
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}