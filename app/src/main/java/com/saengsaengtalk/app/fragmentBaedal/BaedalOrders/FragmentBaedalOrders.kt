package com.saengsaengtalk.app.fragmentBaedal.BaedalOrders

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragBaedalOrdersBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalOrders :Fragment() {
    var postId = "-1"
    var postTitle = ""
    var userId = MainActivity.prefs.getString("userId", "-1").toLong()
    var isMyorder = true

    lateinit var userOrders: MutableList<UserOrder>
    lateinit var adapter: BaedalUserOrderAdapter

    val api= APIS.create()
    val dec = DecimalFormat("#,###")
    private var mBinding: FragBaedalOrdersBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            postTitle = it.getString("postTitle")!!
            isMyorder = it.getString("isMyOrder")!!.toBoolean()
        }

        userOrders = mutableListOf<UserOrder>()
        adapter = BaedalUserOrderAdapter(requireContext(), userOrders, isMyorder)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOrdersBinding.inflate(inflater, container, false)
        binding.tvPostTitle.text = postTitle
        binding.tvOrder.text = if (isMyorder) "내가 고른 메뉴" else "주문할 메뉴"
        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        mappingAdapter()
        getOrders()
    }

    fun getOrders() {
        val loopingDialog = looping()
        if (isMyorder) {
            api.getMyOrders(postId).enqueue(object: Callback<OrderInfo> {
                override fun onResponse(call: Call<OrderInfo>, response: Response<OrderInfo>) {
                    if (response.code() == 200) setUserOrder(response.body()!!.userOrders)

                    else {
                        Log.d("FragBaedalOrders getMyOrders", response.toString())
                        makeToast("주문정보를 불러오지 못했습니다.")
                        onBackPressed()
                    }
                    looping(false, loopingDialog)
                }

                override fun onFailure(call: Call<OrderInfo>, t: Throwable) {
                    Log.e("FragBaedalOrders getMyOrders", t.message.toString())
                    makeToast("주문정보를 불러오지 못했습니다.")
                    onBackPressed()
                    looping(false, loopingDialog)
                }
            })
        } else {
            api.getOrders(postId).enqueue(object: Callback<OrderInfo> {
                override fun onResponse(call: Call<OrderInfo>, response: Response<OrderInfo>) {
                    if (response.code() == 200) setUserOrder(response.body()!!.userOrders)
                    else {
                        Log.d("FragBaedalOrders getMyOrders", response.toString())
                        makeToast("주문정보를 불러오지 못했습니다.")
                        onBackPressed()
                    }
                    looping(false, loopingDialog)
                }

                override fun onFailure(call: Call<OrderInfo>, t: Throwable) {
                    Log.e("FragBaedalOrders getMyOrders", t.message.toString())
                    makeToast("주문정보를 불러오지 못했습니다.")
                    onBackPressed()
                    looping(false, loopingDialog)
                }
            })
        }
    }

    fun mappingAdapter() {

        binding.rvOrders.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
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
            it.orders.forEach {
                var price = it.menu.price
                it.menu.groups?.forEach {
                    it.options?.forEach { price += it.price }
                }
                it.price = price
            }
        }
        binding.rvOrders.adapter!!.notifyDataSetChanged()
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