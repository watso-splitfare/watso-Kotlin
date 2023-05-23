package com.watso.app.fragmentBaedal.Baedal

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.BaedalPost
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragBaedalBinding
import com.watso.app.fragmentAccount.FragmentAccount
import com.watso.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.watso.app.fragmentBaedal.BaedalHistory.FragmentBaedalHistory
import com.watso.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FragmentBaedal :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    lateinit var joinedAdapter: TableAdapter
    lateinit var joinableAdapter: TableAdapter
    lateinit var joinablePosts: List<BaedalPost>

    var mBinding: FragBaedalBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragBaedal"
    val api= API.create()

    var viewClickAble = true    // 포스트 중복 클릭 방지
    var joined = true       // 참가한 게시글 여부
    var joinable = true     // 참가 가능한 게시글 여부

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("deletePost", this) {
                requestKey, bundle -> getPostPreview()
        }
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("backToBaedalList", this) {
                requestKey, bundle -> getPostPreview()
        }

        binding.btnOption.setOnClickListener { AC.setFrag(FragmentAccount(), fragIndex=0) }
        binding.btnBaedalHistory.setOnClickListener { AC.setFrag(FragmentBaedalHistory()) }
        binding.btnBaedalPostAdd.setOnClickListener { AC.setFrag(FragmentBaedalAdd()) }
        binding.lytRefresh.setOnRefreshListener {
            binding.lytRefresh.isRefreshing = false
            getPostPreview()
        }

        setAdapter()
        getPostPreview()

        return binding.root
    }

    fun setAdapter() {
        val mActivity = activity as MainActivity
        joinedAdapter = TableAdapter(mActivity)
        joinableAdapter = TableAdapter(mActivity)

        joinedAdapter.setPostClickListener(object: TableAdapter.OnPostClickListener {
            override fun onClick(postId: String) {
                if (viewClickAble) {
                    viewClickAble = false
                    AC.setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })
        joinableAdapter.setPostClickListener(object: TableAdapter.OnPostClickListener {
            override fun onClick(postId: String) {
                if (viewClickAble) {
                    viewClickAble = false
                    AC.setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })

        binding.rvBaedalListJoined.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoined.setHasFixedSize(true)
        binding.rvBaedalListJoined.adapter = joinedAdapter

        binding.rvBaedalListJoinable.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoinable.setHasFixedSize(true)
        binding.rvBaedalListJoinable.adapter = joinableAdapter
    }

    fun getPostPreview() {
        AC.showProgressBar()
        api.getBaedalPostList("joined").enqueue(object : Callback<List<BaedalPost>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<BaedalPost>>, response: Response<List<BaedalPost>>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    val joinedPosts = response.body()!!.sortedBy { it.orderTime }
                    mappingPostDate(joinedPosts, true)
                } else if (response.code() == 401) {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    AC.logOut(errorResponse.msg)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    AC.makeToast(errorResponse.msg)
                    Log.d("$TAG[getBaedalPostList-joined]", "${errorResponse.code}: ${errorResponse.msg}")
                }
            }

            override fun onFailure(call: Call<List<BaedalPost>>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedal Fragment - getBaedalPostListJoined", t.message.toString())
                AC.makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
            }
        })

        AC.showProgressBar()
        api.getBaedalPostList("joinable").enqueue(object : Callback<List<BaedalPost>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<BaedalPost>>, response: Response<List<BaedalPost>>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    joinablePosts = response.body()!!.sortedBy { it.orderTime }
                    mappingPostDate(joinablePosts)
                    setSpiner()
                } else if (response.code() == 401) {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    AC.logOut(errorResponse.msg)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    AC.makeToast(errorResponse.msg)
                    Log.d("$[TAG][getBaedalPostList-joinable]", "${errorResponse.code}: ${errorResponse.msg}")
                }
            }

            override fun onFailure(call: Call<List<BaedalPost>>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedal Fragment - getBaedalPostListJoinable", t.message.toString())
                AC.makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mappingPostDate(posts: List<BaedalPost>, isJoinedTable: Boolean = false) {
        val tables = mutableListOf<Table>()
        val dates = mutableListOf<LocalDate>()
        var tableIdx = -1
        for (post in posts) {
            val date = LocalDate.parse(post.orderTime.substring(0 until 16), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            if (date !in dates) {
                dates.add(date)
                tables.add(Table(date, mutableListOf(post)))
                tableIdx += 1
            } else tables[tableIdx].rows.add(post)
        }

        if (tables.isNotEmpty()) {
            binding.lytEmptyList.visibility = View.GONE
            if (isJoinedTable) {
                joinedAdapter.setData(tables)
                binding.lytJoinedTable.visibility = View.VISIBLE
                joined = true
            } else {
                joinableAdapter.setData(tables)
                binding.divJoinable.visibility = View.VISIBLE
                binding.rvBaedalListJoinable.visibility = View.VISIBLE
                joinable = true
            }
        } else {
            if (isJoinedTable) {
                binding.lytJoinedTable.visibility = View.GONE
                joined = false
            } else {
                binding.divJoinable.visibility = View.GONE
                binding.rvBaedalListJoinable.visibility = View.GONE
                joinable = false
            }
        }

        if (!joined && !joinable) {
            binding.lytEmptyList.visibility = View.VISIBLE
            binding.lytEmptyList.setOnClickListener { AC.setFrag(FragmentBaedalAdd()) }
        } else binding.lytEmptyList.visibility = View.GONE
    }

    fun setSpiner() {
        val places = listOf("모두", "생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(fragmentContext, R.layout.simple_list_item_1, places)
        binding.spnFilter.adapter = placeSpinerAdapter
        binding.spnFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                when (position) {
                    0 -> mappingPostDate(joinablePosts)
                    1 -> mappingPostDate(getFilteredPosts(places[1]))
                    2 -> mappingPostDate(getFilteredPosts(places[2]))
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
    }

    fun getFilteredPosts(filterBy: String): List<BaedalPost> {
        val filteredPosts = mutableListOf<BaedalPost>()
        joinablePosts.forEach {
            if (it.place == filterBy) filteredPosts.add(it)
        }
        return filteredPosts
    }
}