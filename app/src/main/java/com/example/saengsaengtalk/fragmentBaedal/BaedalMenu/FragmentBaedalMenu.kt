package com.example.saengsaengtalk.fragmentBaedal.BaedalMenu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.SectionMenuModel
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
    var postNum: String? = null
    var member = "0"
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
                postNum = it.getString("postNum")!!
                member = it.getString("member")!!
            }
            storeName = it.getString("storeName")!!
            storeId = it.getString("storeId")!!
            baedalFee = it.getString("baedalFee")!!
            orders = JSONArray(it.getString("orders"))
        }
        Log.d("배달 메뉴", "게시물 번호: ${postNum}")
        Log.d("배달 메뉴", "주문 인원: ${member}")
        Log.d("배달 메뉴", "포스팅?: ${isPosting}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        setRecyclerView()
        setCartBtn()

        /** Option frag에서 메뉴 선택 후 담기 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("order", this) { requestKey, bundle ->
            val orderString = bundle.getString("orderString")
            orders.put(JSONObject(orderString))

            setCartBtn()
        }

        /** Confirm frag에서 뒤로가기(메뉴 더담기) 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("changeOrder", this) { requestKey, bundle ->
            val ordersString = bundle.getString("ordersString")
            orders = JSONArray(ordersString)

            setCartBtn()
        }

        binding.lytCart.setOnClickListener {cartOnClick()}
        binding.btnCart.setOnClickListener {cartOnClick()}
    }

    fun setRecyclerView() {
        api.getSectionMenu(storeId.toInt()).enqueue(object : Callback<List<SectionMenuModel>> {
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
            override fun onClick(sectionId: Int, menuId: Int) {
                //println("클릭: 섹션: ${section_id}, 메뉴: ${menu_id}")
                loop@ for (s in sectionMenu) {
                    if (sectionId == s.section_id) {
                        for (m in s.menu_list) {
                            //println("검색: 섹션: ${s.section_id}, 메뉴: ${m.menu_id}")
                            if (menuId == m.menu_id) {
                                //println(m.toString())
                                setFrag(FragmentBaedalOpt(), mapOf(
                                    "menuId" to menuId.toString(),
                                    "menuName" to m.menu_name,
                                    "menuPrice" to m.menu_price.toString()
                                ))
                                break@loop
                            }
                        }
                    }
                }
            }
        })
        //adapter.notifyDataSetChanged()
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
            "member" to member,
            "storeName" to storeName,
            "baedalFee" to baedalFee,
            "orders" to orders.toString()
        )
        if (!isPosting) map["postNum"] = postNum!!

        setFrag(FragmentBaedalConfirm(), map)
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