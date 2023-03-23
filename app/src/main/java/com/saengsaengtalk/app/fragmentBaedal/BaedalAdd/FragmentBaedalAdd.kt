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
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragBaedalAddBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm.SelectedMenuAdapter
import com.saengsaengtalk.app.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
//import com.saengsaengtalk.app.fragmentBaedal.BaedalOrder
//import com.saengsaengtalk.app.fragmentBaedal.Group
import com.saengsaengtalk.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    var sumPrice = 0
    var orders = listOf<Order>()

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
                //title = it.getString("title")
                //content = it.getString("content")
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

        binding.etTitle.visibility = View.GONE
        binding.etContent.visibility = View.GONE

        val places = listOf("생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, places)
        binding.spnPlace.adapter = placeSpinerAdapter

        if (!isUpdating) {
            orderTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")).toString()
            binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
            setStoreSpinner()
            binding.lytChoice.setOnClickListener {
                setFrag(
                    FragmentBaedalMenu(), mapOf(
                        "member" to "0",
                        "isPosting" to "true",
                        "storeName" to storeNames[selectedIdx],
                        "storeId" to storeIds[selectedIdx],
                        "baedalFee" to storeFees[selectedIdx].toString(),
                        "orders" to orders.toString()
                    )
                )
            }

            getActivity()?.getSupportFragmentManager()
                ?.setFragmentResultListener("ConfirmToPosting", this) { requestKey, bundle ->
                    orders = gson.fromJson(bundle.getString("ordersString"), object: TypeToken<List<Order>>() {}.type) //JSONArray(bundle.getString("ordersString"))
                    Log.d("FragBaedalAdd 메뉴확정", orders.toString())
                    binding.rvMenuList.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                    val adapter = SelectedMenuAdapter(requireContext(), orders, false)
                    binding.rvMenuList.adapter = adapter

                    sumPrice = 0
                    for (order in orders/*i in 0 until orders.length()*/) {
                        //val order = orders.getJSONObject(i)
                        sumPrice += order.price * order.quantity
                    }
                    setBindText()

                    if (sumPrice > 0) {
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
        binding.btnPostAdd.setOnClickListener { baedalPosting() }
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
                            Log.d("FragBaedalAdd-timeString", timeString)
                            Log.d("FragBaedalAdd-orderTime.toString", orderTime.toString())
                            orderTime = LocalDateTime.parse(dateString+timeString).toString()
                            Log.d("FragBaedalAdd-orderTime", orderTime!!)
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
        binding.tvSumPrice.text = "${decPrice.format(sumPrice)}원"
        binding.tvTotalPrice.text = "${decPrice.format(baedalfee+sumPrice)}원"//"${decPrice.format(totalPrice)}원"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun baedalPosting() {
        val minMember = if (binding.cbMinMember.isChecked && binding.etMinMember.text.toString() != "")
            binding.etMinMember.text.toString().toInt() else null
        val maxMember = if (binding.cbMaxMember.isChecked && binding.etMaxMember.text.toString() != "")
            binding.etMaxMember.text.toString().toInt() else null


        if (isUpdating) {
            /** 게시글 수정 */
            val baedalPosting = BaedalPosting(
                null, //binding.tvTitle.text.toString(),
                orderTime!!, // 수정하기
                binding.spnPlace.selectedItem.toString(),
                if (binding.cbMinMember.isChecked && binding.etMinMember.text.toString() != "")
                    binding.etMinMember.text.toString().toInt() else 0,
                if (binding.cbMaxMember.isChecked && binding.etMaxMember.text.toString() != "")
                    binding.etMaxMember.text.toString().toInt() else 999
            )

            val loopingDialog = looping()
            api.updateBaedalPost(postId!!, baedalPosting)
                .enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        if (response.code() == 204) {
                            val bundle = bundleOf("success" to true, "postId" to postId)
                            getActivity()?.getSupportFragmentManager()?.setFragmentResult("updatePost", bundle)
                            onBackPressed()
                        } else {
                            Log.e("baedalAdd Fragment - updateBaedalPost", response.toString())
                            makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                        }
                        looping(false, loopingDialog)
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        Log.e("baedalAdd Fragment - updateBaedalPost", t.message.toString())
                        makeToast("게시글을 수정하지 못 했습니다.\n다시 시도해 주세요.")
                        looping(false, loopingDialog)
                    }
                })
        } else {
            /** 게시글 신규 등록 */
             makeToast("메뉴를 선택해 주세요")

            makeToast("주문을 등록합니다.")
            Log.d("FragBaedalAdd-storeId", storeIds[selectedIdx])
            var orderTimeString = orderTime!!//formattedToDateTimeString(binding.tvOrderTime.text.toString())
            val baedalPosting = BaedalPosting(
                storeIds[selectedIdx],
                orderTimeString,
                binding.spnPlace.selectedItem.toString(),
                if (binding.cbMinMember.isChecked && binding.etMinMember.text.toString() != "")
                    binding.etMinMember.text.toString().toInt() else 0,
                if (binding.cbMaxMember.isChecked && binding.etMaxMember.text.toString() != "")
                    binding.etMaxMember.text.toString().toInt() else 999
            )

            val loopingDialog = looping()
            Log.d("FragBaedalAdd-postingInfo", baedalPosting.toString())
            api.baedalPosting(baedalPosting)
                .enqueue(object : Callback<BaedalPostingResponse> {
                    override fun onResponse(call: Call<BaedalPostingResponse>, response: Response<BaedalPostingResponse>) {
                        if (response.code() == 200) {
                            if (orders.isEmpty())
                                setFrag(FragmentBaedalPost(), mapOf("postId" to response.body()!!.postId), 1)
                            else baedalOrdering(response.body()!!.postId)
                        }
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

    fun baedalOrdering(postId: String){
        val ordering = getOrdering()

        val loopingDialog = looping()
        api.baedalOrdering(postId, ordering).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {
                    setFrag(FragmentBaedalPost(), mapOf("postId" to postId), 1)
                } else {
                    Log.e("baedalAdd Fragment - baedalOrdering", response.toString())
                    makeToast("주문을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.e("baedalAdd Fragment - baedalOrdering", t.message.toString())
                makeToast("주문을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun getOrdering(): Ordering {
        return Ordering(getOrderingOrders())
    }

    private fun getOrderingOrders(): List<OrderingOrder> {
        val orderingOrders = mutableListOf<OrderingOrder>()
        for (order in orders) {
            orderingOrders.add(OrderingOrder(order.quantity, getOrderingMenu(order)))
        }
        return orderingOrders
    }

    private fun getOrderingMenu(order: Order): OrderingMenu {
        return if (order.menu.groups == null)
            OrderingMenu(order.menu.name, null)
        else
            OrderingMenu(order.menu.name, getOrderingGroups(order.menu.groups))
    }

    private fun getOrderingGroups(groups: List<OrderGroup>): List<OrderingGroup> {
        val orderingGroups = mutableListOf<OrderingGroup>()
        for (group in groups) {
            val options = mutableListOf<String>()
            group.options.forEach { options.add(it._id) }
            orderingGroups.add(OrderingGroup(group._id, options))
        }
        return orderingGroups
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