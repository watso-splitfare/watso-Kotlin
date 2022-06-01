package com.example.saengsaengtalk.fragmentBaedal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import org.json.JSONObject
import java.text.DecimalFormat

class FragmentBaedalMenu :Fragment() {
    var postNum: String? = null
    var storeId: String? = null
    var menuInCart= mutableListOf<Array<Any>>()

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

        binding.lyCartCount.visibility = View.INVISIBLE
        binding.btnCart.setEnabled(false)


        /* Detail frag에서 메뉴 선택 후 담기 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("menuWithOpt", this) { requestKey, bundle ->
            val id = bundle.getInt("id")
            val radio = bundle.get("radio")
            val combo = bundle.get("combo")
            val count = bundle.get("count")
            menuInCart.add(arrayOf<Any>(id, radio!!, combo!!, count!!))
            for (i in menuInCart) {
                println("${i[0]}, ${i[1]}, ${i[2]}, ${i[3]}")
                binding.btnCart.setBackgroundResource(R.drawable.btn_baedal_cart)
                binding.lyCartCount.visibility = View.VISIBLE
                binding.tvCartCount.text = menuInCart.size.toString()
                binding.btnCart.setEnabled(true)
            }
        }
        binding.btnCart.setOnClickListener {
            setFrag(FragmentBaedalConfirm())
        }
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}