package com.saengsaengtalk.app.fragmentKara

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragKaraUseBinding
import com.saengsaengtalk.app.fragmentKara.adapter.KaraSpinner
import com.saengsaengtalk.app.fragmentKara.adapter.KaraSpinnerAdapter
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentKaraUse :Fragment() {
    var num: Int? = null
    var use: String? = null

    private var mBinding: FragKaraUseBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragKaraUseBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            num = it.getString("num")!!.toInt()-1
            use = it.getString("use")
        }
        println("num")
        println("use")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        var spinners = mutableListOf(
            KaraSpinner("1번방", true),
            KaraSpinner("2번방", true),
            KaraSpinner("3번방", false),
            KaraSpinner("4번방", true),
            KaraSpinner("5번방", false),
            KaraSpinner("6번방", false),
            KaraSpinner("7번방", true))

        binding.spnNumber.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                setUsingTime(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.spnNumber.adapter = KaraSpinnerAdapter(requireContext(), spinners)
        binding.spnNumber.setSelection(num!!)

        /************코드 수정할것************/
        setUsingTime(num!!)

        binding.lytStartTime.setOnClickListener { showClock() }

        binding.btnUse.setOnClickListener { onBackPressed() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setUsingTime(num:Int) {
        val usee = mutableListOf("사용하기", "사용하기", "15:03~15:13", "사용하기",
            "15:10~15:30", "15:05~15:15", "사용하기")

        if (usee[num] == "사용하기") {
            binding.ivCalendar.setVisibility(View.VISIBLE)
            val startTime = LocalDateTime.now()
            binding.tvStartTime.text = startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            binding.tvEndTime.text = startTime.plusMinutes(20).format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            val usingTime = usee[num].split("~")
            binding.ivCalendar.setVisibility(View.GONE)
            binding.tvStartTime.text = usingTime[0]
            binding.tvEndTime.text = usingTime[1]
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun showClock() {
        var decDt = DecimalFormat("00")
        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute  ->
            run {
                var startTime = "${decDt.format(hourOfDay)}:${decDt.format(minute)}"
                var endTime = "${decDt.format(hourOfDay)}:${decDt.format(minute+20)}"
                binding.tvStartTime.text = startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                binding.tvEndTime.text = endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }

        TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }
}