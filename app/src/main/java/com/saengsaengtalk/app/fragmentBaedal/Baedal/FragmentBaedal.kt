package com.saengsaengtalk.app.fragmentBaedal.Baedal

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.BaedalPost
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragBaedalBinding
import com.saengsaengtalk.app.fragmentAccount.FragmentAccount
import com.saengsaengtalk.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.saengsaengtalk.app.fragmentBaedal.BaedalHistory.FragmentBaedalHistory
import com.saengsaengtalk.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FragmentBaedal :Fragment() {
    var isTouched = false

    private var mBinding: FragBaedalBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalBinding.inflate(inflater, container, false)

        binding.btnOption.setOnClickListener { setFrag(FragmentAccount(), fragIndex=0) }
        binding.tvBaedalHistory.setOnClickListener { setFrag(FragmentBaedalHistory()) }
        binding.btnBaedalPostAdd.setOnClickListener { setFrag(FragmentBaedalAdd()) }
        binding.scrollView.setOnTouchListener { _, event -> isTouched = when (event.action)
            {
                MotionEvent.ACTION_UP ->
                {
                    if (isTouched && binding.scrollView.scrollY == 0) getPostPreview()
                    false
                }
                else -> true
            }
            return@setOnTouchListener false
        }

        getPostPreview()

        return binding.root
    }

    fun getPostPreview() {
        val loopingDialog = looping()
        api.getBaedalPostList("joined").enqueue(object : Callback<List<BaedalPost>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<BaedalPost>>, response: Response<List<BaedalPost>>) {
                looping(false, loopingDialog)
                if (response.code() == 200) {
                    val baedalPosts = response.body()!!.sortedBy { it.orderTime }
                    mappingAdapter(baedalPosts, "joined")
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
                    val baedalPosts = response.body()!!.sortedBy { it.orderTime }
                    mappingAdapter(baedalPosts, "joinable")
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
    fun mappingAdapter(baedalPosts: List<BaedalPost>, table: String) {

        val tables = mutableListOf<Table>()
        val dates = mutableListOf<LocalDate>()
        var tableIdx = -1
            for (post in baedalPosts) {
            val date = LocalDate.parse(post.orderTime.substring(0 until 16), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            if (date !in dates) {
                dates.add(date)
                tables.add(Table(date, mutableListOf(post)))
                tableIdx += 1
            } else tables[tableIdx].rows.add(post)
        }

        if (tables.size > 0) {
            val adapter = TableAdapter(requireContext(), tables)
            if (table == "joined") {
                binding.rvBaedalListJoined.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rvBaedalListJoined.setHasFixedSize(true)
                binding.rvBaedalListJoined.adapter = adapter
            } else {
                binding.rvBaedalListJoinable.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rvBaedalListJoinable.setHasFixedSize(true)
                binding.rvBaedalListJoinable.adapter = adapter
            }
            adapter.addListener(object : TableAdapter.OnItemClickListener {
                override fun onClick(postId: String) {
                    Log.d("배달프래그먼트 온클릭", "${postId}")
                    setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
                }
            })
        }
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