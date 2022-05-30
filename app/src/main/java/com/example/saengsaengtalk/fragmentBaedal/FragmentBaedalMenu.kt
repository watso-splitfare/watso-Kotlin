package com.example.saengsaengtalk.fragmentBaedal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.FragmentHomeDirections
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import org.json.JSONObject
import java.text.DecimalFormat

class FragmentBaedalMenu :Fragment() {
    var postNum: Int? = null
    var storeId: String? = null
    var radio: String? = null
    var combo: String? = null
    var menuId: Int? = null

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!

    private val args: FragmentBaedalMenuArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            //postNum = it.getString("postNum")
            //storeId = it.getString("storeId")
            //println(arguments)
        }


        Log.d("배달 메뉴", "게시물 번호: ${args.postNum}")
        Log.d("배달 메뉴", "스토어 id: ${args.storeId}")*/
        postNum = args.postNum
        storeId = args.storeId
        radio = args.radio
        combo = args.combo
        menuId = args.menuId
        println("****************************************")
        println("게시물 번호: ${postNum}")
        println("스토어 id: ${storeId}")
        println("라디오: ${radio}")
        println("콤보: ${combo}")
        println("메뉴 Id: ${menuId}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        return binding.root
    }

    fun refreshView() {
        var sectionMenu = mutableListOf<BaedalMenuSection>()

        val assetManager = resources.assets
        val jsonString = assetManager.open("nene.json").bufferedReader().use { it.readText() }

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
        }

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
                        val jsonObject = menus.getJSONObject(j)
                        if (id == jsonObject.getInt("id")) {
                            /*setFrag(
                                FragmentBaedalDetail(),
                                mapOf("menu" to menus.getJSONObject(j).toString())
                            )*/
                            var action = FragmentBaedalMenuDirections.
                            actionFragmentBaedalMenuToFragmentBaedalDetail(jsonObject.toString())
                            findNavController().navigate(action)
                            break@loop
                        }
                    }
                }
            }
        })

        adapter.notifyDataSetChanged()
    }
/*
    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }
*/
    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}