package com.example.saengsaengtalk.fragmentBaedal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import com.example.saengsaengtalk.databinding.FragBaedalMenuDetailBinding
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class FragmentBaedalMenuDetail :Fragment() {
    var postNum: String? = null
    var storeId: String? = null

    private var mBinding: FragBaedalMenuDetailBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val menuName = it.getString("menuName")
            println(menuName)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuDetailBinding.inflate(inflater, container, false)

        refreshView()

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        return binding.root
    }

    fun refreshView() {
        /*var sectionMenu = mutableListOf<BaedalMenuSection>()
        var menuMap = mutableMapOf<String, MutableList<BaedalMenu>>()


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

            var temp = BaedalMenu(name, price)
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

        val adapter = BaedalMenuSectionAdapter(requireContext(), sectionMenu)
        binding.rvMenu.adapter = adapter*/
        /*adapter.setItemClickListener(object: BaedalMenuSectionAdapter.OnItemClickListener{
            override fun onClick(v: View, menuName: String) {
                Log.d("제일 바깥", menuName)
                //setFrag(FragmentBaedalPost(), mapOf("postNum" to baedalList[position].postNum.toString()))
            }
        })*/
        /*adapter.addListener(object: BaedalMenuSectionAdapter.OnItemClickListener{
            override fun onClick(menuName: String) {
                //println(menuName)
                setFrag(Fragment)
            }
        })

        adapter.notifyDataSetChanged()*/
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