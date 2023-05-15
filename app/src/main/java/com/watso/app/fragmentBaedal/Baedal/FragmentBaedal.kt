package com.watso.app.fragmentBaedal.Baedal

import android.R
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.BaedalPost
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.RequestPermission
import com.watso.app.databinding.FragBaedalBinding
import com.watso.app.fragmentAccount.FragmentAccount
import com.watso.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.watso.app.fragmentBaedal.BaedalHistory.FragmentBaedalHistory
import com.watso.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FragmentBaedal :Fragment() {
    val TAG = "FragBaedal"
    var isTouched = false       // 새로고침 용
    var viewClickAble = true    // 포스트 중복 클릭 방지

    lateinit var joinedAdapter: TableAdapter
    lateinit var joinableAdapter: TableAdapter
    lateinit var joinablePosts: List<BaedalPost>
    var joined = true       // 참가한 게시글 여부
    var joinable = true     // 참가 가능한 게시글 여부

    private var mBinding: FragBaedalBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalBinding.inflate(inflater, container, false)

        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("deletePost", this) {
                requestKey, bundle ->
            getPostPreview()
        }

        binding.btnOption.setOnClickListener { setFrag(FragmentAccount(), fragIndex=0) }
        binding.btnBaedalHistory.setOnClickListener { setFrag(FragmentBaedalHistory()) }
        binding.btnBaedalPostAdd.setOnClickListener { setFrag(FragmentBaedalAdd()) }
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
                    setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })
        joinableAdapter.setPostClickListener(object: TableAdapter.OnPostClickListener {
            override fun onClick(postId: String) {
                if (viewClickAble) {
                    viewClickAble = false
                    setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })

        binding.rvBaedalListJoined.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoined.setHasFixedSize(true)
        binding.rvBaedalListJoined.adapter = joinedAdapter

        binding.rvBaedalListJoinable.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoinable.setHasFixedSize(true)
        binding.rvBaedalListJoinable.adapter = joinableAdapter
    }

    fun getPostPreview() {
        val loopingDialog = looping()
        api.getBaedalPostList("joined").enqueue(object : Callback<List<BaedalPost>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<BaedalPost>>, response: Response<List<BaedalPost>>) {
                looping(false, loopingDialog)
                if (response.code() == 200) {
                    val joinedPosts = response.body()!!.sortedBy { it.orderTime }
                    mappingPostDate(joinedPosts, true)
                } else {
                    Log.e("baedal Fragment - getBaedalPostListJoined", response.toString())
                    makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                }
            }

            override fun onFailure(call: Call<List<BaedalPost>>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("baedal Fragment - getBaedalPostListJoined", t.message.toString())
                makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
            }
        })

        api.getBaedalPostList("joinable").enqueue(object : Callback<List<BaedalPost>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<BaedalPost>>, response: Response<List<BaedalPost>>) {
                looping(false, loopingDialog)
                if (response.code() == 200) {
                    joinablePosts = response.body()!!.sortedBy { it.orderTime }
                    mappingPostDate(joinablePosts)
                    setSpiner()
                } else {
                    Log.e("baedal Fragment - getBaedalPostListJoinable", response.toString())
                    makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                }
            }

            override fun onFailure(call: Call<List<BaedalPost>>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("baedal Fragment - getBaedalPostListJoinable", t.message.toString())
                makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
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
            } else {
                joinableAdapter.setData(tables)
                binding.lytJoinableTable.visibility = View.VISIBLE
            }
        } else {
            if (isJoinedTable) {
                binding.lytJoinedTable.visibility = View.GONE
                joined = false
            } else {
                binding.lytJoinableTable.visibility = View.GONE
                joinable = false
            }
        }

        if (!joined && !joinable) {
            binding.lytEmptyList.visibility = View.VISIBLE
            binding.lytEmptyList.setOnClickListener { setFrag(FragmentBaedalAdd()) }
        } else binding.lytEmptyList.visibility = View.GONE
    }

    fun setSpiner() {
        val places = listOf("모두", "생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, places)
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

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex:Int = 1) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}