package com.watso.app.fragmentBaedal.BaedalOrders

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
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.databinding.FragBaedalOrdersBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat

class FragmentBaedalOrders :Fragment() {
    val TAG = "FragBaedalOrders"
    var userId = MainActivity.prefs.getString("userId", "-1").toLong()

    lateinit var post: BaedalPost
    //var postId = "-1"
    var isMyorder = true
//    var storeName = ""
//    var fee = 0
//    var currentMember = 1

    lateinit var userOrders: MutableList<UserOrder>
    lateinit var adapter: BaedalUserOrderAdapter

    val api= API.create()
    val dec = DecimalFormat("#,###")
    private var mBinding: FragBaedalOrdersBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            post = Gson().fromJson(it.getString("postJson")!!, BaedalPost::class.java)
            isMyorder = it.getString("isMyOrder")!!.toBoolean()
        }

        userOrders = mutableListOf<UserOrder>()
        adapter = BaedalUserOrderAdapter(requireContext(), userOrders, isMyorder)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOrdersBinding.inflate(inflater, container, false)
        binding.tvOrder.text = if (isMyorder) "내가 고른 메뉴" else "주문할 메뉴"
        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.tvStoreName.text = post.store.name
        mappingAdapter()
        getOrders()

    }

    fun getOrders() {
        val loopingDialog = looping()
        if (isMyorder) {
            api.getMyOrders(post._id).enqueue(object: Callback<MyOrderInfo> {
                override fun onResponse(call: Call<MyOrderInfo>, response: Response<MyOrderInfo>) {
                    looping(false, loopingDialog)
                    if (response.code() == 200) {
                        val userOrder = mutableListOf(response.body()!!.userOrder)
                        setUserOrder(userOrder)
                    }

                    else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[getMyOrders]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) { Log.e("$TAG[getMyOrders]", e.toString())}
                        onBackPressed()
                    }
                }

                override fun onFailure(call: Call<MyOrderInfo>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragBaedalOrders getMyOrders", t.message.toString())
                    makeToast("주문정보를 불러오지 못했습니다.")
                    onBackPressed()
                }
            })
        } else {
            api.getAllOrders(post._id).enqueue(object: Callback<AllOrderInfo> {
                override fun onResponse(call: Call<AllOrderInfo>, response: Response<AllOrderInfo>) {
                    looping(false, loopingDialog)
                    if (response.code() == 200) setUserOrder(response.body()!!.userOrders)
                    else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[getAllOrders]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) { Log.e("$TAG[getAllOrders]", e.toString())}
                        onBackPressed()
                    }
                }

                override fun onFailure(call: Call<AllOrderInfo>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragBaedalOrders getMyOrders", t.message.toString())
                    makeToast("주문정보를 불러오지 못했습니다.")
                    onBackPressed()
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

        bindPrice()
    }

    fun bindPrice() {
        if (isMyorder) {
            val dec = DecimalFormat("#,###")
            var price = 0
            userOrders[0].orders.forEach {
                price += it.price!! * it.quantity
            }
            binding.tvOrderPrice.text = "${dec.format(price)}원"

            if (post.status == "delivered") {
                val personalFee = post.fee / post.users.size
                binding.lbFee.text = "1인당 배달비"
                binding.lbTotalPrice.text = "본인 부담 금액"
                binding.tvFee.text = "${dec.format(personalFee)}원"
                binding.tvTotalPrice.text = "${dec.format(price + personalFee)}원"
            } else {
                val personalFee = post.fee / post.minMember
                binding.tvFee.text = "${dec.format(personalFee)}원"
                binding.tvTotalPrice.text = "${dec.format(price + personalFee)}원"
            }
        } else {
            binding.divider43.visibility = View.GONE
            binding.lytTable.visibility = View.GONE
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

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
        }
}