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
import com.watso.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.watso.app.fragmentBaedal.BaedalOrders.FragmentBaedalOrders
import com.watso.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentBaedalHistory :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    var mBinding: FragBaedalHistoryBinding? = null
    val binding get() = mBinding!!
    val api= API.create()

    var isTouched = false

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

        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }
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
                    if (baedalPosts.isEmpty()) {
                        binding.rvBaedalListJoined.visibility = View.GONE
                        binding.lytEmptyList.visibility = View.VISIBLE
                        binding.lytEmptyList.setOnClickListener { AC.setFrag(FragmentBaedalAdd()) }
                    } else {
                        binding.rvBaedalListJoined.visibility = View.VISIBLE
                        binding.lytEmptyList.visibility = View.GONE
                        baedalPosts.addAll(response.body()!!)
                        mappingAdapter(baedalPosts)
                    }
                } else {
                    Log.e("baedal Fragment - getBaedalPostListJoined", response.toString())
                    AC.makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                }
            }

            override fun onFailure(call: Call<List<BaedalPost>>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedal Fragment - getBaedalPostListJoined", t.message.toString())
                AC.makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
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
                AC.setFrag(FragmentBaedalOrders(), mapOf("postJson" to postJson, "isMyOrder" to "true"))
            }
        })
        adapter.setShowPostListener(object : HistoryAdapter.OnPostBtnListener {
            override fun showPost(postId: String) {
                AC.setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
            }
        })
    }
}