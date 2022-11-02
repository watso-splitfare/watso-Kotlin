package com.example.saengsaengtalk.fragmentBaedal.BaedalOpt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.GroupOptionModel
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragBaedalOptBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class FragmentBaedalOpt :Fragment() {
    //var menuId = 0
    var menuName = ""
    var menuPrice = 0
    var storeId = ""

    val groupNames = mutableMapOf<String, String>()
    val optionNames = mutableMapOf<String, String>()
    val groupOptionPrice = mutableMapOf<String, MutableMap<String, Int>>()
    val groupOptionChecked = mutableMapOf<String, MutableMap<String, Boolean>>()
    var count = 1

    var groupOption = listOf<GroupOptionModel>()                   // 옵션 전체. 현재화면 구성에 사용
    private var mBinding: FragBaedalOptBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //menuId = it.getString("menuId")!!.toInt()
            menuName = it.getString("menuName")!!
            menuPrice = it.getString("menuPrice")!!.toInt()
            storeId = it.getString("storeId")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOptBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.tvMenuName.text = menuName

        setRecyclerView()
        binding.tvTotalPrice.text = "${dec.format(setSumPrice() * count)}원"

        binding.btnSub.setOnClickListener {
            if (count > 1) {
                binding.tvCount.text = (--count).toString()
                binding.tvTotalPrice.text = "${dec.format(setSumPrice() * count)}원"
            }
        }
        binding.btnAdd.setOnClickListener {
            if (count < 10) {
                binding.tvCount.text = (++count).toString()
                binding.tvTotalPrice.text = "${dec.format(setSumPrice() * count)}원"
            }
        }

        /** 메뉴 담기 버튼*/
        binding.btnCartConfirm.setOnClickListener {
            val order = JSONObject()
            order.put("count", count)
            //order.put("menuId", menuId)
            order.put("menuName", menuName)
            order.put("menuPrice", menuPrice)

            val groups = JSONArray()
            groupOptionChecked.forEach {
                val group = JSONObject()
                val groupId = it.key
                group.put("groupId", groupId)
                group.put("groupName", groupNames[groupId])
                val options = JSONArray()
                it.value.forEach {
                    if (it.value) {
                        val option = JSONObject()
                        val optionId = it.key
                        option.put("optionId", optionId)
                        option.put("optionName", optionNames[optionId])
                        option.put("optionPrice", groupOptionPrice[groupId]!![optionId])
                        options.put(option)
                    }
                }
                if (options.length() > 0) {
                    group.put("options", options)
                    groups.put(group)
                }
            }
            order.put("sumPrice", setSumPrice())
            order.put("groups", groups)
            println(order)

            val bundle = bundleOf("orderString" to order.toString())
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("order", bundle)
            onBackPressed()
        }
    }

    fun setSumPrice(): Int {
        var totalPrice = 0
        groupOptionChecked.forEach{
            val groupId = it.key
            it.value.forEach{
                val optionId = it.key
                if (it.value) {
                    totalPrice += groupOptionPrice[groupId]!![optionId]!!
                }
            }
        }
        return menuPrice + totalPrice
    }


    fun setRecyclerView() {
        api.getGroupOption(menuName, storeId).enqueue(object : Callback<List<GroupOptionModel>> {
            override fun onResponse(call: Call<List<GroupOptionModel>>, response: Response<List<GroupOptionModel>>) {
                groupOption = response.body()!!
                mappingAdapter()
                setGroupOptionData()
                Log.d("log옵션@@@@@@@@@@@@@@@@@@@@", response.toString())
                Log.d("log", groupOption.toString())
            }

            override fun onFailure(call: Call<List<GroupOptionModel>>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun mappingAdapter() {
        binding.rvOptionGroup.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvOptionGroup.setHasFixedSize(true)

        val adapter = BaedalOptAreaAdapter(requireContext(), groupOption)
        binding.rvOptionGroup.adapter = adapter


        adapter.addListener(object: BaedalOptAreaAdapter.OnItemClickListener {
            override fun onClick(groupId: String, isRadio: Boolean, optionId: String, isChecked: Boolean) {
                //println("group: ${groupId}, optionId:${optionId}, isChecked:${isChecked}")
                setChecked(groupId, isRadio, optionId, isChecked)
                //binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
            }
        })

        //adapter.notifyDataSetChanged()
    }

    fun setGroupOptionData() {
        groupOption.forEach {
            val groupId = it.group_id
            val groupName = it.group_name
            var radioFirst = true
            val minQ = it.min_orderable_quantity
            val maxQ = it.max_orderable_quantity

            groupNames[groupId] = groupName
            groupOptionChecked[groupId] = mutableMapOf<String, Boolean>()
            groupOptionPrice[groupId] = mutableMapOf<String, Int>()

            it.options.forEach{
                val optionId = it.option_id

                if (radioFirst && (minQ == 1 && maxQ == 1)) {
                    groupOptionChecked[groupId]!![optionId] = true
                    radioFirst = false
                } else {
                    groupOptionChecked[groupId]!![optionId] = false
                }
                optionNames[optionId] = it.option_name
                groupOptionPrice[groupId]!![optionId] = it.option_price
            }
        }
        println(groupOptionChecked)
    }

    fun setChecked(groupId: String, isRadio:Boolean, optionId: String, isChecked: Boolean){
        if (isRadio) {
            for (i in groupOptionChecked[groupId]!!.keys) {
                groupOptionChecked[groupId]!![i] = (i == optionId)
            }
        } else {
            groupOptionChecked[groupId]!![optionId] = isChecked
            var count = 0
            groupOptionChecked[groupId]!!.forEach{
                if (it.value) count += 1
            }
            //println("[FragOpt] count: ${count}, max: ${groupOption[groupId].max_orderable_quantity}")
        }
        //println("[FragOpt] isChecked: ${isChecked}")
        //println("groupOptionChecked: ${groupOptionChecked}")
        binding.tvTotalPrice.text = "${dec.format(setSumPrice() * count)}원"
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}