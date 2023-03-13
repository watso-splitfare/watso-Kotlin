package com.saengsaengtalk.app.fragmentAccount.admin

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragAdminBinding
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentAdmin :Fragment() {

    var stores = listOf<Store>()
    var storeIds = mutableListOf<String>()
    var storeNames = mutableListOf<String>()
    var storeFees = mutableListOf<Int>()
    var selectedIdx = 0
    var baedalfee = 0

    private var mBinding: FragAdminBinding? = null
    private val binding get() = mBinding!!

    val appApi = APIS.create()
    val BaeminApi = BaeminAPIS.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragAdminBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @SuppressLint("ResourceAsColor")
    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        getShopList()

        binding.btnGetDetail.setOnClickListener { getMenuDetail()}

    }

    fun getShopList() {
        appApi.getStoreList().enqueue(object : Callback<List<Store>> {
            override fun onResponse(call: Call<List<Store>>, response: Response<List<Store>>) {
                val detail = JSONObject(response.body()!!.toString()).getJSONObject("data")
                Log.d("log", response.toString())
                Log.d("log", detail.toString())

                stores = response.body()!!

                stores.forEach {
                    storeIds.add(it._id)
                    storeNames.add(it.name)
                    storeFees.add(it.fee)
                }

                val searchmethod =
                    ArrayAdapter(requireContext(), R.layout.simple_spinner_item, storeNames)

                searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spnStores!!.adapter = searchmethod
                binding.spnStores.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        baedalfee = storeFees[position]
                        selectedIdx = position

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) { }
                }
            }

            override fun onFailure(call: Call<List<Store>>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun getMenuDetail() {
        val shopId = 10087212
        val menuId = 12476325

        BaeminApi.getMenuDetail(shopId, menuId).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val detail = JSONObject(response.body()!!.toString()).getJSONObject("data")
                Log.d("log", response.toString())
                Log.d("log", detail.toString())
                //val menuPrice =
                //val priceOptions = menuPrice.getJSONArray("options")

                binding.tvMenuName.text = detail.getString("name")
                //println("priceOptions: ${priceOptions}")
                mappingPriceAdapter(detail.getJSONObject("menuPrice").getJSONArray("options"))
                mappingGroupAdapter(detail.getJSONArray("optionGroups"))
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun mappingPriceAdapter(priceOptions: JSONArray) {
        binding.rvPrice.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvPrice.setHasFixedSize(true)
        val priceAdapter = AdminPriceAdapter(priceOptions)
        binding.rvPrice.adapter = priceAdapter
    }

    fun mappingGroupAdapter(optionGroups: JSONArray) {
        binding.rvOptionGroups.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvOptionGroups.setHasFixedSize(true)
        val optionGroupAdapter = AdminGroupAdapter(requireContext(), optionGroups)
        binding.rvOptionGroups.adapter = optionGroupAdapter
    }

    fun addMenu(menu: MenuAdd){

        appApi.addMenu(menu).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val detail = JSONObject(response.body()!!.toString())
                Log.d("log", response.toString())
                Log.d("log", detail.toString())

                val menuRes = detail.getString("menu_name")
                if (menuRes != "") {
                    println("메뉴추가 성공: $menuRes")
                    println("${menuRes == menu.menu_name}")
                }

            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }


    fun groupPost(groups: GroupsAdd){
        /*appApi.addGroup(group).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val detail = JSONArray(response.body()!!.toString())
                Log.d("log", response.toString())
                Log.d("log", detail.toString())


                /*if (detail != null) {
                    println("그룹추가 성공: ${}")
                    println("${menuRes == menu.menu_name}")
                }*/

            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })*/
    }

    fun optionPost(options: OptionsAdd) {

    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}