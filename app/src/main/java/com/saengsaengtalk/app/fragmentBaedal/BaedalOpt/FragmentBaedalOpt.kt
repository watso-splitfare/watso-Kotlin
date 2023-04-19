package com.saengsaengtalk.app.fragmentBaedal.BaedalOpt

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
import com.saengsaengtalk.app.databinding.FragBaedalOptBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat

class FragmentBaedalOpt :Fragment() {
    var isPosting = false
    var postId = ""
    //lateinit var menu: SectionMenu
    var menuId = ""
    lateinit var storeInfo: StoreInfo

    val groupNames = mutableMapOf<String, String>()
    val optionNames = mutableMapOf<String, String>()
    val quantities = mutableMapOf<String, List<Int>>()
    val optionPrice = mutableMapOf<String, MutableMap<String, Int>>()
    val optionChecked = mutableMapOf<String, MutableMap<String, Boolean>>()
    var quantity = 1
    var price = 0

    var viewClickAble = true

    lateinit var menuInfo: Menu                   // 메뉴 정보. 현재화면 구성에 사용
    private var mBinding: FragBaedalOptBinding? = null
    private val binding get() = mBinding!!
    val gson = Gson()
    val api= APIS.create()
    val dec = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            try {
                isPosting = it.getString("isPosting").toBoolean()
                if (isPosting!!) { postId = it.getString("postId")!! }
            } catch (e:Exception) {}
            finally {

            }
            //menu = gson.fromJson(it.getString("menu"), SectionMenu::class.java)
            menuId = it.getString("menuId")!!
            //storeId = it.getString("storeId")!!
            storeInfo = gson.fromJson(it.getString("storeInfo"), StoreInfo::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOptBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        setRecyclerView()

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
        binding.btnCartConfirm.setOnClickListener {
            if (viewClickAble) {
                viewClickAble = false
                val groups = mutableListOf<Group>()
                Log.d("optionChecked", optionChecked.toString())
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
                Log.d("FragBaedalOpt - order", order.toString())
                setFrag(
                    FragmentBaedalConfirm(), mapOf(
                        "isPosting" to isPosting.toString(),
                        "postId" to postId,
                        "order" to gson.toJson(order),
                        "storeInfo" to gson.toJson(storeInfo)
                    )
                )
            }
        }
    }

    fun setRecyclerView() {
        val loopingDialog = looping()
        api.getMenuInfo(storeInfo._id, menuId).enqueue(object : Callback<Menu> {
            override fun onResponse(call: Call<Menu>, response: Response<Menu>) {
                if (response.code() == 200) {
                    menuInfo = response.body()!!
                    binding.tvMenuName.text = menuInfo.name
                    mappingAdapter()
                    setGroupOptionData()
                    setOrderPrice()
                } else {
                    Log.e("baedalOpt Fragment - getGroupOption", response.toString())
                    makeToast("옵션정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<Menu>, t: Throwable) {
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

        val adapter = BaedalOptGroupAdapter(requireContext(), menuInfo.groups!!)

        adapter.addListener(object: BaedalOptGroupAdapter.OnItemClickListener {
            override fun onClick(groupId: String, isRadio: Boolean, optionId: String, isChecked: Boolean) {
                setChecked(groupId, isRadio, optionId, isChecked)
                setOrderPrice()
            }
        })
        binding.rvOptionGroup.adapter = adapter
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