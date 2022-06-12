package com.example.saengsaengtalk.fragmentBaedal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.fragmentBaedal.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalOptBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

class FragmentBaedalOpt :Fragment() {
    var menu: JSONObject? = null
    var section: String? = null

    val radioPrice = mutableMapOf<String, Int>()
    val comboPrice = mutableMapOf<String, Int>()
    val radioChecked = mutableMapOf<String, Int>()
    val comboChecked = mutableMapOf<String, Int>()
    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val jsonString = it.getString("menu")
            menu = JSONObject(jsonString)

            println("디테일 프래그먼트: ${jsonString}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragBaedalOptBinding.inflate(inflater, container, false)

        refreshView(binding)

        return binding.root
    }

    fun refreshView(binding: FragBaedalOptBinding) {
        var areaMenu = mutableListOf<BaedalOptArea>()

        val id = menu!!.getInt("id")
        val menuName = menu!!.getString("menuName")
        val radios = menu!!.getJSONArray("radio")
        val combos = menu!!.getJSONArray("combo")
        val dec = DecimalFormat("#,###")

        for (i in 0 until radios.length()) {
            val radio = radios.getJSONObject(i)
            val num = radio.getString("num")
            val area = radio.getString("area")
            val optName = radio.getString("optName")
            val price = radio.getInt("price")

            radioPrice[num] = price
            val baedalOpt = BaedalOpt(num, optName, price, area, true)
            var areaCheck = false

            for (j in areaMenu){
                if (j.area == area){
                    j.areaList.add(baedalOpt)   // BaedalOptArea 클래스에서 맞는 area 찾아 baedalOpt 클래스 넣기
                    areaCheck = true
                    radioChecked[num] = 0
                    break
                }
            }
            if (!areaCheck) {                   // BaedalOptArea 에 없는 area 일 경우 새로 생성, 첫번째 옵션을 의미하므로 디폴트로 체크
                areaMenu.add(BaedalOptArea(area, mutableListOf(baedalOpt)))
                radioChecked[num] = 1
            }
        }

        if (combos[0] != "") {
            for (i in 0 until combos.length()) {
                val combo = combos.getJSONObject(i)
                val num = combo.getString("num")
                val area = combo.getString("area")
                val optName = combo.getString("optName")
                val price = combo.getInt("price")

                comboPrice[num] = price
                comboChecked[num] = 0

                val baedalOpt = BaedalOpt(num, optName, price, area, false)
                var areaCheck = false
                for (j in areaMenu){
                    if (j.area == area){
                        j.areaList.add(baedalOpt)
                        areaCheck = true
                    }
                }
                if (!areaCheck) areaMenu.add(BaedalOptArea(area, mutableListOf(baedalOpt)))
            }
        }

        binding.tvMenuName.text = menuName
        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)
        binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        val adapter = BaedalOptAreaAdapter(requireContext(), areaMenu)

        //binding.rvMenu.addItemDecoration(BaedalOptAreaAdapter.BaedalOptAreaAdapterDecoration())
        adapter.notifyDataSetChanged()

        binding.rvMenu.adapter = adapter

        binding.btnSub.setOnClickListener {
            if (count > 1) binding.tvCount.text = (--count).toString()
            binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        }
        binding.btnAdd.setOnClickListener {
            if (count < 10) binding.tvCount.text = (++count).toString()
            binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        }

        adapter.addListener(object: BaedalOptAreaAdapter.OnItemClickListener {
            override fun onClick(isRadio: Boolean, area: String, num: String, isChecked: Boolean) {
                println("isRadio: ${isRadio}, area: ${area}, num:${num}, isChecked:${isChecked}")
                if (isRadio) setChecked(isRadio, area, num, isChecked, radios)
                else setChecked(isRadio, area, num, isChecked)
                binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
            }
        })

        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.btnCartConfirm.setOnClickListener {
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
        }
    }

    fun setTotalPrice(): Int {
        var totalPrice = 0

        if (radioChecked.isNotEmpty())
            for (i in radioChecked.keys)
                totalPrice += radioChecked[i]!! * radioPrice[i]!!
        if (comboChecked.isNotEmpty())
            for (i in comboChecked.keys)
                totalPrice += comboChecked[i]!! * comboPrice[i]!!

        return (totalPrice * count)
    }

    fun setChecked(isRadio: Boolean, area: String, num: String, isChecked: Boolean, optList: JSONArray= JSONArray()) {
        if (isRadio) {
            if (radioChecked[num] == 0){
                //var radios = JSONArray()
                var nums = mutableListOf<String>()

                for (i in 0 until optList.length()){
                    if (optList.getJSONObject(i).getString("area") == area) {
                        //radios.put(optList.getJSONObject(i))
                        nums.add(optList.getJSONObject(i).getString("num"))
                    }
                }

                /*val array = radios.getJSONArray("option")
                for (i in 0 until array.length()){
                    nums.add(array.getJSONObject(i).getString("num"))
                }*/
                for (i in nums) {
                    if (i == num) radioChecked[i] = 1
                    else radioChecked[i] = 0
                }
            }
        } else {
            if (isChecked) comboChecked[num] = 1
            else comboChecked[num] = 0
        }
        println("라디오: ${radioChecked}, 콤보: ${comboChecked}")
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}