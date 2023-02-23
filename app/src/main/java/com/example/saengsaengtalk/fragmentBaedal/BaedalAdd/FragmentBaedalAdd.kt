package com.example.saengsaengtalk.fragmentBaedal.BaedalAdd

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.LoopingDialog
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragBaedalAddBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm.SelectedMenuAdapter
import com.example.saengsaengtalk.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.example.saengsaengtalk.fragmentBaedal.BaedalOrder
import com.example.saengsaengtalk.fragmentBaedal.Group
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
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
    var orderPrice = 0
    var orders = JSONArray()

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
    val decPrice = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isUpdating = it.getString("isUpdating").toBoolean()
            if (isUpdating) {
                postId = it.getString("postId")
                title = it.getString("title")
                content = it.getString("content")
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

        if (!isUpdating) {
            orderTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")).toString()
            binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
            setStoreSpinner()
            binding.lytChoice.setOnClickListener {
                setFrag(
                    FragmentBaedalMenu(), mapOf(
                        "member" to "0",
                        "isPosting" to "true",
                        "storeName" to storeNames[selectedIdx],
                        "storeId" to storeIds[selectedIdx].toString(),
                        "baedalFee" to storeFees[selectedIdx].toString(),
                        "orders" to orders.toString()
                    )
                )
            }

            getActivity()?.getSupportFragmentManager()
                ?.setFragmentResultListener("ConfirmToPosting", this) { requestKey, bundle ->
                    orders = JSONArray(bundle.getString("ordersString"))

                    binding.rvMenuList.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                    val adapter = SelectedMenuAdapter(requireContext(), orders, false)
                    binding.rvMenuList.adapter = adapter

                    orderPrice = 0
                    for (i in 0 until orders.length()) {
                        val order = orders.getJSONObject(i)
                        orderPrice += order.getInt("sumPrice") * order.getInt("count")
                    }
                    setBindText()

                    if (orderPrice > 0) {
                        binding.btnPostAdd.isEnabled = true
                        binding.btnPostAdd.setBackgroundResource(R.drawable.btn_baedal_confirm)
                    }
                }
        } else {
            binding.lytStore.visibility = View.GONE
            binding.lytChoice.visibility = View.GONE
            binding.lytTable.visibility = View.GONE

            binding.etTitle.setText(title)
            binding.etContent.setText(content)
            binding.tvOrderTime.text = getDateTimeFormating(orderTime!!)
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
            binding.tvBaedalStoreFee.text = "${decPrice.format(fee)}원"

            binding.btnPostAdd.isEnabled = true
            binding.btnPostAdd.setBackgroundResource(R.drawable.btn_baedal_confirm)
        }
        binding.btnPostAdd.setOnClickListener {
            if (binding.etTitle.text.toString() == "") {
                makeToast("제목을 입력해주세요.")
            } else {
                baedalPosting()
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
                            orderTime = LocalDateTime.parse(dateString+timeString).toString()
                            //println(orderTime)
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
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
        return dateTime.format(formatter)
    }

    fun setStoreSpinner(){
        val loopingDialog = looping()
        api.getStoreList().enqueue(object : Callback<List<Store>> {
            override fun onResponse(call: Call<List<Store>>, response: Response<List<Store>>) {
                Log.d("log", response.toString())
                Log.d("log", response.body().toString())
                if (response.code() == 200) {
                    stores = response.body()!!
                    stores.forEach {
                        storeIds.add(it._id)
                        storeNames.add(it.name)
                        storeFees.add(it.fee)
                    }

                    val searchmethod = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, storeNames)

                    searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spnStore!!.adapter = searchmethod
                    binding.spnStore.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                baedalfee = storeFees[position]
                                setBindText()
                                selectedIdx = position
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                } else {
                    Log.d("가게조회", response.toString())
                    Log.d("가게조회", response.body().toString())
                    Log.d("가게조회", response.headers().toString())

                    Log.d("log", response.toString())
                    makeToast("가게 리스트 조회 실패")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<List<Store>>, t: Throwable) {
                // 실패
                Log.d("log", t.message.toString())
                Log.d("log", "fail")
                makeToast("가게 리스트 조회 실패")
                looping(false, loopingDialog)
                onBackPressed()
            }
        })
    }

    fun setBindText() {
        binding.tvBaedalStoreFee.text = "${decPrice.format(baedalfee)}원"
        binding.tvBaedalFee.text = "${decPrice.format(baedalfee)}원"
        binding.tvOrderPrice.text = "${decPrice.format(orderPrice)}원"
        binding.tvTotalPrice.text = "${decPrice.format(baedalfee+orderPrice)}원"//"${decPrice.format(totalPrice)}원"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun baedalPosting() {
        val minMember = if (binding.cbMinMember.isChecked && binding.etMinMember.text.toString() != "")
            binding.etMinMember.text.toString().toInt() else null
        val maxMember = if (binding.cbMaxMember.isChecked && binding.etMaxMember.text.toString() != "")
            binding.etMaxMember.text.toString().toInt() else null


        if (isUpdating) {
            /** 게시글 수정 */
            val baedalUpdateModel = BaedalUpdateModel(
                postId.toString(),
                binding.tvTitle.text.toString(),
                if (binding.etContent.text.toString()!="") binding.etContent.text.toString()!! else "같이 주문해요",
                orderTime!!, // 수정하기
                binding.spnPlace.selectedItem.toString(),
                if (binding.cbMinMember.isChecked) binding.etMinMember.text.toString().toInt() else -1,
                if (binding.cbMaxMember.isChecked) binding.etMaxMember.text.toString().toInt() else -1
            )

            val loopingDialog = looping()
            api.updateBaedalPost(baedalUpdateModel)
                .enqueue(object : Callback<BaedalPostingResponse> {
                    override fun onResponse(call: Call<BaedalPostingResponse>, response: Response<BaedalPostingResponse>) {
                        if (response.code() == 200 && response.body()!!.success) {
                            val result = response.body()!!
                            val bundle = bundleOf("success" to result.success, "postId" to result.post_id)
                            getActivity()?.getSupportFragmentManager()?.setFragmentResult("updatePost", bundle)
                            onBackPressed()
                        } else {
                            Log.e("baedalAdd Fragment - updateBaedalPost", response.toString())
                            makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                        }
                        looping(false, loopingDialog)
                    }

                    override fun onFailure(call: Call<BaedalPostingResponse>, t: Throwable) {
                        Log.e("baedalAdd Fragment - updateBaedalPost", t.message.toString())
                        makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                        looping(false, loopingDialog)
                    }
                })
        } else {
            /** 게시글 신규 등록 */
            if (binding.tvTitle.text.toString() == "") makeToast("제목을 입력해 주세요")
            else if (orders.length() == 0) makeToast("메뉴를 선택해 주세요")
            else {
                makeToast("주문을 등록합니다.")
                var orderTimeString = orderTime!!//formattedToDateTimeString(binding.tvOrderTime.text.toString())
                val baedalPostModel = BaedalPostingModel(
                    storeIds[selectedIdx],
                    binding.etTitle.text.toString(),
                    if (binding.etContent.text.toString()!="") binding.etContent.text.toString()!! else "같이 주문해요",
                    orderTimeString,
                    binding.spnPlace.selectedItem.toString(),
                    if (binding.cbMinMember.isChecked) binding.etMinMember.text.toString().toInt() else -1,
                    if (binding.cbMaxMember.isChecked) binding.etMaxMember.text.toString().toInt() else -1
                )

                val loopingDialog = looping()
                api.baedalPosting(baedalPostModel)
                    .enqueue(object : Callback<BaedalPostingResponse> {
                        override fun onResponse(call: Call<BaedalPostingResponse>, response: Response<BaedalPostingResponse>) {
                            val postingResult = response.body()!!
                            if (response.code() == 200 && postingResult.success) baedalOrdering(postingResult.post_id)
                            else {
                                Log.e("baedalAdd Fragment - baedalPosting", response.toString())
                                makeToast("게시글을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                            }
                            looping(false, loopingDialog)
                        }

                        override fun onFailure(call: Call<BaedalPostingResponse>, t: Throwable) {
                            Log.e("baedalAdd Fragment - baedalPosting", t.message.toString())
                            makeToast("게시글을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                            looping(false, loopingDialog)
                        }
                    })
            }
        }
    }

    fun baedalOrdering(postId: String){
        val ordersObject: List<BaedalOrder> = gson.fromJson(orders.toString(), object: TypeToken<MutableList<BaedalOrder>>() {}.type)
        val orderings = mutableListOf<OrderingOrder>()
        for (order in ordersObject) {
            orderings.add(getOrdering(order))
        }

        val orderingModel = OrderingModel(
            storeIds[selectedIdx],
            postId,
            orderings
        )
        println(orderingModel)

        val loopingDialog = looping()
        api.baedalOrdering(orderingModel).enqueue(object : Callback<OrderingResponse> {
            override fun onResponse(call: Call<OrderingResponse>, response: Response<OrderingResponse>) {
                if (response.code() == 200 && response.body()!!.success) {
                    setFrag(FragmentBaedalPost(), mapOf("postId" to response.body()!!.post_id))
                } else {
                    Log.e("baedalAdd Fragment - baedalOrdering", response.toString())
                    makeToast("주문을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<OrderingResponse>, t: Throwable) {
                Log.e("baedalAdd Fragment - baedalOrdering", t.message.toString())
                makeToast("주문을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun getOrdering(order: BaedalOrder): OrderingOrder {
        val groups = mutableListOf<OrderingGroup>()
        for (group in order.groups) {
            groups.add(getGroup(group))
        }
        return OrderingOrder(order.count, order.menuName, groups)
    }

    fun getGroup(group: Group): OrderingGroup {
        val options = mutableListOf<String>()
        for (option in group.options){
            options.add(option.optionId!!)
        }
        return OrderingGroup(group.groupId!!, options)
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int =-1) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, popBackStack, 1)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}