package com.watso.app.fragmentBaedal.BaedalOrders

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragBaedalOrdersBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat

class FragmentBaedalOrders :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    lateinit var userOrders: MutableList<UserOrder>
    lateinit var adapter: BaedalUserOrderAdapter

    lateinit var post: BaedalPost

    var mBinding: FragBaedalOrdersBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragBaedalOrders"
    val api= API.create()
    val dec = DecimalFormat("#,###")

    var userId = (-1).toLong()
    var isMyorder = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            post = Gson().fromJson(it.getString("postJson")!!, BaedalPost::class.java)
            isMyorder = it.getString("isMyOrder")!!.toBoolean()
        }

        userOrders = mutableListOf<UserOrder>()
        adapter = BaedalUserOrderAdapter(fragmentContext, userOrders, isMyorder)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOrdersBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

        userId = AC.getString("userId", "-1").toLong()

        binding.tvOrder.text = if (isMyorder) "내가 고른 메뉴" else "주문할 메뉴"
        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }
        binding.tvStoreName.text = post.store.name
        mappingAdapter()
        getOrders()

    }

    fun getOrders() {
        AC.showProgressBar()
        if (isMyorder) {
            api.getMyOrders(post._id).enqueue(object: Callback<MyOrderInfo> {
                override fun onResponse(call: Call<MyOrderInfo>, response: Response<MyOrderInfo>) {
                    AC.hideProgressBar()
                    if (response.code() == 200) {
                        val userOrder = mutableListOf(response.body()!!.userOrder)
                        setUserOrder(userOrder)
                    }

                    else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            AC.makeToast(errorResponse.msg)
                            Log.d("$TAG[getMyOrders]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) { Log.e("$TAG[getMyOrders]", e.toString())}
                        AC.onBackPressed()
                    }
                }

                override fun onFailure(call: Call<MyOrderInfo>, t: Throwable) {
                    AC.hideProgressBar()
                    Log.e("FragBaedalOrders getMyOrders", t.message.toString())
                    AC.makeToast("주문정보를 불러오지 못했습니다.")
                    AC.onBackPressed()
                }
            })
        } else {
            api.getAllOrders(post._id).enqueue(object: Callback<AllOrderInfo> {
                override fun onResponse(call: Call<AllOrderInfo>, response: Response<AllOrderInfo>) {
                    AC.hideProgressBar()
                    if (response.code() == 200) setUserOrder(response.body()!!.userOrders)
                    else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            AC.makeToast(errorResponse.msg)
                            Log.d("$TAG[getAllOrders]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) { Log.e("$TAG[getAllOrders]", e.toString())}
                        AC.onBackPressed()
                    }
                }

                override fun onFailure(call: Call<AllOrderInfo>, t: Throwable) {
                    AC.hideProgressBar()
                    Log.e("FragBaedalOrders getMyOrders", t.message.toString())
                    AC.makeToast("주문정보를 불러오지 못했습니다.")
                    AC.onBackPressed()
                }
            })
        }
    }

    fun mappingAdapter() {
        binding.rvOrders.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvOrders.setHasFixedSize(true)
        binding.rvOrders.adapter = adapter
    }

    fun setUserOrder(userOrderData: List<UserOrder>) {
        Log.d("FragBaedalOrders userOrderData", userOrderData.toString())
        Log.d("FragBaedalOrders userOrders", userOrders.toString())
        userOrders.clear()
        Log.d("FragBaedalOrders userOrders clear", userOrders.toString())
        userOrders.addAll(userOrderData)
        Log.d("FragBaedalOrders userOrders addAll", userOrders.toString())
        userOrders.forEach {
            it.isMyOrder = it.userId == userId
            it.orders.forEach { it.setPrice() }
        }
        binding.rvOrders.adapter!!.notifyDataSetChanged()

        bindPrice()
    }

    fun bindPrice() {
        val dec = DecimalFormat("#,###")

        if (isMyorder) {
            if (post.status == "delivered") {
                binding.lbFee.text = "1인당 배달비"
                binding.lbTotalPrice.text = "본인 부담 금액"
            }

            val price = userOrders[0].getTotalPrice()
            val personalFee = post.fee / post.users.size
            binding.tvOrderPrice.text = "${dec.format(price)}원"
            binding.tvFee.text = "${dec.format(personalFee)}원"
            binding.tvTotalPrice.text = "${dec.format(price + personalFee)}원"
        } else {
            var price = 0
            userOrders.forEach { price += it.getTotalPrice() }

            binding.tvOrderPrice.text = "${dec.format(price)}원"
            binding.tvFee.text = "${dec.format(post.fee)}원"
            binding.tvTotalPrice.text = "${dec.format(price + post.fee)}원"

            if (post.status == "delivered") {
                binding.lbFee.text = "배달비"
                binding.lbTotalPrice.text = "총 결제 금액"
            } else {
                binding.lbFee.text = "예상 배달비"
                binding.lbTotalPrice.text = "예상 총 결제 금액"
            }
        }
    }
}