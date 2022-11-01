package com.example.saengsaengtalk.fragmentAccount.admin

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
import com.example.saengsaengtalk.APIS.BaeminAPIS
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragAdminBinding
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.min

class FragmentAdmin :Fragment() {

    private var mBinding: FragAdminBinding? = null
    private val binding get() = mBinding!!
    val api= BaeminAPIS.create()

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

    data class addStoreModel(
        val store_name: String,
        val fee: Int,
        val min_order: Int
    )

    fun getShopList() {
        api.getShopList().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val detail = JSONObject(response.body()!!.toString()).getJSONObject("data")
                Log.d("log", response.toString())
                Log.d("log", detail.toString())
                val shopObjects = detail.getJSONArray("shops")
                val storeNames = mutableListOf<String>()
                var storeName = ""
                var selectedIdx = 0
                val storeModels = mutableListOf<addStoreModel>()

                for (idx in 0 until shopObjects.length()){
                    val shop = shopObjects.getJSONObject(idx)
                    val _name = shop.getJSONObject("shopInfo").getString("shopName")
                    val min_order = shop.getJSONObject("deliveryInfo").getInt("minimumOrderPrice")

                    storeNames.add(_name)
                    storeModels.add(addStoreModel(_name, 10000, min_order))
                }

                val searchmethod =
                    ArrayAdapter(requireContext(), R.layout.simple_spinner_item, storeNames)

                searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spnStores!!.adapter = searchmethod
                binding.spnStores.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedIdx = position
                        println("가게이름: ${storeNames[selectedIdx]}")
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) { }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun getMenuDetail() {
        val shopId = 10087212
        val menuId = 12476325

        api.getMenuDetail(shopId, menuId).enqueue(object : Callback<JsonObject> {
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

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}