package com.example.saengsaengtalk.fragmentTaxi

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.APIS.BaedalPostingResponse
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostingModel
import com.example.saengsaengtalk.APIS.PostingResponse
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragTaxiAddBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentTaxiAdd :Fragment() {
    var userId = MainActivity.prefs.getString("userId", "-1").toLong()

    @RequiresApi(Build.VERSION_CODES.O)
    var departTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")).toString()

    var currentMember = 1
    var minMember = 2
    var maxMember = 4

    private var mBinding: FragTaxiAddBinding? = null
    private val binding get() = mBinding!!
    val api = APIS.create()
    var decDt = DecimalFormat("00")


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragTaxiAddBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.lytTable.visibility = View.GONE

        binding.lytTime.setOnClickListener { showCalendar() }
        binding.tvTime.text = departTime

        setMemberCountButton()

        binding.btnPostAdd.setOnClickListener { taxiPosting() }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showCalendar() {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            run {
                var dateString = "${year}-${decDt.format(month + 1)}-${decDt.format(dayOfMonth)}T"

                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        run {
                            var timeString = "${decDt.format(hourOfDay)}:${decDt.format(minute)}:00"
                            departTime = LocalDateTime.parse(dateString+timeString).toString()
                            //println(orderTime)
                            binding.tvTime.text = getDateTimeFormating(departTime.toString())
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
    fun getDateTimeFormating(dateTimeStr: String): String {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
        return dateTime.format(formatter)
    }

    fun setMemberCountButton() {
        binding.btnAdd.setOnClickListener {
            if (currentMember < 4) {
                currentMember += 1
                binding.tvMember.text = currentMember.toString()
            }
        }
        binding.btnMinAdd.setOnClickListener {
            if (minMember < 4) {
                minMember += 1
                binding.tvMinMember.text = minMember.toString()
            }
        }
        binding.btnMaxAdd.setOnClickListener {
            if (maxMember < 4) {
                maxMember += 1
                binding.tvMaxMember.text = maxMember.toString()
            }
        }
        binding.btnSub.setOnClickListener {
            if (1 < currentMember) {
                currentMember -= 1
                binding.tvMember.text = currentMember.toString()
            }
        }
        binding.btnMinSub.setOnClickListener {
            if (1 < minMember) {
                minMember -= 1
                binding.tvMinMember.text = minMember.toString()
            }
        }
        binding.btnMaxSub.setOnClickListener {
            if (1 < maxMember) {
                maxMember -= 1
                binding.tvMaxMember.text = maxMember.toString()
            }
        }
    }

    fun taxiPosting() {
        val taxiPostingModel = TaxiPostingModel(
            userId,
            "생자대",
            "밀양역",
            binding.etTitle.text.toString(),
            binding.etContent.text.toString(),
            departTime,
            minMember,
            maxMember
        )
        api.taxiPosting(taxiPostingModel).enqueue(object : Callback<PostingResponse> {
            override fun onResponse(call: Call<PostingResponse>,response: Response<PostingResponse>) {
                println("성공")
                Log.d("log", response.toString())
                Log.d("log", response.body().toString())
                val result = response.body()!!
                println(result)

                val bundle = bundleOf("success" to result.success, "postId" to result.post_id)
                println(bundle)
                getActivity()?.getSupportFragmentManager()?.setFragmentResult("updatePost", bundle)
                onBackPressed()
            }

            override fun onFailure(call: Call<PostingResponse>, t: Throwable) {
                println("실패")
                Log.d("log", t.message.toString())
                Log.d("log", "fail")
            }
        })
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