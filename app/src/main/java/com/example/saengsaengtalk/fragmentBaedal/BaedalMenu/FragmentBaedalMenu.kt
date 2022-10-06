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
    var postNum: String? = null
    var member: String? = null
    var isPosting = "false"
    var storeName = ""
    var storeId = "0"
    var baedalFee = ""


    var sectionMenu = listOf<SectionMenuModel>()                   // 스토어 메뉴 전체. 현재화면 구성에 사용
    //var sectionMenu = mutableListOf<BaedalMenuSection>()    // 어댑터에 넘겨줄 인자
    var orderList = JSONArray()                              // confirm frag 뷰바인딩을 위한 String Array
    //var optInfo = JSONArray()                               // confirm frag 에서 API로 보내기 위한 형식

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
            member = it.getString("member")
            isPosting = it.getString("isPosting")!!
            storeName = it.getString("storeName")!!
            storeId = it.getString("storeId")!!
            baedalFee = it.getString("baedalFee")!!
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

        binding.lyCartCount.visibility = View.INVISIBLE
        binding.btnCart.setEnabled(false)

        /** Detail frag에서 메뉴 선택 후 담기 시 작동 */
        /*getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("menuWithOpt", this) { requestKey, bundle ->
            val opt = bundle.getString("opt")
            println("데이터 받음: ${opt}")
            //optInfo.put(JSONObject(opt))
            setOptArray(opt!!)

            binding.lytCart.setBackgroundResource(R.drawable.btn_baedal_cart)
            binding.lyCartCount.visibility = View.VISIBLE
            binding.tvCartCount.text = orderList.length().toString()
            binding.btnCart.setEnabled(true)
        }
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("fromConfirmFrag", this) { requestKey, bundle ->
            val countChanged = JSONObject(bundle.getString("countChanged"))
            //optInfo = JSONArray(bundle.getString("optInfo"))

            var tempArray = JSONArray()
            var position = mutableListOf<String>()
            for (i in countChanged.keys())
                position.add(i)
            for (i in 0 until orderList.length()) {
                if (position.contains(i.toString())) {
                    if (countChanged[i.toString()] != 0) {
                        val obj = orderList.getJSONObject(i)
                        val menuName = obj.getString("menuName")
                        val price = obj.getInt("price")
                        val count = countChanged[i.toString()]
                        val optString = jArrayToList(obj.getJSONArray("optString"))
                        tempArray.put(
                            JSONObject(mapOf(
                                "menuName" to menuName,"price" to price,"count" to count, "optString" to optString
                            ))
                        )
                    }
                } else {
                    tempArray.put(orderList[i])
                }
            }

            orderList = tempArray
            binding.tvCartCount.text = orderList.length().toString()
            if (orderList.length() == 0) {
                binding.btnCart.setBackgroundResource(R.drawable.btn_baedal_cart_empty)
                //binding.lyCartCount.visibility = View.INVISIBLE
                binding.btnCart.setEnabled(false)
            }
        }

        binding.btnCart.setOnClickListener {
            setFrag(
                FragmentBaedalConfirm(), mapOf(
                    "postNum" to postNum!!, "storeName" to storeName, "baedalFee" to baedalFee,
                    "member" to member!!, "orderList" to orderList.toString(), "isPosting" to isPosting!!))
        }*/
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
        adapter.notifyDataSetChanged()
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