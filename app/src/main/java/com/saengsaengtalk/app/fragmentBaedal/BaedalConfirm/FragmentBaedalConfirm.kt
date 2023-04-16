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
import com.saengsaengtalk.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat

class FragmentBaedalConfirm :Fragment() {
    var isPosting = false
    var postId = ""
    lateinit var postOrder: PostOrder
    lateinit var storeInfo: StoreInfo
    lateinit var baedalPosting: BaedalPosting
    var orderPrice = 0
    var fee = 0

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val gson = Gson()
    val prefs = MainActivity.prefs
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isPosting = it.getString("isPosting").toBoolean()
            try {
                if (!isPosting) { postId = it.getString("postId")!!}
            } catch (e: Exception) {} finally {}



            val order = gson.fromJson(it.getString("order"), Order::class.java)
            val temp = prefs.getString("postOrder", "")
            Log.d("FragBaedalConfirm - order", order.toString())
            Log.d("FragBaedalConfirm - temp", temp)
            if (temp != "") {
                postOrder = gson.fromJson(temp, PostOrder::class.java)
                postOrder.orders.add(order)
            }
            else postOrder = PostOrder(mutableListOf(order))
            storeInfo = gson.fromJson(it.getString("storeInfo"), StoreInfo::class.java)
        }
        prefs.setString("postOrder", gson.toJson(postOrder.orders))

        var temp = prefs.getString("baedalPosting", "")
        if (temp != "") { baedalPosting = gson.fromJson(temp, BaedalPosting::class.java) }
        fee = storeInfo.fee / baedalPosting.minMember
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)
        Log.d("FragBaedalConfirm onCreate View orders", postOrder.toString())

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            rectifyOrders()
            //val bundle = bundleOf("ordersString" to gson.toJson(orders))
            //getActivity()?.getSupportFragmentManager()?.setFragmentResult("changeOrder", bundle)
            onBackPressed()
            onBackPressed()
        }

        binding.tvStoreName.text = storeInfo.name
        binding.rvOrderList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvOrderList.setHasFixedSize(true)

        val adapter = SelectedMenuAdapter(requireContext(), postOrder.orders)
        binding.rvOrderList.adapter = adapter

        adapter.setItemClickListener(object: SelectedMenuAdapter.OnItemClickListener {
            override fun onChange(position: Int, change: String) {
                val order = postOrder.orders[position]

                when (change) {
                    "remove" -> order.quantity = 0
                    "sub" -> order.quantity -= 1
                    else -> order.quantity += 1
                }

                bindSetText()

                var confirmAble = false
                postOrder.orders.forEach {
                    if (it.quantity > 0) confirmAble = true
                }
                if (!confirmAble) {
                    binding.btnConfirm.setEnabled(false)
                    binding.btnConfirm.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
                }
            }
        })

        binding.tvBaedalFee.text = "${dec.format(fee)}원"
        bindSetText()

        binding.lytAddMenu.setOnClickListener {
            rectifyOrders()
            val bundle = bundleOf("ordersCnt" to postOrder.orders.size)
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("addOrder", bundle)
            onBackPressed()
            onBackPressed()
        }

        if (isPosting) { binding.lytRequest.setVisibility(View.GONE) }

        binding.btnConfirm.setOnClickListener { ordering() }
    }

    /** 메뉴 추가 Frag로 가기전 삭제된 데이터 교정
     *  리사이클러뷰 어댑터 문제로 삭제된 주문은 개수 0으로 저장하고 넘겨주기 직전에 0개인 주문 제거 */
    fun rectifyOrders() {
        var removedIndex = mutableListOf<Int>()
        for (i in 0 until postOrder.orders.size) {
            if (postOrder.orders[i].quantity == 0) removedIndex.add(i)
        }
        removedIndex.reversed().forEach() {
            postOrder.orders.removeAt(it)
        }
        prefs.setString("postOrder", gson.toJson(postOrder))
    }

    fun ordering(){
        rectifyOrders()
        val loopingDialog = looping()
        if (isPosting) {
            baedalPosting.orders = postOrder.orders
            api.baedalPosting(baedalPosting).enqueue(object : Callback<BaedalPostingResponse> {
                override fun onResponse(call: Call<BaedalPostingResponse>, response: Response<BaedalPostingResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 201) {
                        val postId = response.body()!!.postId
                        prefs.removeString("baedalPosting")
                        prefs.removeString("storeInfo")
                        prefs.removeString("postOrder")
                        setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
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
            api.postOrders(postId, postOrder).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 204) goToPosting(true)
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

    fun goToPosting(success: Boolean){
        getActivity()?.getSupportFragmentManager()?.setFragmentResult("ordering", bundleOf("success" to success))
        onBackPressed()
        onBackPressed()
    }

    fun bindSetText() {
        orderPrice = 0
        postOrder.orders.forEach {
            orderPrice += it.price!! * it.quantity
        }
        binding.tvOrderPrice.text = "${dec.format(orderPrice)}원"
        binding.tvTotalPrice.text = "${dec.format(orderPrice + fee)}원"
        binding.btnConfirm.text = "메뉴 확정"
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
        mActivity.setFrag(fragment, arguments, 2)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}