package com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm

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
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.LoopingDialog
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragBaedalConfirmBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalOrder
import com.example.saengsaengtalk.fragmentBaedal.Group
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
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
    var orders = mutableListOf<BaedalOrder>()

    var orderPrice = 0

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val gson = Gson()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isPosting = it.getString("isPosting").toBoolean()
            if (!isPosting) {
                postId = it.getString("postId")!!
                currentMember = it.getString("currentMember")!!.toInt()
                isUpdating = it.getString("isUpdating").toBoolean()
                storeId = it.getString("storeId")!!
                //if (isUpdating) currentMember -= 1
            }
            storeName = it.getString("storeName")!!
            baedalFee = it.getString("baedalFee")!!.toInt()
            orders = gson.fromJson(it.getString("orders"), object: TypeToken<MutableList<BaedalOrder>>() {}.type)
                //JSONArray(it.getString("orders"))
            //orderList = JSONArray(it.getString("orderList"))
            //optInfo = JSONArray(it.getString("info"))
        }
        println("배달 컨펌 스토어이름: ${storeName}")
        println("배달 컨펌 메뉴: ${orders}")
        println("배달 컨펌 currentMenber: ${currentMember}")
        Log.d("배달 컨펌", "스토어이름: ${storeName}")
        Log.d("배달 컨펌", "메뉴: ${orders}")
        Log.d("배달 컨펌", "currentMenber: ${currentMember}")

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)

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

        val adapter = SelectedMenuAdapter(requireContext(), JSONArray(gson.toJson(orders)))
        binding.rvOrderList.adapter = adapter

        adapter.setItemClickListener(object: SelectedMenuAdapter.OnItemClickListener {
            override fun onChange(position: Int, change: String) {
                val order = orders[position]
                if (change == "remove") {
                    order.count = 0
                }
                else if (change == "sub") {
                    order.count -= 1
                }
                else {
                    order.count += 1
                }

                bindSetText()

                var confirmAble = false
                orders.forEach {
                    if (it.count > 0) confirmAble = true
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
                else baedalOrdering()                   /** 주문작성 */            }
        }
    }

    /** 메뉴 추가 Frag로 가기전 삭제된 데이터 교정
     *  리사이클러뷰 어댑터 문제로 삭제된 주문은 개수 0으로 저장하고 넘겨주기 직전에 0개인 주문 제거 */
    fun rectifyOrders() {
        var removedIndex = mutableListOf<Int>()
        for (i in 0 until orders.size) {
            if (orders[i].count == 0) removedIndex.add(i)
        }
        removedIndex.reversed().forEach() {
            orders.removeAt(it)
        }
    }

    fun baedalOrderUpdating(){
        val orderingModel = getOrderingModel()
        val loopingDialog = looping()
        api.baedalOrderUpdate(orderingModel).enqueue(object : Callback<OrderingResponse> {
            override fun onResponse(call: Call<OrderingResponse>, response: Response<OrderingResponse>) {
                if (response.code() == 200 && response.body()!!.success) {
                    goToPosting(true)
                } else {
                    Log.e("baedal Confirm Fragment - baedalOrderUpdate", response.toString())
                    makeToast("주문을 수정하지 못했습니다. \n다시 시도해주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<OrderingResponse>, t: Throwable) {
                Log.e("baedal Confirm Fragment - baedalOrderUpdate", t.message.toString())
                makeToast("주문을 수정하지 못했습니다. \n다시 시도해주세요.")
                looping(false, loopingDialog)
                goToPosting(false)
            }
        })
    }

    fun baedalOrdering(){
        val orderingModel = getOrderingModel()
        val loopingDialog = looping()
        api.baedalOrdering(orderingModel).enqueue(object : Callback<OrderingResponse> {
            override fun onResponse(call: Call<OrderingResponse>, response: Response<OrderingResponse>) {
                if (response.code() == 200) orderingComplete()
                else {
                    Log.e("baedal Confirm Fragment - baedalOrdering", response.toString())
                    makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<OrderingResponse>, t: Throwable) {
                Log.e("baedal Confirm Fragment - baedalOrdering", t.message.toString())
                makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun orderingComplete(){
        val loopingDialog = looping()
        api.switchBaedalJoin(mapOf("post_id" to postId)).enqueue(object : Callback<JoinResponse> {
            override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) {
                if (response.code() == 200 && response.body()!!.success) goToPosting(true)
                else {
                    Log.e("baedal Confirm Fragment - switchBaedalJoin", response.toString())
                    makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<JoinResponse>, t: Throwable) {
                Log.e("baedal Confirm Fragment - switchBaedalJoin", t.message.toString())
                makeToast("주문을 작성하지 못했습니다. \n다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun goToPosting(success: Boolean){
        getActivity()?.getSupportFragmentManager()?.setFragmentResult("ordering", bundleOf("success" to success, "postId" to postId))
        onBackPressed()
        onBackPressed()
    }

    fun getOrderingModel(): OrderingModel {
        val orderings = mutableListOf<OrderingOrder>()
        for (order in orders) {
            orderings.add(getOrdering(order))
        }

        return OrderingModel(storeId, postId, orderings)
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

    fun bindSetText() {
        var temp = 0
        orders.forEach {
            temp += it.sumPrice!! * it.count
        }
        orderPrice = temp
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