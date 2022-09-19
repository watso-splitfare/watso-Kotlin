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
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

class FragmentBaedalConfirm :Fragment() {
    var postNum: String? = null
    var storeName: String? = null
    var isPosting: String? = null

    var baedalFee = 0
    var member = 1
    var orderList: JSONArray? = null        // 프래그먼트간 메뉴 전송용 데이터
    //var optInfo: JSONArray? = null

    var orderPrice = 0
    var countChanged = mutableMapOf<String, Int>()
    val dec = DecimalFormat("#,###")

    var menu = mutableListOf<BaedalConfirmMenu>()       // 어댑터 바인딩용 데이터

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
            storeName = it.getString("storeName")
            baedalFee = it.getString("baedalFee")!!.toInt()
            member = it.getString("member")!!.toInt()
            orderList = JSONArray(it.getString("orderList"))
            //optInfo = JSONArray(it.getString("info"))
            isPosting = it.getString("isPosting")
        }
        println("스토어이름: ${storeName}")
        println("메뉴: ${orderList}")

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        var bundle = bundleOf("countChanged" to JSONObject(countChanged as Map<*, *>).toString())
        binding.btnPrevious.setOnClickListener {
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("fromConfirmFrag", bundle)
            onBackPressed()
        }
        binding.tvStoreName.text = storeName

        binding.rvMenuSelected.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSelected.setHasFixedSize(true)

        for (i in 0 until orderList!!.length()) {
            val obj = orderList!!.getJSONObject(i)
            val menuName = obj.getString("menuName")
            val price = obj.getInt("price")
            val count = obj.getInt("count")

            val strings = mutableListOf<BaedalConfirm>()
            val stringArray = JSONArray(obj.getString("optString"))
            for (j in 0 until stringArray.length()) {
                strings.add(BaedalConfirm(stringArray.getString(j)))
            }

            menu.add(BaedalConfirmMenu(menuName, price, count, strings))
            orderPrice += price * count
        }
        val adapter = BaedalConfirmMenuAdapter(requireContext(), menu)
        binding.rvMenuSelected.adapter = adapter

        adapter.setItemClickListener(object: BaedalConfirmMenuAdapter.OnItemClickListener {
            override fun onChange(position: Int, price:Int, change: String) {
                var correction = 0

                if (change == "remove") {
                    menu[position].count = 0
                    orderPrice -= price
                }
                else if (change == "sub") {
                    menu[position].count -= 1
                    orderPrice -= price
                }
                else {
                    menu[position].count += 1
                    orderPrice += price
                }

                bindSetText(orderPrice)
                countChanged[(position + correction).toString()] = menu[position].count

                bundle = bundleOf("countChanged" to JSONObject(countChanged as Map<*, *>).toString())

                var confirmAble = false
                for (i in menu) {
                    if (i.count > 0) {
                        confirmAble = true
                        break
                    }
                }
                if (!confirmAble) {
                    binding.btnConfirm.setEnabled(false)
                    binding.btnConfirm.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
                }
            }
        })

        binding.lytAddMenu.setOnClickListener {
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("fromConfirmFrag", bundle)
            onBackPressed()
        }

        bindSetText(orderPrice)

        if (isPosting == "true") {
            binding.lytRequest.setVisibility(View.GONE)
            binding.btnConfirm.setOnClickListener {
                getActivity()?.getSupportFragmentManager()?.setFragmentResult("ConfirmToAdd", bundleOf("menu" to menu))
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

    fun bindSetText(orderPrice: Int) {
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