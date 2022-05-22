package com.example.saengsaengtalk.fragmentBaedal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat

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
        var sectionMenu = mutableListOf<BaedalMenuSection>()
        var menuMap = mutableMapOf<String, MutableList<BaedalMenu>>()


        val assetManager = resources.assets
        val jsonString = assetManager.open("nene.json").bufferedReader().use { it.readText() }

        //val jObject =
        val jArray = JSONObject(jsonString).getJSONArray("nene")

        for (i in 0 until jArray.length()) {
            val obj = jArray.getJSONObject(i)           // 섹션
            val section = obj.getString("section")  // 섹션명
            val secArray = obj.getJSONArray("menu")
            var temp = mutableListOf<BaedalMenu>()
            for (j in 0 until secArray.length()) {
                val id = secArray.getJSONObject(j).getString("id").toInt()
                val menuname = secArray.getJSONObject(j).getString("menuName")
                val radio = secArray.getJSONObject(j).getJSONArray("radio")
                // val combo = secArray.getJSONObject(j).getJSONArray("combo")
                println("id: ${id}, menuname: ${menuname}")

                var minPrice = 2147483647
                var maxPrice = 0
                val priceArray = radio.getJSONObject(0).getJSONArray("option")
                for (k in 0 until priceArray.length()) {
                    val optPrice = priceArray.getJSONObject(k).getString("price").toInt()
                    if (minPrice > optPrice) minPrice = optPrice
                    if (maxPrice < optPrice) maxPrice = optPrice
                }
                val dec = DecimalFormat("#,###")
                val price =
                    if (minPrice == maxPrice) "${dec.format(minPrice)}원"
                    else "${dec.format(minPrice)}~${dec.format(maxPrice)}원"

                temp.add(BaedalMenu(id, menuname, price))
            }
            sectionMenu.add(BaedalMenuSection(section,temp))

            /* 실사용 코드 */

            /*var min_price = 2147483647
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

            var temp = BaedalMenu(id, name, price)
            if (section in menuMap.keys)
                menuMap[section]!!.add(temp)
            else
                menuMap[section] = mutableListOf(temp)*/

        }
        /*for ((key, value) in menuMap) {
            sectionMenu.add(BaedalMenuSection(key, value))
        }*/

        binding.rvMenu.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)

        val adapter = BaedalMenuSectionAdapter(requireContext(), sectionMenu)
        binding.rvMenu.adapter = adapter

        adapter.addListener(object: BaedalMenuSectionAdapter.OnItemClickListener{
            override fun onClick(id: Int) {
                println(id)
                loop@ for (i in 0 until jArray.length()) {
                    val menus = jArray.getJSONObject(i).getJSONArray("menu")
                    for (j in 0 until menus.length()) {
                        if (id == menus.getJSONObject(j).getInt("id")) {
                            setFrag(
                                FragmentBaedalDetail(),
                                mapOf("menu" to menus.getJSONObject(j).toString())
                            )
                            break@loop
                        }
                    }
                }

            }
        })

        adapter.notifyDataSetChanged()
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