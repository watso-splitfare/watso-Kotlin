package com.saengsaengtalk.app.fragmentBaedal.BaedalMenu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragBaedalMenuBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import com.saengsaengtalk.app.fragmentBaedal.BaedalOpt.FragmentBaedalOpt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class FragmentBaedalMenu :Fragment() {
    var isPosting = false
    var postId = ""
    var storeId = "0"
    lateinit var storeInfo: StoreInfo

    //lateinit var postOrder: PostOrder                            // 주문 리스트
    var orderCnt = 0

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val gson = Gson()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isPosting = it.getString("isPosting").toBoolean()
            storeId = it.getString("storeId")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        getMenuData()
        //if (isUpdating) getOrders()

        /** Option frag에서 메뉴 선택 후 담기 시 작동 */ // deprecated
        /*getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("order", this) { requestKey, bundle ->
            val orderString = bundle.getString("orderString")
            orders.put(JSONObject(orderString))
            Log.d("FragBaedalMenu 옵션->메뉴 담기", orders.toString())
            setCartBtn()
        }*/

        /** Confirm frag에서 뒤로가기(메뉴 더담기) 시 작동 */
        /*getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("changeOrder", this) { requestKey, bundle ->
            postOrder = gson.fromJson(bundle.getString("postOrder"), PostOrder::class.java)
            Log.d("FragBaedalMenu confirm->뒤로가기", postOrder.toString())

            setCartBtn()
        }*/
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("addOrder", this) { requestKey, bundle ->
            orderCnt = bundle.getInt("orderCnt")
            Log.d("FragBaedalMenu 메뉴 갯수 :", orderCnt.toString())

            setCartBtn()
        }

        binding.lytCart.setOnClickListener { cartOnClick() }
        binding.btnCart.setOnClickListener { cartOnClick() }
        setCartBtn()
    }

    fun getMenuData() {
        val loopingDialog = looping()
        api.getStoreInfo(storeId).enqueue(object : Callback<StoreInfo> {
            override fun onResponse(call: Call<StoreInfo>, response: Response<StoreInfo>) {
                if (response.code() == 200) {
                    storeInfo = response.body()!!
                    binding.tvBaedalFee.text = "예상 배달비 : %s원".format(dec.format(storeInfo.fee))
                    binding.tvMinOrder.text = "최소 배달 금액 : %s원".format(dec.format(storeInfo.minOrder))
                    mappingAdapter()
                } else {
                    Log.e("baedalMenu Fragment - getSectionMenu", response.toString())
                    makeToast("메뉴정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<StoreInfo>, t: Throwable) {
                Log.e("baedalMenu Fragment - getSectionMenu", t.message.toString())
                makeToast("메뉴정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun mappingAdapter() {

        binding.tvStoreName.text = storeInfo.name

        binding.rvMenuSection.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSection.setHasFixedSize(true)

        val adapter = BaedalMenuSectionAdapter(requireContext(), storeInfo.sections)
        binding.rvMenuSection.adapter = adapter

        /** 이중 어댑터안의 메뉴 이름을 선택할 경우 해당 메뉴의 옵션을 보여주는 프래그먼트로 이동 */
        adapter.addListener(object : BaedalMenuSectionAdapter.OnItemClickListener {
            override fun onClick(sectionName: String, menuId: String) {
                loop@ for (section in storeInfo.sections) {
                    if (sectionName == section.name) {
                        for (menu in section.menus) {
                            if (menuId == menu._id) {
                                setFrag(FragmentBaedalOpt(), mapOf(
                                    /*"menuId" to menu._id,
                                    "menuName" to menu.name,
                                    "menuPrice" to menu.price.toString(),*/
                                    "isPosting" to isPosting.toString(),
                                    "menu" to gson.toJson(menu),
                                    //"storeId" to storeId
                                    "storeInfo" to gson.toJson(storeInfo)
                                ))
                                break@loop
                            }
                        }
                    }
                }
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
            "isPosting" to isPosting.toString(),
            "postId" to postId,
            /*"storeId" to storeId,
            "storeName" to storeInfo.name,
            "fee" to storeInfo.fee.toString()*/
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