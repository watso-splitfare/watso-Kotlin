package com.watso.app.fragmentBaedal.BaedalMenu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragBaedalMenuBinding
import com.watso.app.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import com.watso.app.fragmentBaedal.BaedalOpt.FragmentBaedalOpt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class FragmentBaedalMenu :Fragment() {
    var postId = ""
    var storeId = "0"
    lateinit var storeInfo: StoreInfo
    lateinit var adapter: BaedalMenuSectionAdapter

    var orderCnt = 0
    var viewClickAble = true

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()
    val gson = Gson()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            storeId = it.getString("storeId")!!
        }

        MainActivity.prefs.removeString("userOrder")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("addOrder", this) {
            requestKey, bundle ->
                viewClickAble = true
                orderCnt = bundle.getInt("orderCnt")
                setCartBtn()
            }

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        setAdapter()
        getStoreInfo()

        binding.lytCart.setOnClickListener { cartOnClick() }
        binding.btnCart.setOnClickListener { cartOnClick() }
        setCartBtn()

        return binding.root
    }

    fun setAdapter() {
        adapter = BaedalMenuSectionAdapter(requireContext())

        /** 이중 어댑터안의 메뉴 이름을 선택할 경우 해당 메뉴의 옵션을 보여주는 프래그먼트로 이동 */
        adapter.setSecMenuClickListener(object : BaedalMenuSectionAdapter.OnSecMenuClickListener {
            override fun onClick(sectionName: String, menuId: String) {
                Log.d("메뉴 프래그먼트", "리스너")
                if (viewClickAble) {
                    viewClickAble = false
                    setFrag(FragmentBaedalOpt(), mapOf(
                        "postId" to postId,
                        "menuId" to menuId,
                        "storeInfo" to gson.toJson(storeInfo)
                    ))
                }
            }
        })
        binding.rvMenuSection.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSection.setHasFixedSize(true)
        binding.rvMenuSection.adapter = adapter
    }

    fun getStoreInfo() {
        val loopingDialog = looping()
        api.getStoreInfo(storeId).enqueue(object : Callback<StoreInfo> {
            override fun onResponse(call: Call<StoreInfo>, response: Response<StoreInfo>) {
                looping(false, loopingDialog)
                if (response.code() == 200) {
                    storeInfo = response.body()!!
                    binding.tvStoreName.text = storeInfo.name
                    binding.tvBaedalFee.text =
                        "예상 배달비 : %s원".format(dec.format(storeInfo.fee))
                    binding.tvMinOrder.text =
                        "최소 배달 금액 : %s원".format(dec.format(storeInfo.minOrder))
                    adapter.setData(storeInfo.sections)
                } else {
                    Log.e("baedalMenu Fragment - getSectionMenu", response.toString())
                    makeToast("메뉴정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                }
            }

            override fun onFailure(call: Call<StoreInfo>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("baedalMenu Fragment - getSectionMenu", t.message.toString())
                makeToast("메뉴정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
            }
        })
    }

    fun setCartBtn() {
        binding.tvCartCount.text = orderCnt.toString()

        if (orderCnt > 0) {
            binding.lytCart.setBackgroundResource(R.drawable.btn_baedal_cart)
            binding.lytCartCount.visibility = View.VISIBLE
            binding.btnCart.setEnabled(true)
            binding.lytCart.setEnabled(true)
        } else {
            binding.lytCart.setBackgroundResource(R.drawable.btn_baedal_cart_empty)
            binding.lytCartCount.visibility = View.INVISIBLE
            binding.btnCart.setEnabled(false)
            binding.lytCart.setEnabled(false)
        }
    }

    fun cartOnClick(){
        val map = mutableMapOf(
            "postId" to postId,
            "order" to "",
            "storeInfo" to gson.toJson(storeInfo)
        )

        setFrag(FragmentBaedalConfirm(), map)
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
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