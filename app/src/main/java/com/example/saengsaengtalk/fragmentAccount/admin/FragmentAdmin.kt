package com.example.saengsaengtalk.fragmentAccount.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.btnGetDetail.setOnClickListener { getMenuDetail()}

    }

    fun getShopList() {

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