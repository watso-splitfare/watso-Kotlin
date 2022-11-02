package com.example.saengsaengtalk.fragmentBaedal.BaedalMenu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.OrderingModel
import com.example.saengsaengtalk.APIS.SectionMenuModel
import com.example.saengsaengtalk.APIS.UserOrder
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import com.example.saengsaengtalk.fragmentBaedal.BaedalOpt.FragmentBaedalOpt
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentBaedalMenu :Fragment() {
    var isPosting = false
    var postId = ""
    var currentMember = "0"
    var isUpdating = false
    var storeName = ""
    var storeId = "0"
    var baedalFee = ""

    var orders = JSONArray()                            // 주문 리스트
    var sectionMenu = listOf<SectionMenuModel>()        // 스토어 메뉴 전체. 현재화면 구성에 사용

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isPosting = it.getString("isPosting")!!.toBoolean()
            if (!isPosting) {
                postId = it.getString("postId")!!
                currentMember = it.getString("currentMember")!!
                isUpdating = it.getString("isUpdating")!!.toBoolean()
            }
            storeName = it.getString("storeName")!!
            storeId = it.getString("storeId")!!
            baedalFee = it.getString("baedalFee")!!
            //orders = JSONArray(it.getString("orders"))
        }
        Log.d("배달 메뉴", "게시물 번호: ${postId}")
        Log.d("배달 메뉴", "주문 인원: ${currentMember}")
        Log.d("배달 메뉴", "포스팅?: ${isPosting}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        getMenuData()
        if (isUpdating) getOrdersObject()

        /** Option frag에서 메뉴 선택 후 담기 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("order", this) { requestKey, bundle ->
            val orderString = bundle.getString("orderString")
            println("메뉴 담기 ${bundle.getString("orderString")}")
            orders.put(JSONObject(orderString))

            setCartBtn()
        }

        /** Confirm frag에서 뒤로가기(메뉴 더담기) 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("changeOrder", this) { requestKey, bundle ->
            val ordersString = bundle.getString("ordersString")
            println("메뉴 더 담기 ${bundle.getString("ordersString")}")
            orders = JSONArray(ordersString)

            setCartBtn()
        }

        binding.lytCart.setOnClickListener {cartOnClick()}
        binding.btnCart.setOnClickListener {cartOnClick()}
        setCartBtn()
    }

    fun getMenuData() {
        api.getSectionMenu(storeId).enqueue(object : Callback<List<SectionMenuModel>> {
            override fun onResponse(call: Call<List<SectionMenuModel>>, response: Response<List<SectionMenuModel>>) {
                sectionMenu = response.body()!!
                mappingAdapter()
                Log.d("log", response.toString())
                Log.d("log", sectionMenu.toString())
            }

            override fun onFailure(call: Call<List<SectionMenuModel>>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun mappingAdapter() {
        binding.tvStoreName.text = storeName

        binding.rvMenuSection.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSection.setHasFixedSize(true)

        val adapter = BaedalMenuSectionAdapter(requireContext(), sectionMenu)
        binding.rvMenuSection.adapter = adapter

        adapter.addListener(object : BaedalMenuSectionAdapter.OnItemClickListener {
            override fun onClick(sectionName: String, menuName: String) {
                //println("클릭: 섹션: ${section_id}, 메뉴: ${menu_id}")
                loop@ for (s in sectionMenu) {
                    if (sectionName == s.section_name) {
                        for (m in s.menus) {
                            //println("검색: 섹션: ${s.section_id}, 메뉴: ${m.menu_id}")
                            if (menuName == m.menu_name) {
                                //println(m.toString())
                                setFrag(FragmentBaedalOpt(), mapOf(
                                    //"menuId" to menuId.toString(),
                                    "menuName" to m.menu_name,
                                    "menuPrice" to m.menu_price.toString(),
                                    "storeId" to storeId
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
        binding.tvCartCount.text = orders.length().toString()

        if (orders.length() > 0) {
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
            "currentMember" to currentMember,
            "isUpdating" to isUpdating.toString(),
            "storeId" to storeId,
            "storeName" to storeName,
            "baedalFee" to baedalFee,
            "orders" to orders.toString()
        )
        //if (!isPosting) map["postId"] = postId!!

        setFrag(FragmentBaedalConfirm(), map)
    }

    fun getOrdersObject(){
        api.getOrders(postId).enqueue(object : Callback<UserOrder> {
            override fun onResponse(call: Call<UserOrder>, response: Response<UserOrder>) {
                if (response.code() == 200) {
                    val orderingModel = response.body()!!
                    Log.d("log", response.toString())
                    //Log.d("log", sectionMenu.toString())
                    val ordersString = apiModelToObject(orderingModel)
                    println("api모델에서 네이밍 변경: $ordersString")
                    orders = JSONArray(ordersString)
                    setCartBtn()
                } else onBackPressed()
            }

            override fun onFailure(call: Call<UserOrder>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                onBackPressed()
            }
        })
    }

    fun apiModelToObject(userOrders: UserOrder): String{
        val orders = JSONArray()
        for (order in userOrders.orders) {
            val orderObject = JSONObject()
            orderObject.put("count", order.quantity)
            orderObject.put("menuName", order.menu_name)
            orderObject.put("menuPrice", order.menu_price)
            orderObject.put("sumPrice", order.sum_price)

            val groups = JSONArray()
            for (group in order.groups) {
                val groupObject = JSONObject()
                groupObject.put("groupId", group.group_id)
                groupObject.put("groupName", group.group_name)
                val options = JSONArray()
                for (option in group.options) {
                    val optionObject = JSONObject()
                    optionObject.put("optionId", option.option_id)
                    optionObject.put("optionName", option.option_name)
                    optionObject.put("optionPrice", option.option_price)
                    options.put(optionObject)
                    groupObject.put("options",options)
                }
                groups.put(groupObject)
                orderObject.put("groups", groups)
            }
            orders.put(orderObject)
        }
        return orders.toString()
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