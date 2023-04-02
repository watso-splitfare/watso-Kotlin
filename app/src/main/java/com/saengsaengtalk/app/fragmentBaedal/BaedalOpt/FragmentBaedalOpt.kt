package com.saengsaengtalk.app.fragmentBaedal.BaedalOpt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragBaedalOptBinding
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat

class FragmentBaedalOpt :Fragment() {
    var isUpdating = false
    var postId = ""                 // 수정시에만 사용
    lateinit var orderInfo: Order   // 수정시에만 사용
    var orderId = ""            // 수정시에만 사용
    val orderGroups = mutableMapOf<String, MutableList<String>>()

    var menuName = ""
    var menuPrice = 0
    var storeId = ""

    val groupNames = mutableMapOf<String, String>()
    val optionNames = mutableMapOf<String, String>()
    val groupOptionPrice = mutableMapOf<String, MutableMap<String, Int>>()
    val groupOptionChecked = mutableMapOf<String, MutableMap<String, Boolean>>()
    var quantity = 1

    var groups = listOf<Group>()                   // 옵션 전체. 현재화면 구성에 사용
    private var mBinding: FragBaedalOptBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val gson = Gson()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            try {
                isUpdating = it.getString("isUpdating").toBoolean()
                if (isUpdating) {
                    Log.d("FragBaedalOpt-order", it.getString("order")!!)
                    postId = it.getString("postId")!!
                    orderInfo = gson.fromJson(it.getString("order"), Order::class.java)
                    orderId = orderInfo._id!!
                    orderInfo.menu.groups!!.forEach {
                        val group = it
                        orderGroups[group._id] = mutableListOf<String>()
                        group.options.forEach { orderGroups[group._id]!!.add(it._id) }
                    }
                    quantity = orderInfo.quantity
                }
            }
            catch (e:Exception) {}
            finally {
                menuName = it.getString("menuName")!!
                menuPrice = it.getString("menuPrice")!!.toInt()
                storeId = it.getString("storeId")!!
            }
        }
        Log.d("FragBaedalOpt-Oncreate",
            isUpdating.toString() + ", " +
                    orderId + ", " +
                    menuName + ", " +
                    menuPrice.toString() + ", " +
                    storeId
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOptBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.tvMenuName.text = menuName
        binding.tvQuantity.text = quantity.toString()
        binding.tvBtnCartConfirm.text = if (isUpdating) "옵션 변경"
        else "메뉴 담기"

        setRecyclerView()
        setOrderPrice()

        binding.btnSub.setOnClickListener {
            if (quantity > 1) {
                binding.tvQuantity.text = (--quantity).toString()
                setOrderPrice()
            }
        }
        binding.btnAdd.setOnClickListener {
            if (quantity < 10) {
                binding.tvQuantity.text = (++quantity).toString()
                setOrderPrice()
            }
        }

        /** 메뉴 담기 버튼*/
        binding.btnCartConfirm.setOnClickListener { confirmBtnListener() }
    }

    fun setRecyclerView() {
        val loopingDialog = looping()
        api.getMenuInfo(storeId, menuName).enqueue(object : Callback<MenuInfo> {
            override fun onResponse(call: Call<MenuInfo>, response: Response<MenuInfo>) {
                if (response.code() == 200) {
                    groups = response.body()!!.groups
                    mappingAdapter()
                    setGroupOptionData()
                    setOrderPrice()
                } else {
                    Log.e("baedalOpt Fragment - getGroupOption", response.toString())
                    makeToast("옵션정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<MenuInfo>, t: Throwable) {
                Log.e("baedalOpt Fragment - getGroupOption", t.message.toString())
                makeToast("옵션정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun mappingAdapter() {
        binding.rvOptionGroup.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvOptionGroup.setHasFixedSize(true)

        val adapter = if (isUpdating) {
            BaedalOptGroupAdapter(requireContext(), groups, orderInfo.menu.groups!!)
        } else BaedalOptGroupAdapter(requireContext(), groups, null)

        adapter.addListener(object: BaedalOptGroupAdapter.OnItemClickListener {
            override fun onClick(groupId: String, isRadio: Boolean, optionId: String, isChecked: Boolean) {
                setChecked(groupId, isRadio, optionId, isChecked)
                setOrderPrice()
            }
        })
        binding.rvOptionGroup.adapter = adapter
    }

    fun confirmBtnListener() {
        if (isUpdating) updateOrder()

        else {
            val order = JSONObject()
            val menu = JSONObject()
            order.put("quantity", quantity)
            order.put("price", setOrderPrice())
            menu.put("name", menuName)
            menu.put("price", menuPrice)

            val groups = JSONArray()
            groupOptionChecked.forEach {
                val group = JSONObject()
                val groupId = it.key
                group.put("_id", groupId)
                group.put("name", groupNames[groupId])
                val options = JSONArray()
                it.value.forEach {
                    if (it.value) {
                        val option = JSONObject()
                        val optionId = it.key
                        option.put("_id", optionId)
                        option.put("name", optionNames[optionId])
                        option.put("price", groupOptionPrice[groupId]!![optionId])
                        options.put(option)
                    }
                }
                if (options.length() > 0) {
                    group.put("options", options)
                    groups.put(group)
                }
            }
            if (groups.length() > 0) {
                menu.put("groups", groups)
            }
            order.put("menu", menu)
            val bundle = bundleOf("orderString" to order.toString())
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("order", bundle)
            onBackPressed()
        }
    }

    fun setGroupOptionData() {
        groups.forEach {
            val groupId = it._id
            val groupName = it.name
            var radioFirst = true
            val minQ = it.minOrderQuantity
            val maxQ = it.maxOrderQuantity

            groupNames[groupId] = groupName
            groupOptionChecked[groupId] = mutableMapOf<String, Boolean>()
            groupOptionPrice[groupId] = mutableMapOf<String, Int>()

            /**
             *
             * 주문 수정일 때 과정 추가하기
             *
             */


            it.options.forEach{
                val optionId = it._id

                if (orderGroups.keys.contains(groupId)) {
                    if (orderGroups[groupId]!!.contains(optionId)) {
                        groupOptionChecked[groupId]!![optionId] = true
                    } else {
                        groupOptionChecked[groupId]!![optionId] = false
                    }
                }

                else {
                    if (radioFirst && (minQ == 1 && maxQ == 1)) {
                        groupOptionChecked[groupId]!![optionId] = true
                        radioFirst = false
                    } else {
                        groupOptionChecked[groupId]!![optionId] = false
                    }
                }
                optionNames[optionId] = it.name
                groupOptionPrice[groupId]!![optionId] = it.price
            }
        }
    }

    fun setChecked(groupId: String, isRadio:Boolean, optionId: String, isChecked: Boolean){
        if (isRadio) {
            for (i in groupOptionChecked[groupId]!!.keys) {
                groupOptionChecked[groupId]!![i] = (i == optionId)
            }
        } else {
            groupOptionChecked[groupId]!![optionId] = isChecked
            var quantity = 0
            groupOptionChecked[groupId]!!.forEach{
                if (it.value) quantity += 1
            }
        }
    }

    fun setOrderPrice(): Int {
        var totalPrice = 0
        groupOptionChecked.forEach{
            val groupId = it.key
            it.value.forEach{
                val optionId = it.key
                if (it.value) {
                    totalPrice += groupOptionPrice[groupId]!![optionId]!!
                }
            }
        }
        val orderPrice = menuPrice + totalPrice
        val orderPriceStr = "${dec.format(orderPrice * quantity)}원"
        binding.tvOrderPrice.text = orderPriceStr
        return orderPrice
    }

    fun updateOrder() {
        val updateOrder = UpdateOrder(getOrderingOrder())
        Log.d("FragBaedalOpt-updateOrder-orderingOrder", updateOrder.toString())

        val loopingDialog = looping()
        api.updateBaedalOrder(postId, orderId, updateOrder).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {

                    val bundle = bundleOf("success" to true)
                    getActivity()?.getSupportFragmentManager()?.setFragmentResult("updatePost", bundle)
                    onBackPressed()
                    //setFrag(FragmentBaedalPost(), mapOf("postId" to postId), 1)
                } else {
                    Log.e("FragBaedalOpt-updateOrder", response.toString())
                    makeToast("주문을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.e("FragBaedalOpt-updateOrder", t.message.toString())
                makeToast("주문을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun getOrderingOrder():OrderingOrder {
        val orderingGroups = mutableListOf<OrderingGroup>()
        groupOptionChecked.forEach {
            val groupId = it.key
            val orderingOptions = mutableListOf<String>()
            it.value.forEach {
                if (it.value) orderingOptions.add(it.key)
            }
            if (orderingOptions.isNotEmpty()) {
                orderingGroups.add(OrderingGroup(groupId, orderingOptions))
            }
        }

        val menu = OrderingMenu(menuName, orderingGroups)
        return OrderingOrder(quantity, menu)
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}