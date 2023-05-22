package com.watso.app.fragmentBaedal.BaedalConfirm

import android.content.Context
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
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragBaedalConfirmBinding
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.fragmentBaedal.Baedal.FragmentBaedal
import com.watso.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat

class FragmentBaedalConfirm :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    lateinit var userOrder: UserOrder
    lateinit var storeInfo: StoreInfo
    lateinit var baedalPosting: BaedalPosting

    var mBinding: FragBaedalConfirmBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragBaedalConfirm"
    val api= API.create()
    val gson = Gson()
    val dec = DecimalFormat("#,###")

    var postId = ""
    var orderPrice = 0
    var fee = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            val orderString = it.getString("order")
            val userOrderString = AC.getString("userOrder", "")

            userOrder = if (userOrderString != "") gson.fromJson(userOrderString, UserOrder::class.java)
            else {
                val userId = AC.getString("userId", "").toLong()
                val nickname = AC.getString("nickname", "")
                UserOrder(userId, nickname, "", mutableListOf<Order>(), null)
            }

            if (orderString != "") userOrder.orders.add(gson.fromJson(orderString, Order::class.java))
            storeInfo = gson.fromJson(it.getString("storeInfo"), StoreInfo::class.java)
        }
        AC.setString("userOrder", gson.toJson(userOrder))

        var postString = AC.getString("baedalPosting", "")
        if (postString != "") {
            baedalPosting = gson.fromJson(postString, BaedalPosting::class.java)
            fee = storeInfo.fee / baedalPosting.minMember
        }
        else fee = storeInfo.fee / AC.getString("minMember", "").toInt()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)
        Log.d("FragBaedalConfirm onCreate View orders", userOrder.toString())

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.tvStoreName.text = storeInfo.name
        binding.rvOrderList.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvOrderList.setHasFixedSize(true)

        val adapter = SelectedMenuAdapter(fragmentContext, userOrder.orders)
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
        AC.showProgressBar()
        Log.d("FragBaedalConfirm orders", userOrder.orders.toString())
        if (postId == "-1") {   // 게시글 작성일 때
            baedalPosting.order = userOrder
            api.baedalPosting(baedalPosting).enqueue(object : Callback<BaedalPostingResponse> {
                override fun onResponse(call: Call<BaedalPostingResponse>, response: Response<BaedalPostingResponse>) {
                    AC.hideProgressBar()
                    if (response.code() == 201) AC.setFrag(FragmentBaedal(), popBackStack = 0)
                    else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            AC.makeToast(errorResponse.msg)
                            Log.d("$TAG[baedalPosting]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) {
                            Log.e("$TAG[baedalPosting]", e.toString())
                            Log.d("$TAG[baedalPosting]", response.errorBody()?.string().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<BaedalPostingResponse>, t: Throwable) {
                    AC.hideProgressBar()
                    Log.e("baedal Confirm Fragment - baedalPosting", t.message.toString())
                    AC.makeToast("게시글을 작성하지 못했습니다. \n다시 시도해주세요.")
                }
            })
        } else {            // 게시글에 참가할 때
            userOrder.requestComment = binding.etRequest.text.toString()
            api.postOrders(postId, userOrder).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    AC.hideProgressBar()
                    if (response.code() == 204) goToPosting()
                    else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            AC.makeToast(errorResponse.msg)
                            Log.d("$TAG[postOrders]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) {
                            Log.e("$TAG[postOrders]", e.toString())
                            Log.d("$TAG[postOrders]", response.errorBody()?.string().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    AC.hideProgressBar()
                    Log.e("baedal Confirm Fragment - postOrders", t.message.toString())
                    AC.makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                }
            })
        }
    }

    fun goToPosting() {
        AC.requestNotiPermission()
        AC.removeString("baedalPosting")
        AC.removeString("storeInfo")
        AC.removeString("userOrder")
        AC.removeString("minMember")
        AC.setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
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

    fun onBackPressed() {
        AC.setString("userOrder", gson.toJson(userOrder))
        val bundle = bundleOf("orderCnt" to userOrder.orders.size)
        getActivity()?.getSupportFragmentManager()?.setFragmentResult("addOrder", bundle)

        AC.onBackPressed()
    }
}