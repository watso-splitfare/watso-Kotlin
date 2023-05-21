package com.watso.app.fragmentBaedal.BaedalMenu

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragBaedalMenuBinding
import com.watso.app.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import com.watso.app.fragmentBaedal.BaedalOpt.FragmentBaedalOpt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class FragmentBaedalMenu :Fragment() {
    val TAG = "FragBaedalMenu"
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

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
        AC = ActivityController(activity as MainActivity)

        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("addOrder", this) {
            requestKey, bundle ->
                orderCnt = bundle.getInt("orderCnt")
                setFooter()
            }

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        setAdapter()
        getStoreInfo()

        setFooter()

        return binding.root
    }

    fun setAdapter() {
        adapter = BaedalMenuSectionAdapter(fragmentContext)

        /** 이중 어댑터안의 메뉴 이름을 선택할 경우 해당 메뉴의 옵션을 보여주는 프래그먼트로 이동 */
        adapter.setSecMenuClickListener(object : BaedalMenuSectionAdapter.OnSecMenuClickListener {
            override fun onClick(sectionName: String, menuId: String) {
                Log.d("메뉴 프래그먼트", "리스너")
                if (viewClickAble) {
                    viewClickAble = false
                    setFrag(FragmentBaedalOpt(), mapOf(
                        "postId" to postId,
                        "menuId" to menuId,
                        "storeInfo" to gson.toJson(storeInfo),
                        "orderCnt" to orderCnt.toString()
                    ))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })
        binding.rvMenuSection.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSection.setHasFixedSize(true)
        binding.rvMenuSection.adapter = adapter
    }

    fun getStoreInfo() {
        AC.showProgressBar()
        api.getStoreInfo(storeId).enqueue(object : Callback<StoreInfo> {
            override fun onResponse(call: Call<StoreInfo>, response: Response<StoreInfo>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    storeInfo = response.body()!!
                    binding.tvStoreName.text = storeInfo.name
                    adapter.setData(storeInfo.sections)
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[getStoreInfo]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) { Log.e("$TAG[getStoreInfo]", e.toString())}
                }
            }

            override fun onFailure(call: Call<StoreInfo>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedalMenu Fragment - getSectionMenu", t.message.toString())
                makeToast("메뉴정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
            }
        })
    }

    fun setFooter() {
        binding.btnCart.setOnClickListener { cartOnClick() }
        binding.tvCartCount.text = orderCnt.toString()

        if (orderCnt > 0) binding.lytFooter.visibility = View.VISIBLE
        else binding.lytFooter.visibility = View.GONE
    }

    fun cartOnClick(){
        val map = mutableMapOf(
            "postId" to postId,
            "order" to "",
            "storeInfo" to gson.toJson(storeInfo)
        )

        setFrag(FragmentBaedalConfirm(), map)
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