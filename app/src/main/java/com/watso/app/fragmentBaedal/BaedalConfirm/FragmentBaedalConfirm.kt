package com.watso.app.fragmentBaedal.BaedalConfirm

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
import com.watso.app.API.*
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragBaedalConfirmBinding
import com.google.gson.Gson
import com.watso.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class FragmentBaedalConfirm :Fragment() {
    var postId = ""
    lateinit var userOrder: UserOrder
    lateinit var storeInfo: StoreInfo
    lateinit var baedalPosting: BaedalPosting
    var orderPrice = 0
    var fee = 0

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()
    val gson = Gson()
    val prefs = MainActivity.prefs
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            val orderString = it.getString("order")
            val userOrderString = prefs.getString("userOrder", "")

            userOrder = if (userOrderString != "") gson.fromJson(userOrderString, UserOrder::class.java)
            else {
                val userId = prefs.getString("userId", "").toLong()
                val nickname = prefs.getString("nickname", "")
                UserOrder(userId, nickname, "", mutableListOf<Order>(), null)
            }

            if (orderString != "") userOrder.orders.add(gson.fromJson(orderString, Order::class.java))

            storeInfo = gson.fromJson(it.getString("storeInfo"), StoreInfo::class.java)
        }
        prefs.setString("userOrder", gson.toJson(userOrder))

        var postString = prefs.getString("baedalPosting", "")
        if (postString != "") {
            baedalPosting = gson.fromJson(postString, BaedalPosting::class.java)
            fee = storeInfo.fee / baedalPosting.minMember
        }
        else fee = storeInfo.fee / prefs.getString("minMember", "").toInt()
        Log.d("FragBaedalConfirm storeInfo", storeInfo.toString())
        Log.d("FragBaedalConfirm minMember", prefs.getString("minMember", ""))
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)
        Log.d("FragBaedalConfirm onCreate View orders", userOrder.toString())

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.tvStoreName.text = storeInfo.name
        binding.rvOrderList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvOrderList.setHasFixedSize(true)

        val adapter = SelectedMenuAdapter(requireContext(), userOrder.orders)
        binding.rvOrderList.adapter = adapter

        adapter.setItemClickListener(object: SelectedMenuAdapter.OnItemClickListener {
            override fun onChange(position: Int, change: String) {
                val order = userOrder.orders[position]

                when (change) {
                    "remove" -> userOrder.orders.removeAt(position)//order.quantity = 0
                    "sub" -> order.quantity -= 1
                    else -> order.quantity += 1
                }

                bindSetText()

                var confirmAble = false
                userOrder.orders.forEach {
                    if (it.quantity > 0) confirmAble = true
                }
                if (!confirmAble) {
                    binding.btnConfirm.setEnabled(false)
                    binding.btnConfirm.setBackgroundResource(R.drawable.solid_gray_10)
                }
            }
        })

        binding.tvBaedalFee.text = "${dec.format(fee)}원"
        bindSetText()

        binding.lytAddMenu.setOnClickListener { onBackPressed() }

        if (postId == "-1") { binding.lytRequest.setVisibility(View.GONE) }

        binding.btnConfirm.setOnClickListener { ordering() }
    }

    fun ordering(){
        val loopingDialog = looping()
        Log.d("FragBaedalConfirm orders", userOrder.orders.toString())
        if (postId == "-1") {
            baedalPosting.order = userOrder
            api.baedalPosting(baedalPosting).enqueue(object : Callback<BaedalPostingResponse> {
                override fun onResponse(call: Call<BaedalPostingResponse>, response: Response<BaedalPostingResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 201) {
                        postId = response.body()!!.postId
                        goToPosting()
                    }
                    else {
                        Log.e("baedal Confirm Fragment - baedalPosting", response.toString())
                        makeToast("게시글을 작성하지 못했습니다. \n다시 시도해주세요.")
                    }
                }

                override fun onFailure(call: Call<BaedalPostingResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("baedal Confirm Fragment - baedalPosting", t.message.toString())
                    makeToast("게시글을 작성하지 못했습니다. \n다시 시도해주세요.")
                }
            })
        } else {
            userOrder.requestComment = binding.etRequest.text.toString()
            api.postOrders(postId, userOrder).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 204) goToPosting()
                    else {
                        Log.e("baedal Confirm Fragment - postOrders", response.toString())
                        makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("baedal Confirm Fragment - postOrders", t.message.toString())
                    makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                }
            })
        }
    }

    fun goToPosting() {
        requestNotiPermission()
        prefs.removeString("baedalPosting")
        prefs.removeString("storeInfo")
        prefs.removeString("userOrder")
        prefs.removeString("minMember")
        setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
    }

    fun bindSetText() {
        orderPrice = 0
        userOrder.orders.forEach {
            orderPrice += it.price!! * it.quantity
        }
        binding.tvOrderPrice.text = "${dec.format(orderPrice)}원"
        binding.tvTotalPrice.text = "${dec.format(orderPrice + fee)}원"
        if (postId != "-1")
            binding.tvConfirm.text = "주문 등록"
    }

    fun requestNotiPermission() {
        val mActivity = activity as MainActivity
        mActivity.requestNotiPermission()
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
        mActivity.setFrag(fragment, arguments, 3)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        prefs.setString("userOrder", gson.toJson(userOrder))

        val bundle = bundleOf("orderCnt" to userOrder.orders.size)
        getActivity()?.getSupportFragmentManager()?.setFragmentResult("addOrder", bundle)

        mActivity.onBackPressed()
    }
}