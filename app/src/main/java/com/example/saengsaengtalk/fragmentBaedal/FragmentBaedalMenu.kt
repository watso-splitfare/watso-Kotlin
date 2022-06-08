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
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import kotlin.reflect.typeOf

class FragmentBaedalMenu :Fragment() {
    var postNum: String? = null
    var member: String? = null
    var isPosting: String? = null

    val dec = DecimalFormat("#,###")

    var storeName = ""
    var baedalFee = ""
    var menuArray = JSONArray()                             // 스토어 메뉴 전체. 현재화면 구성에 사용
    var sectionMenu = mutableListOf<BaedalMenuSection>()    // 어댑터에 넘겨줄 인자
    var orderList = JSONArray()                              // confirm frag 뷰바인딩을 위한 String Array
    //var optInfo = JSONArray()                               // confirm frag 에서 API로 보내기 위한 형식

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
            member = it.getString("member")
            isPosting = it.getString("isPosting")
        }
        Log.d("배달 메뉴", "게시물 번호: ${postNum}")
        Log.d("배달 메뉴", "주문 인원: ${member}")
        Log.d("배달 메뉴", "포스팅?: ${isPosting}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        menuArray = getMenuArray()

        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.tvStoreName.text = storeName

        setSectionMenu()

        binding.rvMenu.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)

        val adapter = BaedalMenuSectionAdapter(requireContext(), sectionMenu)
        binding.rvMenu.adapter = adapter

        adapter.addListener(object: BaedalMenuSectionAdapter.OnItemClickListener{
            override fun onClick(id: Int) {
                println(id)
                loop@ for (i in 0 until menuArray.length()) {
                    val menus = menuArray.getJSONObject(i)
                    if (id == menus.getInt("id")) {
                        setFrag(FragmentBaedalOpt(), mapOf("menu" to menus.toString()))
                        break@loop
                    }
                }
            }
        })
        adapter.notifyDataSetChanged()

        binding.lyCartCount.visibility = View.INVISIBLE
        binding.btnCart.setEnabled(false)

        /* Detail frag에서 메뉴 선택 후 담기 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("menuWithOpt", this) { requestKey, bundle ->
            val opt = bundle.getString("opt")
            println("데이터 받음: ${opt}")
            //optInfo.put(JSONObject(opt))
            setOptArray(opt!!)

            binding.lytCart.setBackgroundResource(R.drawable.btn_baedal_cart)
            binding.lyCartCount.visibility = View.VISIBLE
            binding.tvCartCount.text = orderList.length().toString()
            binding.btnCart.setEnabled(true)
        }
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("fromConfirmFrag", this) { requestKey, bundle ->
            val countChanged = JSONObject(bundle.getString("countChanged"))
            //optInfo = JSONArray(bundle.getString("optInfo"))

            var tempArray = JSONArray()
            var position = mutableListOf<String>()
            for (i in countChanged.keys())
                position.add(i)
            for (i in 0 until orderList.length()) {
                if (position.contains(i.toString())) {
                    if (countChanged[i.toString()] != 0) {
                        val obj = orderList.getJSONObject(i)
                        val menuName = obj.getString("menuName")
                        val price = obj.getInt("price")
                        val count = countChanged[i.toString()]
                        val optString = jArrayToList(obj.getJSONArray("optString"))
                        tempArray.put(
                            JSONObject(mapOf(
                                "menuName" to menuName,"price" to price,"count" to count, "optString" to optString
                            ))
                        )
                    }
                } else {
                    tempArray.put(orderList[i])
                }
            }

            orderList = tempArray
            binding.tvCartCount.text = orderList.length().toString()
            if (orderList.length() == 0) {
                binding.btnCart.setBackgroundResource(R.drawable.btn_baedal_cart_empty)
                binding.lyCartCount.visibility = View.INVISIBLE
                binding.btnCart.setEnabled(false)
            }
        }

        binding.btnCart.setOnClickListener {
            setFrag(FragmentBaedalConfirm(), mapOf(
                "postNum" to postNum!!, "storeName" to storeName, "baedalFee" to baedalFee,
                "member" to member!!, "orderList" to orderList.toString(), "isPosting" to isPosting!!))
        }
    }

    @JvmName("getMenuArray1")
    fun getMenuArray(): JSONArray{
        val assetManager = resources.assets
        val jsonObj = JSONObject(assetManager.open("nene.json").bufferedReader().use { it.readText() })

        storeName = jsonObj.getString("storeName")
        baedalFee = jsonObj.getInt("baedalFee").toString()
        return jsonObj.getJSONArray("info")
    }

    fun setSectionMenu() {
        for (i in 0 until menuArray.length()) {
            val obj = menuArray.getJSONObject(i)
            val id = obj.getInt("id")
            val section = obj.getString("section")
            val menuname = obj.getString("menuName")

            println("id: ${id}, menuname: ${menuname}")

            var minPrice = 2147483647
            var maxPrice = 0
            val radio = obj.getJSONArray("radio")

            for (j in 0 until radio.length()) {
                if (radio.getJSONObject(j).getString("area") == "가격") {
                    val optPrice = radio.getJSONObject(j).getString("price").toInt()
                    if (minPrice > optPrice) minPrice = optPrice
                    if (maxPrice < optPrice) maxPrice = optPrice
                }
            }

            val price =
                if (minPrice == maxPrice) "${dec.format(minPrice)}원"
                else "${dec.format(minPrice)}~${dec.format(maxPrice)}원"

            val baedalmenu = BaedalMenu(id, menuname, price)
            var sectionCheck = false

            for (i in sectionMenu) {
                if (i.section == section) {
                    i.sectionList.add(baedalmenu)
                    sectionCheck = true
                    break
                }
            }
            if (!sectionCheck) sectionMenu.add(BaedalMenuSection(section,mutableListOf(baedalmenu)))
        }
        println(sectionMenu)
    }

    fun setOptArray(opt: String) {
        val jObect = JSONObject(opt)
        val menuName = jObect.getString("menuName")
        val id = jObect.getInt("id")
        val radio = jObect.getJSONArray("radio")
        val combo = jObect.getJSONArray("combo")
        val totalPrice = jObect.getInt("price")
        val count = jObect.getInt("count")

        var optString = mutableListOf<String>()

        for (i in 0 until menuArray.length()){
            if (menuArray.getJSONObject(i).getInt("id") == id){
                val radioArray = menuArray.getJSONObject(i).getJSONArray("radio")
                val comboArray = menuArray.getJSONObject(i).getJSONArray("combo")

                val radioList = jArrayToList(radio)
                while (optString.size < radioList.size) {
                    for (j in 0 until radioArray.length()) {
                        if (radioArray.getJSONObject(j).getString("num") in radioList) {
                            val obj = radioArray.getJSONObject(j)
                            val area = obj.getString("area")
                            val optName = obj.getString("optName")
                            val price = obj.getInt("price")
                            optString.add("• ${area}: ${optName} (${dec.format(price)}원)")
                        }
                    }
                }

                val comboList = jArrayToList(combo)
                var comboIndex = mutableMapOf<String, Int>()
                var comboCount = 0
                while (comboCount < comboList.size){
                    for (j in 0 until comboArray.length()) {
                        if (comboArray.getJSONObject(j).getString("num") in comboList) {
                            val obj = comboArray.getJSONObject(j)
                            val area = obj.getString("area")
                            val optName = obj.getString("optName")
                            val price = obj.getInt("price")
                            if (area in comboIndex.keys) {
                                optString[comboIndex[area]!!] =
                                    "${optString[comboIndex[area]!!]} / ${optName} (${dec.format(price)}원)"
                            }
                            else {
                                optString.add("• ${area}: ${optName} (${dec.format(price)}원)")
                                comboIndex[area] = optString.lastIndex
                            }
                            comboCount++
                        }
                    }
                }
                break
            }
        }
        //for (i in optString) println(i)
        orderList.put(JSONObject(mapOf("menuName" to menuName, "price" to totalPrice,
            "count" to count, "optString" to JSONArray(optString)
        )))
    }

    fun jArrayToList(array: JSONArray): MutableList<String> {
        var list = mutableListOf<String>()
        for (i in 0 until array.length())
            list.add(array.getString(i))

        return list
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