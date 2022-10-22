package com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragBaedalConfirmBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalOrder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.text.DecimalFormat

class FragmentBaedalConfirm :Fragment() {
    var isPosting = false
    var postNum: Int? = null
    var member = 0
    var storeName = ""
    var baedalFee = 0
    var orders = mutableListOf<BaedalOrder>()

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!
    val gson = Gson()
    var orderPrice = 0
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isPosting = it.getString("isPosting").toBoolean()
            if (!isPosting) {
                postNum = it.getString("postNum")!!.toInt()
                member = it.getString("member")!!.toInt()
            }
            storeName = it.getString("storeName")!!
            baedalFee = it.getString("baedalFee")!!.toInt()
            orders = gson.fromJson(it.getString("orders"), object: TypeToken<MutableList<BaedalOrder>>() {}.type)
                //JSONArray(it.getString("orders"))
            //orderList = JSONArray(it.getString("orderList"))
            //optInfo = JSONArray(it.getString("info"))
        }
        println("스토어이름: ${storeName}")
        println("메뉴: ${orders}")

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

        binding.lytAddMenu.setOnClickListener {
            rectifyOrders()
            val bundle = bundleOf("ordersString" to gson.toJson(orders))
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("changeOrder", bundle)
            onBackPressed()
        }

        bindSetText()

        if (isPosting) {
            binding.lytRequest.setVisibility(View.GONE)
            binding.btnConfirm.setOnClickListener {
                getActivity()?.getSupportFragmentManager()?.setFragmentResult("ConfirmToPosting", bundleOf("ordersString" to gson.toJson(orders)))
                onBackPressed()
                onBackPressed()
            }
        } else {
            binding.btnConfirm.setOnClickListener {
                //setFrag(FragmentBaedalPost(), mapOf("postNum" to postNum!!))
                onBackPressed()
                onBackPressed()
            }
        }
    }

    /** 메뉴 추가 Frag로 가기전 삭제된 데이터 교정*/
    fun rectifyOrders() {
        var removedIndex = mutableListOf<Int>()
        for (i in 0 until orders.size) {
            if (orders[i].count == 0) removedIndex.add(i)
        }
        removedIndex.reversed().forEach() {
            orders.removeAt(it)
        }
    }

    fun bindSetText() {
        var temp = 0
        orders.forEach {
            temp += it.sumPrice!! * it.count
        }
        orderPrice = temp
        binding.tvOrderPrice.text = "${dec.format(orderPrice)}원"
        binding.tvBaedalFee.text = "${dec.format(baedalFee/(member + 1))}원"
        binding.tvTotalPrice.text = "${dec.format(orderPrice + baedalFee/(member + 1))}원"
        binding.btnConfirm.text = "${dec.format(orderPrice + baedalFee/(member + 1))}원 메뉴확정"
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