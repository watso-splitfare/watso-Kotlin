package com.example.saengsaengtalk.fragmentBaedal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ArrayAdapter.createFromResource
import android.widget.ListAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterBaedal.BaedalConfirmMenu
import com.example.saengsaengtalk.adapterBaedal.BaedalOrder
import com.example.saengsaengtalk.adapterBaedal.BaedalOrderAdapter
import com.example.saengsaengtalk.adapterBaedal.BaedalOrderOpt
import com.example.saengsaengtalk.databinding.FragBaedalAddBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalAdd :Fragment() {
    var baedalfee = 10000
    var orderPrice = 0
    var totalPrice = 0

    var decDt = DecimalFormat("00")
    val decPrice = DecimalFormat("#,###")

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

        val storeList = arrayOf<String>("네네치킨", "BBQ", "맘스터치치")
        val searchmethod = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,storeList)
        searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnStore!!.adapter = searchmethod

        binding.lytChoice.setOnClickListener {
            setFrag(FragmentBaedalMenu(), mapOf("postNum" to "0", "member" to "0", "isPosting" to "true"))
        }

        setText()

        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("ConfirmToAdd", this) { requestKey, bundle ->

            val menu: MutableList<BaedalConfirmMenu> = bundle.get("menu") as MutableList<BaedalConfirmMenu>

            val baedalOrder = mutableListOf<BaedalOrder>()
            var totalPrice = 0
            for (i in menu) {
                val menuName = i.menu
                totalPrice += i.price
                val count = i.count

                val baedalOrderOpt = mutableListOf<BaedalOrderOpt>()
                for (j in i.optList) {
                    baedalOrderOpt.add(BaedalOrderOpt(j.optPrice))
                }
                baedalOrder.add(BaedalOrder(menuName, count, baedalOrderOpt))
            }

            binding.rvMenuList.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = BaedalOrderAdapter(requireContext(), baedalOrder)
            binding.rvMenuList.adapter = adapter

            binding.tvOrderPrice.text = "${decPrice.format(totalPrice)}원"
            binding.tvBaedalFee.text = "${decPrice.format(baedalfee)}원"
            binding.tvTotalPrice.text = "${decPrice.format(totalPrice+baedalfee)}원"

            if (totalPrice > 0) {
                binding.btnPostAdd.isEnabled = true
                binding.btnPostAdd.setBackgroundResource(R.drawable.btn_baedal_confirm)
                binding.btnPostAdd.setOnClickListener {
                    setFrag(FragmentBaedalPost(), mapOf("postNum" to "0"))
                }
            }
        }
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

    fun setText() {
        binding.tvBaedalFeeContent.text = "${decPrice.format(baedalfee)}원"
        binding.tvBaedalFee.text = "${decPrice.format(baedalfee)}원"
        binding.tvOrderPrice.text = "${decPrice.format(orderPrice)}원"
        binding.tvTotalPrice.text = "${decPrice.format(totalPrice)}원"

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