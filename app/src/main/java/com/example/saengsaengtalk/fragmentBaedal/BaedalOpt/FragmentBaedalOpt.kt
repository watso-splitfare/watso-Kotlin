package com.example.saengsaengtalk.fragmentBaedal.BaedalOpt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.GroupOptionModel
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragBaedalOptBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class FragmentBaedalOpt :Fragment() {
    var menuId = 0
    var menuName = ""
    var menuPrice = 0

    val groupOptionPrice = mutableMapOf<Int, MutableMap<Int, Int>>()
    var groupOptionChecked = mutableMapOf<Int, MutableMap<Int, Boolean>>()
    var count = 1

    var groupOption = listOf<GroupOptionModel>()                   // 옵션 전체. 현재화면 구성에 사용
    private var mBinding: FragBaedalOptBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            menuId = it.getString("menuId")!!.toInt()
            menuName = it.getString("menuName")!!
            menuPrice = it.getString("menuPrice")!!.toInt()
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
        binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"

        binding.btnSub.setOnClickListener {
            if (count > 1) binding.tvCount.text = (--count).toString()
            binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        }
        binding.btnAdd.setOnClickListener {
            if (count < 10) binding.tvCount.text = (++count).toString()
            binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        }



        /*binding.btnCartConfirm.setOnClickListener {
            val optObject = JSONObject()
            optObject.put("menuName", menuName)
            optObject.put("id", id)
            var tempRadio = mutableListOf<String>()
            for ((k, v) in radioChecked) {
                if (v == 1) tempRadio.add(k)
            }
            optObject.put("radio", JSONArray(tempRadio))
            var tempCombo = mutableListOf<String>()
            for ((k, v) in comboChecked) {
                if (v == 1) tempCombo.add(k)
            }
            optObject.put("combo", JSONArray(tempCombo))
            optObject.put("price", setTotalPrice()/count)
            optObject.put("count", count)

            //println("제이슨 출력: ${jsonObject.toString()}")
            val bundle = bundleOf("opt" to optObject.toString())
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("menuWithOpt", bundle)
            onBackPressed()
        }*/
    }

    fun setTotalPrice(): Int {
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

        return menuPrice + (totalPrice * count)
    }


    fun setRecyclerView() {
        api.getGroupOption(menuId).enqueue(object : Callback<List<GroupOptionModel>> {
            override fun onResponse(call: Call<List<GroupOptionModel>>, response: Response<List<GroupOptionModel>>) {
                groupOption = response.body()!!
                mappingAdapter()
                setGroupOptionChecked()
                Log.d("log", response.toString())
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
            override fun onClick(groupId: Int, isRadio: Boolean, optionId: Int, isChecked: Boolean) {
                //println("group: ${groupId}, optionId:${optionId}, isChecked:${isChecked}")
                setChecked(groupId, isRadio, optionId, isChecked)
                //binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
            }
        })

        //adapter.notifyDataSetChanged()
    }

    fun setGroupOptionChecked() {
        groupOption.forEach {
            val groupId = it.group_id
            var radioFirst = true
            val minQ = it.min_orderable_quantity
            val maxQ = it.max_orderable_quantity
            groupOptionChecked[groupId] = mutableMapOf<Int, Boolean>()
            groupOptionPrice[groupId] = mutableMapOf<Int, Int>()
            //println("${it.group_name}")
            it.option_list.forEach{
                //println("${radioFirst}, ${minQ}, ${maxQ}")
                if (radioFirst && (minQ == 1 && maxQ == 1)) {
                    groupOptionChecked[groupId]!![it.option_id] = true
                    radioFirst = false
                } else {
                    groupOptionChecked[groupId]!![it.option_id] = false
                }
                groupOptionPrice[groupId]!![it.option_id] = it.option_price
            }
        }
        println(groupOptionChecked)
    }

    fun setChecked(groupId: Int, isRadio:Boolean, optionId: Int, isChecked: Boolean){
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
            println("[FragOpt] count: ${count}, max: ${groupOption[groupId].max_orderable_quantity}")
        }
        println("[FragOpt] isChecked: ${isChecked}")
        println("groupOptionChecked: ${groupOptionChecked}")
        binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}