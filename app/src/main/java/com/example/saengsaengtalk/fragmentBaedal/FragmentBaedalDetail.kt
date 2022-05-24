package com.example.saengsaengtalk.fragmentBaedal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuDetailBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

class FragmentBaedalDetail :Fragment() {
    var menu: JSONObject? = null

    //private var mBinding: FragBaedalMenuDetailBinding? = null
    //private val binding get() = mBinding!!
    val radioPrice = mutableMapOf<Int, Int>()
    val comboPrice = mutableMapOf<Int, Int>()
    val radioChecked = mutableMapOf<Int, Int>()
    val comboChecked = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val jsonString = it.getString("menu")
            menu = JSONObject(jsonString)
            println("디테일 프래그먼트: ${jsonString}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragBaedalMenuDetailBinding.inflate(inflater, container, false)

        refreshView(binding)

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        return binding.root
    }

    fun refreshView(binding: FragBaedalMenuDetailBinding) {
        var areaMenu = mutableListOf<BaedalDetailArea>()

        val id = menu!!.getInt("id")
        val menuName = menu!!.getString("menuName")
        val radios = menu!!.getJSONArray("radio")
        val combos = menu!!.getJSONArray("combo")

        for (i in 0 until radios.length()) {
            val radio = radios.getJSONObject(i)
            var temp = mutableListOf<BaedalDetail>()
            val area = radio.getString("area")
            val opts = radio.getJSONArray("option")

            for (j in 0 until opts.length()){
                val opt = opts.getJSONObject(j)
                val rnum = opt.getInt("rnum")
                val optName = opt.getString("optName")
                val price = opt.getString("price").toInt()
                temp.add(BaedalDetail(rnum, optName, price, area,true))

                radioPrice[rnum] = price
                if (j == 0) radioChecked[rnum] = 1
                else radioChecked[rnum] = 0
            }
            areaMenu.add(BaedalDetailArea(area, temp))
        }

        if (combos[0] != "") {
            for (i in 0 until combos.length()) {
                val combo = combos.getJSONObject(i)

                var temp = mutableListOf<BaedalDetail>()
                val area = combo.getString("area")
                val opts = combo.getJSONArray("option")
                val min = combo.getInt("min")
                val max = combo.getInt("max")

                for (j in 0 until opts.length()) {
                    val opt = opts.getJSONObject(j)
                    val cnum = opt.getInt("cnum")
                    val optName = opt.getString("optName")
                    val price = opt.getString("price").toInt()
                    temp.add(BaedalDetail(cnum, optName, price, area,false, min, max))

                    comboPrice[cnum] = price
                    comboChecked[cnum] = 0
                }
                areaMenu.add(BaedalDetailArea(area, temp))
            }
        }

        binding.tvMenuName.text = menuName
        binding.rvMenu.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)

        val adapter = BaedalDetailAreaAdapter(requireContext(), areaMenu)

        binding.rvMenu.addItemDecoration(BaedalDetailAreaAdapter.BaedalDetailAreaAdapterDecoration())
        adapter.notifyDataSetChanged()

        binding.rvMenu.adapter = adapter
        val dec = DecimalFormat("#,###")
        binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"

        adapter.addListener(object: BaedalDetailAreaAdapter.OnItemClickListener {
            override fun onClick(isRadio: Boolean, area: String, num: Int, isChecked: Boolean) {
                println("isRadio: ${isRadio}, area: ${area}, num:${num}, isChecked:${isChecked}")
                if (isRadio) setChecked(isRadio, area, num, isChecked, radios)
                else setChecked(isRadio, area, num, isChecked)
                binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
            }
        })
    }

    fun setTotalPrice(): Int {
        var totalPrice = 0

        if (radioChecked.isNotEmpty())
            for (i in radioChecked.keys)
                totalPrice += radioChecked[i]!! * radioPrice[i]!!
        if (comboChecked.isNotEmpty())
            for (i in comboChecked.keys)
                totalPrice += comboChecked[i]!! * comboPrice[i]!!

        return totalPrice
    }

    fun setChecked(isRadio: Boolean, area: String, num: Int, isChecked: Boolean, optList: JSONArray= JSONArray()) {
        if (isRadio) {
            if (radioChecked[num] == 0){
                var radios = JSONObject()
                for (i in 0 until optList.length()){
                    if (optList.getJSONObject(i).getString("area") == area) {
                        radios = optList.getJSONObject(i)
                        break
                    }
                }
                var nums = mutableListOf<Int>()
                val array = radios.getJSONArray("option")
                for (i in 0 until array.length()){
                    nums.add(array.getJSONObject(i).getInt("rnum"))
                }
                for (i in nums) {
                    if (i == num) radioChecked[i] = 1
                    else radioChecked[i] = 0
                }
            }
        } else {
            if (isChecked) comboChecked[num] = 1
            else comboChecked[num] = 0
        }
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String?> = mapOf("none" to null)) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}