package com.example.saengsaengtalk.fragmentBaedal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class FragmentBaedalMenu :Fragment() {
    var postNum: String? = null
    var storeId: String? = null

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
            storeId = it.getString("storeId")
        }

        Log.d("배달 메뉴", "게시물 번호: ${postNum}")
        Log.d("배달 메뉴", "스토어 id: ${storeId}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        return binding.root
    }

    fun refreshView() {
        //lateinit var sectionMenu: MutableList<BaedalMenuSection>
        //lateinit var menuMap: MutableMap<String, MutableList<BaedalMenu>>
        var sectionMenu = mutableListOf<BaedalMenuSection>()
        var menuMap = mutableMapOf<String, MutableList<BaedalMenu>>()


        //lateinit var tempmap: MutableMap<String, String>
        /*var temp = BaedalMenu("n", "p")
        Log.d("temp.name", temp.name)
        Log.d("temp.price", temp.price)
        menuMap.put("n", mutableListOf(BaedalMenu("a", "b")))
        Log.d("menumap[n][0].name", menuMap["n"]!![0].name)*/
        //menuMap[section]!!.add(temp)

        val assetManager = resources.assets
        val jsonString = assetManager.open("nene.json").bufferedReader().use { it.readText() }

        val jObject = JSONObject(jsonString)
        val jArray = jObject.getJSONArray("nene")

        for (i in 0 until jArray.length()) {
            val obj = jArray.getJSONObject(i)
            val id = obj.getInt("id")
            val section = obj.getString("section")
            val name = obj.getString("name")
            val radios = obj.getJSONArray("radio")
            val combos = obj.getJSONArray("combo")

            /* debug */

            Log.d("id: ${id} ", "${section}\t${name}")

            try {
                for (j in 0 until radios.length()) {
                    val radio = radios.getJSONObject(j)
                    val radio_area = radio.getString("area")
                    val radio_name = radio.getString("name")
                    val radio_price = radio.getString("price")
                    Log.d("id: ${id}  ${radio_area}", "${radio_name}\t${radio_price}")
                }
            } catch (e: JSONException) {
                Log.d("id: ${id} ", "라디오버튼 없음")
            }

            try {
                for (k in 0 until combos.length()) {
                    val combo = combos.getJSONObject(k)
                    val combo_area = combo.getString("area")
                    val combo_name = combo.getString("name")
                    val combo_price = combo.getString("price")
                    Log.d("id: ${id}  ${combo_area}", "${combo_name}\t${combo_price}")
                }
            } catch (e: JSONException) {
                Log.d("id: ${id} ", "콤보박스 없음")
            }

            /* 실사용 코드 */

            var min_price = 2147483647
            var max_price = 0
            for (i in 0 until radios.length()) {
                if (radios.getJSONObject(i).getString("area") == "가격") {
                    val radio_price = radios.getJSONObject(i).getString("price").toInt()
                    if (min_price > radio_price) min_price = radio_price
                    if (max_price < radio_price) max_price = radio_price
                }
            }
            val dec = DecimalFormat("#,###")
            val price =
                if (min_price == max_price) "${dec.format(min_price)}원"
                else "${dec.format(min_price)}~${dec.format(max_price)}원"
            Log.d("section", section)
            Log.d("name", name)
            Log.d("price", price)
            var temp = BaedalMenu(name, price)
            Log.d("temp.name", temp.name)
            Log.d("temp.price", temp.price)

            if (section in menuMap.keys)
                menuMap[section]!!.add(temp)
            else
                menuMap[section] = mutableListOf(temp)

        }
        for ((key, value) in menuMap) {
            sectionMenu.add(BaedalMenuSection(key, value))
        }

        binding.rvMenu.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)
        binding.rvMenu.adapter = BaedalMenuSectionAdapter(requireContext(), sectionMenu)

    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}