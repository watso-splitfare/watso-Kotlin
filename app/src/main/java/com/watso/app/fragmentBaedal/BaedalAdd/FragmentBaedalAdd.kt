package com.watso.app.fragmentBaedal.BaedalAdd

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.watso.app.API.*
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragBaedalAddBinding
import com.watso.app.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.fragmentAccount.FragmentUpdateAccount
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalAdd :Fragment(), View.OnTouchListener {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    var mBinding: FragBaedalAddBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragBaedalAdd"
    val api = API.create()
    val gson = Gson()
    val decDt = DecimalFormat("00")

    var isScrolled = false
    var isUpdating = false
    var postId:String? = null
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
    var selectedStore: Store? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isUpdating = it.getString("isUpdating").toBoolean()
            if (isUpdating) {
                postId = it.getString("postId")
                orderTime = it.getString("orderTime")
                selectedStore = Gson().fromJson(it.getString("storeInfo"), Store::class.java)
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
        AC = ActivityController(activity as MainActivity)

        refreshView()
        binding.scrollView2.setOnTouchListener(this)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AC.hideSoftInput()
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> isScrolled = true
            MotionEvent.ACTION_UP -> {
                if (!isScrolled) AC.hideSoftInput()
                isScrolled = false
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {

        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }
        binding.lytTime.setOnClickListener { showCalendar() }

        val places = listOf("생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(fragmentContext, android.R.layout.simple_list_item_1, places)
        binding.spnPlace.adapter = placeSpinerAdapter
        binding.etMinMember.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onMemberChanged() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etMaxMember.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onMemberChanged() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        if (isUpdating) {
            binding.tvOrderTime.text = getDateTimeFormating(orderTime!!)
            binding.lytStore.visibility = View.GONE
            binding.tvStore.text = selectedStore!!.name

            if (place == "기숙사") binding.spnPlace.setSelection(1)
            binding.etMinMember.setText(minMember.toString())
            binding.etMaxMember.setText(maxMember.toString())

            bindStoreInfo()
            binding.tvCompletePostinfo.text = "수정 완료"
        } else {
            val currentDateTime = LocalDateTime.now()
            val roundedMinute = (currentDateTime.minute / 10) * 10
            val tartgetDatetime = currentDateTime.withMinute(roundedMinute).plusMinutes(30)

            orderTime = tartgetDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")).toString()
            binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
            setStoreSpinner()
        }
        binding.btnCompletePostinfo.setBackgroundResource(R.drawable.solid_primary_10)
        binding.btnCompletePostinfo.setOnClickListener { btnCompletePostInfo() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showCalendar() {
        val orderTimeObj = LocalDateTime.parse(orderTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            run {
                var dateString = "${year}-${decDt.format(month + 1)}-${decDt.format(dayOfMonth)}T"
                val timePicker = DialogTimePicker(fragmentContext, object: DialogTimePicker.TimePickerClickListener {
                    override fun onPositiveClick(hour: Int, minute: Int) {
                        var timeString = "${decDt.format(hour)}:${decDt.format(minute)}:00"
                        orderTime = dateString+timeString
                        binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
                    }

                    override fun onNegativeClick() { }
                })

                timePicker.setHourValue(orderTimeObj.hour)
                timePicker.setMinuteValue(orderTimeObj.minute)
                timePicker.setCanceledOnTouchOutside(true)
                timePicker.setCancelable(true)
                timePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                timePicker.window?.requestFeature(Window.FEATURE_NO_TITLE)
                timePicker.show()
            }
        }

        val dpd = DatePickerDialog(fragmentContext, dateSetListener,
            orderTimeObj.year, orderTimeObj.monthValue-1, orderTimeObj.dayOfMonth)
        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.datePicker.maxDate = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7
        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateTimeFormating(dateTimeStr: String): String {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
        return dateTime.format(formatter)
    }

    fun setStoreSpinner(){
        AC.showProgressBar()
        api.getStoreList().enqueue(object : Callback<List<Store>> {
            override fun onResponse(call: Call<List<Store>>, response: Response<List<Store>>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    stores = response.body()!!
                    stores.forEach {
                        storeIds.add(it._id)
                        storeNames.add(it.name)
                        storeFees.add(it.fee)
                    }
                    storeName = storeNames[0]
                    val searchmethod = ArrayAdapter(fragmentContext, android.R.layout.simple_spinner_item, storeNames)

                    searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spnStore.setTitle("")
                    binding.spnStore.adapter = searchmethod
                    binding.spnStore.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                baedalfee = storeFees[position]
                                selectedIdx = position
                                selectedStore = stores[position]
                                bindStoreInfo()
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        AC.makeToast(errorResponse.msg)
                        Log.d("$TAG[getStoreList]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) { Log.e("$TAG[getStoreList]", e.toString())}
                    AC.onBackPressed()
                }
            }

            override fun onFailure(call: Call<List<Store>>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("FragBaedalAdd setStoreSpinner", t.message.toString())
                AC.makeToast("가게 리스트 조회 실패")
                AC.onBackPressed()
            }
        })
    }

    fun bindStoreInfo() {
        binding.tvTelNum.text = "가게번호 : ${selectedStore!!.telNum}"
        binding.tvMinOrder.text = "최소 주문 금액 : ${selectedStore!!.minOrder}원"
        binding.tvFee.text = "배달비 : ${selectedStore!!.fee}원"
        var noteStr = ""
        for ((idx, note)in selectedStore!!.note.withIndex()) {
            if (note.trim() != "") {
                noteStr += "·"
                noteStr += note
                if (idx < selectedStore!!.note.size - 1)
                    noteStr += "\n"
            }
        }
        if (noteStr.trim() == "") noteStr = "없음"
        binding.tvNote.text = noteStr
    }

    fun onMemberChanged() {
        val min = binding.etMinMember.text.toString()
        val max = binding.etMaxMember.text.toString()
        val memberAlert = binding.tvMemberAlert
        binding.btnCompletePostinfo.setBackgroundResource(R.drawable.solid_gray_10)
        binding.btnCompletePostinfo.isEnabled = false

        if (min != "" && max != "") {
            when {
                min.toInt() > max.toInt() -> memberAlert.text = "최소주문 인원은 최대주문 인원보다 많을 수 없습니다."
                min.toInt() < 2 -> memberAlert.text = "최소주문 인원은 2명 이상이어야 합니다."
                else -> {
                    memberAlert.text = ""
                    binding.btnCompletePostinfo.setBackgroundResource(R.drawable.solid_primary_10)
                    binding.btnCompletePostinfo.isEnabled = true
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun btnCompletePostInfo() {
        if (!isPostAbleTime()) {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setTitle("게시글 작성 불가")
                .setMessage("주문은 현재시간부터 10분 이후로 등록 가능합니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()

            return
        }

        val minMember = if (binding.etMinMember.text.toString() != "")
            binding.etMinMember.text.toString().toInt() else 2
        val maxMember = if (binding.etMaxMember.text.toString() != "")
            binding.etMaxMember.text.toString().toInt() else 100

        if (isUpdating) {
            /** 게시글 수정 */
            val baedalPostUpdate = BaedalPostUpdate(
                orderTime!!,
                binding.spnPlace.selectedItem.toString(),
                minMember,
                maxMember
            )

            AC.showProgressBar()
            api.updateBaedalPost(postId!!, baedalPostUpdate)
                .enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        AC.hideProgressBar()
                        if (response.code() == 204) {
                            val bundle = bundleOf("success" to true, "postId" to postId)
                            getActivity()?.getSupportFragmentManager()?.setFragmentResult("updatePost", bundle)
                            getActivity()?.getSupportFragmentManager()?.setFragmentResult("backToBaedalList", bundleOf())
                            AC.onBackPressed()
                        } else {
                            Log.e("baedalAdd Fragment - updateBaedalPost", response.toString())
                            AC.makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        AC.hideProgressBar()
                        Log.e("baedalAdd Fragment - updateBaedalPost", t.message.toString())
                        AC.makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                    }
                })
        } else {
            /** 게시글 신규 등록 */
            val accountInfo = "${AC.getString("accountNum")} (${AC.getString("name")})"
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setTitle("계좌정보 확인")
                .setMessage("계좌정보가 정확한지 확인해 주세요.\n${accountInfo}")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    goToFragBaedalMenu(minMember, maxMember)
                })
                .setNegativeButton("계좌번호 수정", DialogInterface.OnClickListener { dialog, id ->
                    AC.setFrag(FragmentUpdateAccount(), mapOf("target" to "accountNum"))
                })
            builder.show()
        }
    }

    fun goToFragBaedalMenu(minMember: Int, maxMember: Int) {
        val baedalPosting = BaedalPosting(
            storeIds[selectedIdx],
            orderTime!!,
            binding.spnPlace.selectedItem.toString(),
            minMember,
            maxMember,
            null
        )
        AC.setString("postOrder", "")
        AC.setString("baedalPosting", gson.toJson(baedalPosting))
        AC.setFrag( FragmentBaedalMenu(), mapOf(
            "postId" to "-1",
            "storeId" to storeIds[selectedIdx])
        )

    }

    fun isPostAbleTime(): Boolean {
        val orderDateTime = LocalDateTime.parse(orderTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        val now = LocalDateTime.now()
        val diff = Duration.between(now, orderDateTime).toMinutes()
        Log.d(TAG, orderDateTime.toString())
        Log.d(TAG, now.toString())
        Log.d(TAG, diff.toString())
        Log.d(TAG, orderDateTime.isAfter(now).toString())
        return (orderDateTime.isAfter(now) && diff > 10)
    }
}