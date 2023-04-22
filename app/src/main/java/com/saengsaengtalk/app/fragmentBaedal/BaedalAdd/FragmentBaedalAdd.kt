package com.saengsaengtalk.app.fragmentBaedal.BaedalAdd

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragBaedalAddBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalAdd :Fragment() {
    var isUpdating = false
    var postId:String? = null
    var title: String? = null
    var content: String? = null
    var orderTime: String? = null
    var storeName: String? = null
    var place: String? = null
    var minMember: Int? = null
    var maxMember: Int? = null
    var fee: Int? = null

    var baedalfee = 0

    var stores = listOf<Store>()
    var storeIds = mutableListOf<String>()
    var storeNames = mutableListOf<String>()
    var storeFees = mutableListOf<Int>()
    var selectedIdx = 0

    private var mBinding: FragBaedalAddBinding? = null
    private val binding get() = mBinding!!
    val gson = Gson()
    val api = APIS.create()
    var decDt = DecimalFormat("00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isUpdating = it.getString("isUpdating").toBoolean()
            if (isUpdating) {
                postId = it.getString("postId")
                orderTime = it.getString("orderTime")
                storeName = it.getString("storeName")
                place = it.getString("place")
                minMember = it.getString("minMember")?.toInt()
                maxMember = it.getString("maxMember")?.toInt()
                fee = it.getString("fee")?.toInt()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalAddBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.lytTime.setOnClickListener { showCalendar() }

        val places = listOf("생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, places)
        binding.spnPlace.adapter = placeSpinerAdapter

        if (isUpdating) {
            binding.tvOrderTime.text = getDateTimeFormating(orderTime!!)
            binding.lytStore.visibility = View.GONE
            binding.tvStoreName.text = storeName

            if (place == "기숙사") binding.spnPlace.setSelection(1)
            if (minMember != 0) {
                binding.cbMinMember.setChecked(true)
                binding.etMinMember.setText(minMember.toString())
            }
            if (maxMember != 0) {
                binding.cbMaxMember.setChecked(true)
                binding.etMaxMember.setText(maxMember.toString())
            }
            binding.tvCompletePostinfo.text = "수정 완료"
        } else {
            orderTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")).toString()
            binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
            setStoreSpinner()
        }
        binding.btnCompletePostinfo.setBackgroundResource(R.drawable.btn_baedal_confirm)
        binding.btnCompletePostinfo.setOnClickListener { btnCompletePostInfo() }
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
                            var timeString = "${decDt.format(hourOfDay)}:${decDt.format(minute)}:01"
                            orderTime = LocalDateTime.parse(dateString+timeString).toString()
                            binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
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
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
        return dateTime.format(formatter)
    }

    fun setStoreSpinner(){
        val loopingDialog = looping()
        api.getStoreList().enqueue(object : Callback<List<Store>> {
            override fun onResponse(call: Call<List<Store>>, response: Response<List<Store>>) {
                looping(false, loopingDialog)
                if (response.code() == 200) {
                    stores = response.body()!!
                    stores.forEach {
                        storeIds.add(it._id)
                        storeNames.add(it.name)
                        storeFees.add(it.fee)
                    }
                    storeName = storeNames[0]
                    val searchmethod = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, storeNames)

                    searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spnStore!!.adapter = searchmethod
                    binding.spnStore.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                baedalfee = storeFees[position]
                                selectedIdx = position
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                } else {
                    Log.e("FragBaedalAdd setStoreSpinner", response.toString())
                    makeToast("가게 리스트 조회 실패")
                }
            }

            override fun onFailure(call: Call<List<Store>>, t: Throwable) {
                // 실패
                looping(false, loopingDialog)
                Log.e("FragBaedalAdd setStoreSpinner", t.message.toString())
                makeToast("가게 리스트 조회 실패")
                onBackPressed()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun btnCompletePostInfo() {
        val minMember = if (binding.cbMinMember.isChecked && binding.etMinMember.text.toString() != "")
            binding.etMinMember.text.toString().toInt() else 1
        val maxMember = if (binding.cbMaxMember.isChecked && binding.etMaxMember.text.toString() != "")
            binding.etMaxMember.text.toString().toInt() else 999

        if (isUpdating) {
            /** 게시글 수정 */
            val baedalPostUpdate = BaedalPostUpdate(
                orderTime!!,
                binding.spnPlace.selectedItem.toString(),
                minMember,
                maxMember
            )

            val loopingDialog = looping()
            api.updateBaedalPost(postId!!, baedalPostUpdate)
                .enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            val bundle = bundleOf("success" to true, "postId" to postId)
                            getActivity()?.getSupportFragmentManager()?.setFragmentResult("updatePost", bundle)
                            onBackPressed()
                        } else {
                            Log.e("baedalAdd Fragment - updateBaedalPost", response.toString())
                            makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e("baedalAdd Fragment - updateBaedalPost", t.message.toString())
                        makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                    }
                })
        } else {
            /** 게시글 신규 등록 */
            var orderTimeString = orderTime!!//formattedToDateTimeString(binding.tvOrderTime.text.toString())

            val baedalPosting = BaedalPosting(
                storeIds[selectedIdx],
                orderTimeString,
                binding.spnPlace.selectedItem.toString(),
                minMember,
                maxMember,
                "",
                null
            )
            val prefs = MainActivity.prefs
            prefs.setString("postOrder", "")
            prefs.setString("baedalPosting", gson.toJson(baedalPosting))
            setFrag( FragmentBaedalMenu(), mapOf(
                "postId" to "-1",
                "storeId" to storeIds[selectedIdx])
            )
        }
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, popBackStack)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}