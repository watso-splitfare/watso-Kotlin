package com.watso.app.fragmentBaedal.BaedalHistory

import android.annotation.SuppressLint
import android.content.Context
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
import com.watso.app.API.BaedalPost
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragBaedalHistoryBinding
import com.watso.app.fragmentBaedal.BaedalOrders.FragmentBaedalOrders
import com.watso.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentBaedalHistory :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    var isTouched = false

    private var mBinding: FragBaedalHistoryBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalHistoryBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

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

        binding.btnPrevious.setOnClickListener { onBackPressed() }
        getPostPreview()

        return binding.root
    }

    fun getPostPreview() {
        AC.showProgressBar()
        api.getBaedalPostList("all").enqueue(object : Callback<List<BaedalPost>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<BaedalPost>>, response: Response<List<BaedalPost>>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    val baedalPosts = mutableListOf<BaedalPost>()
                    baedalPosts.addAll(response.body()!!)
                    mappingAdapter(baedalPosts)
                } else {
                    Log.e("baedal Fragment - getBaedalPostListJoined", response.toString())
                    makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                }
            }

            override fun onFailure(call: Call<List<BaedalPost>>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedal Fragment - getBaedalPostListJoined", t.message.toString())
                makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mappingAdapter(baedalPosts: MutableList<BaedalPost>) {

        val adapter = HistoryAdapter(baedalPosts)

        binding.rvBaedalListJoined.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoined.setHasFixedSize(true)
        binding.rvBaedalListJoined.adapter = adapter

        adapter.setShowOrderListener(object : HistoryAdapter.OnOrderBtnListener {
            override fun showOrder(postJson: String) {
                setFrag(FragmentBaedalOrders(), mapOf("postJson" to postJson, "isMyOrder" to "true"))
            }
        })
        adapter.setShowPostListener(object : HistoryAdapter.OnPostBtnListener {
            override fun showPost(postId: String) {
                setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
            }
        })
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex:Int = 1) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}