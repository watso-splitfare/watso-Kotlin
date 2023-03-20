package com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragBaedalConfirmBinding
/*import com.saengsaengtalk.app.fragmentBaedal.BaedalOrder
import com.saengsaengtalk.app.fragmentBaedal.Group*/
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class FragmentBaedalConfirm :Fragment() {
    var isPosting = false
    var postId = ""
    var currentMember = 1
    var isUpdating = false
    var storeName = ""
    var storeId = "0"
    var baedalFee = 0
    var orders = mutableListOf<Order>()

    var orderPrice = 0

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val gson = Gson()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var ordersString = ""
        arguments?.let {
            isPosting = it.getString("isPosting").toBoolean()
            if (!isPosting) {
                postId = it.getString("postId")!!
                currentMember = it.getString("currentMember")!!.toInt()
                isUpdating = it.getString("isUpdating").toBoolean()
                storeId = it.getString("storeId")!!
            }
            storeName = it.getString("storeName")!!
            baedalFee = it.getString("baedalFee")!!.toInt()
            ordersString = it.getString("orders")!!
            orders = gson.fromJson(it.getString("orders"), object: TypeToken<MutableList<Order>>() {}.type)
        }

        Log.d("FragBaedalConfirm onCreate orders", ordersString)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)
        Log.d("FragBaedalConfirm onCreate View orders", orders.toString())

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            rectifyOrders()
            val bundle = bundleOf("ordersString" to gson.toJson(orders))
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("changeOrder", bundle)
            onBackPressed()
        }

        binding.tvStoreName.text = storeName
        binding.rvOrderList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvOrderList.setHasFixedSize(true)

        val adapter = SelectedMenuAdapter(requireContext(), orders)
        binding.rvOrderList.adapter = adapter

        adapter.setItemClickListener(object: SelectedMenuAdapter.OnItemClickListener {
            override fun onChange(position: Int, change: String) {
                val order = orders[position]
                if (change == "remove") {
                    order.quantity = 0
                }
                else if (change == "sub") {
                    order.quantity -= 1
                }
                else {
                    order.quantity += 1
                }

                bindSetText()

                var confirmAble = false
                orders.forEach {
                    if (it.quantity > 0) confirmAble = true
                }
                if (!confirmAble) {
                    binding.btnConfirm.setEnabled(false)
                    binding.btnConfirm.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
                }
            }
        })

        bindSetText()

        binding.lytAddMenu.setOnClickListener {
            rectifyOrders()
            val bundle = bundleOf("ordersString" to gson.toJson(orders))
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("changeOrder", bundle)
            onBackPressed()
        }

        if (isPosting) {
            /** 신규 게시글 작성 : 클릭시 게시글 작성화면으로 이동 */
            binding.lytRequest.setVisibility(View.GONE)
            binding.btnConfirm.setOnClickListener {
                rectifyOrders()
                getActivity()?.getSupportFragmentManager()?.setFragmentResult("ConfirmToPosting", bundleOf("ordersString" to gson.toJson(orders)))
                onBackPressed()
                onBackPressed()
            }
        } else {
            /** 기존 게시글에 주문 작성 또는 수정 : 클릭시 게시글 화면으로 이동 */
            binding.btnConfirm.setOnClickListener {
                rectifyOrders()
                if (isUpdating) baedalOrderUpdating()   /** 주문수정 */
                else ordering()                   /** 주문작성 */            }
        }
    }

    /** 메뉴 추가 Frag로 가기전 삭제된 데이터 교정
     *  리사이클러뷰 어댑터 문제로 삭제된 주문은 개수 0으로 저장하고 넘겨주기 직전에 0개인 주문 제거 */
    fun rectifyOrders() {
        var removedIndex = mutableListOf<Int>()
        for (i in 0 until orders.size) {
            if (orders[i].quantity == 0) removedIndex.add(i)
        }
        removedIndex.reversed().forEach() {
            orders.removeAt(it)
        }
    }

    fun baedalOrderUpdating(){
        val ordering = getOrdering()

        val loopingDialog = looping()
        api.baedalOrderUpdate(postId, ordering).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {
                    goToPosting(true)
                } else {
                    Log.e("baedal Confirm Fragment - baedalOrderUpdate", response.toString())
                    makeToast("주문을 수정하지 못했습니다. \n다시 시도해주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.e("baedal Confirm Fragment - baedalOrderUpdate", t.message.toString())
                makeToast("주문을 수정하지 못했습니다. \n다시 시도해주세요.")
                looping(false, loopingDialog)
                goToPosting(false)
            }
        })
    }

    /*fun baedalJoin(){
        val loopingDialog = looping()
        api.baedalJoin(postId).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) ordering()
                else {
                    Log.e("baedal Confirm Fragment - baedalJoin", response.toString())
                    makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.e("baedal Confirm Fragment - baedalJoin", t.message.toString())
                makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }*/

    fun ordering(){
        val ordering = getOrdering()
        val loopingDialog = looping()
        api.baedalOrdering(postId, ordering).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {
                    looping(false, loopingDialog)
                    goToPosting(true)
                }
                else {
                    Log.e("baedal Confirm Fragment - ordering", response.toString())
                    makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                    looping(false, loopingDialog)
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.e("baedal Confirm Fragment - ordering", t.message.toString())
                makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun goToPosting(success: Boolean){
        getActivity()?.getSupportFragmentManager()?.setFragmentResult("ordering", bundleOf("success" to success))
        onBackPressed()
        onBackPressed()
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

    fun bindSetText() {
        orderPrice = 0
        orders.forEach {
            orderPrice += it.price * it.quantity
        }
        binding.tvOrderPrice.text = "${dec.format(orderPrice)}원"
        binding.tvBaedalFee.text = "${dec.format(baedalFee/currentMember)}원"
        binding.tvTotalPrice.text = "${dec.format(orderPrice + baedalFee/currentMember)}원"
        binding.btnConfirm.text = "${dec.format(orderPrice + baedalFee/currentMember)}원 메뉴확정"
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
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