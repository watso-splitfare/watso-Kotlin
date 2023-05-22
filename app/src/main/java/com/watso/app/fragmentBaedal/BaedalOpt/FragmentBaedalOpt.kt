package com.watso.app.fragmentBaedal.BaedalOpt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragBaedalOptBinding
import com.watso.app.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat

class FragmentBaedalOpt :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    lateinit var storeInfo: StoreInfo
    lateinit var menuInfo: Menu                   // 메뉴 정보. 현재화면 구성에 사용
    lateinit var adapter: BaedalOptGroupAdapter

    var mBinding: FragBaedalOptBinding? = null
    val binding get() = mBinding!!
    val TAG="FragBaedalOpt"
    val api= API.create()
    val gson = Gson()
    val dec = DecimalFormat("#,###")

    val groupNames = mutableMapOf<String, String>()
    val optionNames = mutableMapOf<String, String>()
    val quantities = mutableMapOf<String, List<Int>>()
    val optionPrice = mutableMapOf<String, MutableMap<String, Int>>()
    val optionChecked = mutableMapOf<String, MutableMap<String, Boolean>>()

    var postId = ""
    var menuId = ""
    var orderCnt = ""
    var quantity = 1
    var price = 0
    var viewClickAble = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            menuId = it.getString("menuId")!!
            storeInfo = gson.fromJson(it.getString("storeInfo"), StoreInfo::class.java)
            orderCnt = it.getString("orderCnt")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOptBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

        setAdapter()
        refreshView()

        return binding.root
    }

    fun setAdapter() {
        binding.rvOptionGroup.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvOptionGroup.setHasFixedSize(true)

        adapter = BaedalOptGroupAdapter(fragmentContext)

        adapter.setGroupOptClickListener(object: BaedalOptGroupAdapter.OnGroupOptClickListener {
            override fun onClick(groupId: String, isRadio: Boolean, optionId: String, isChecked: Boolean) {
                setChecked(groupId, isRadio, optionId, isChecked)
                setOrderPrice()
            }
        })
        binding.rvOptionGroup.adapter = adapter
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        setRecyclerView()

        binding.btnSub.setOnClickListener {
            if (quantity > 1) {
                binding.tvQuantity.text = "${(--quantity)}개"
                setOrderPrice()
            }
        }
        binding.btnAdd.setOnClickListener {
            if (quantity < 10) {
                binding.tvQuantity.text = "${(++quantity)}개"
                setOrderPrice()
            }
        }

        /** 메뉴 담기 버튼*/
        binding.btnCartConfirm.setOnClickListener {
            if (viewClickAble) {
                viewClickAble = false
                val groups = mutableListOf<Group>()
                optionChecked.forEach {
                    val groupId = it.key
                    val options = mutableListOf<Option>()
                    it.value.forEach {
                        if (it.value) {
                            val optionId = it.key
                            val selectedPrice = optionPrice[groupId]!![optionId]!!
                            val option = Option(optionId, optionNames[optionId]!!, selectedPrice)
                            options.add(option)
                        }
                    }
                    if (options.isNotEmpty()) { groups.add(Group(
                        groupId,
                        groupNames[groupId]!!,
                        quantities[groupId]!![0],
                        quantities[groupId]!![1],
                        options
                    ))}
                }
                val menu = Menu(menuInfo._id, menuInfo.name, menuInfo.price, groups)
                val order = Order(quantity, price, menu)
                AC.setFrag(
                    FragmentBaedalConfirm(), mapOf(
                        "postId" to postId,
                        "order" to gson.toJson(order),
                        "storeInfo" to gson.toJson(storeInfo)
                    )
                )
            }
        }
    }

    fun setRecyclerView() {
        AC.showProgressBar()
        api.getMenuInfo(storeInfo._id, menuId).enqueue(object : Callback<Menu> {
            override fun onResponse(call: Call<Menu>, response: Response<Menu>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    menuInfo = response.body()!!
                    binding.tvMenuName.text = menuInfo.name
                    menuInfo.groups?.let{ adapter.setData(it) }
                    val dec = DecimalFormat("#,###")
                    binding.tvMenuPrice.text = "기본 가격 : ${dec.format(menuInfo.price)}원"
                    setGroupOptionData()
                    setOrderPrice()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        AC.makeToast(errorResponse.msg)
                        Log.d("$TAG[getMenuInfo]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) { Log.e("$TAG[getMenuInfo]", e.toString())}
                }
            }

            override fun onFailure(call: Call<Menu>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedalOpt Fragment - getGroupOption", t.message.toString())
                AC.makeToast("옵션정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
            }
        })
    }

    fun setGroupOptionData() {
        menuInfo.groups!!.forEach {
            val groupId = it._id
            val groupName = it.name
            var radioFirst = true
            val minQ = it.minOrderQuantity
            val maxQ = it.maxOrderQuantity

            groupNames[groupId] = groupName
            quantities[groupId] = listOf(minQ, maxQ)
            optionChecked[groupId] = mutableMapOf<String, Boolean>()
            optionPrice[groupId] = mutableMapOf<String, Int>()

            it.options!!.forEach {
                val optionId = it._id

                if (radioFirst && (minQ == 1 && maxQ == 1)) {
                    optionChecked[groupId]!![optionId] = true
                    radioFirst = false
                }
                else optionChecked[groupId]!![optionId] = false

                optionNames[optionId] = it.name
                optionPrice[groupId]!![optionId] = it.price
            }
        }
    }

    fun setChecked(groupId: String, isRadio:Boolean, optionId: String, isChecked: Boolean){
        if (isRadio) {
            for (i in optionChecked[groupId]!!.keys) {
                optionChecked[groupId]!![i] = (i == optionId)
            }
        } else {
            optionChecked[groupId]!![optionId] = isChecked
            var quantity = 0
            optionChecked[groupId]!!.forEach { if (it.value) quantity += 1 }
        }
    }

    fun setOrderPrice() {
        price = menuInfo.price
        optionChecked.forEach{
            val groupId = it.key
            it.value.forEach{
                val optionId = it.key
                if (it.value) price += optionPrice[groupId]!![optionId]!!
            }
        }
        val orderPriceStr = "${dec.format(price * quantity)}원"
        binding.tvOrderPrice.text = orderPriceStr
    }

    fun onBackPressed() {
        val bundle = bundleOf("orderCnt" to orderCnt.toInt())
        getActivity()?.getSupportFragmentManager()?.setFragmentResult("addOrder", bundle)

        AC.onBackPressed()
    }
}