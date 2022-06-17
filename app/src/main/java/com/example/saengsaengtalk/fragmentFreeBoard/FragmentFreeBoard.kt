package com.example.saengsaengtalk.fragmentFreeBoard

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragFreeBoardBinding
import com.example.saengsaengtalk.fragmentFreeBoard.adapterFB.PostInList
import com.example.saengsaengtalk.fragmentFreeBoard.adapterFB.PostInListAdapter
import java.time.LocalDateTime

class FragmentFreeBoard :Fragment() {

    private var mBinding: FragFreeBoardBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragFreeBoardBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.lytPostAdd.setOnClickListener { setFrag(FragmentFreeBoardAdd()) }
        val postsInList = mutableListOf<PostInList>()
        postsInList.add(PostInList(1, "자유게시판 입니다.", "게시판관리자",
            arrayOf("주넝이", "동동이"), 3, LocalDateTime.parse("2022-04-04T15:10:00")))
        postsInList.add(PostInList(2, "주제와 상관없이 글을 작성할 수 있습니다.", "게시판관리자",
            arrayOf("주넝이", "동동이", "징징이"), 2, LocalDateTime.parse("2022-04-04T16:10:00")))
        postsInList.add(PostInList(3, "생생톡 화이팅", "주넝이",
            arrayOf("주넝이"), 0, LocalDateTime.parse("2022-04-08T15:10:00")))
        postsInList.add(PostInList(4, "ㅎㅇ", "익명",
            arrayOf("동동이", "ㅋㅋ"), 3, LocalDateTime.parse("2022-04-09T15:10:00")))
        postsInList.add(PostInList(5, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@", "익명",
            arrayOf("동동이", "ㅋㅋ"), 3, LocalDateTime.now()))

        val adapter = PostInListAdapter(postsInList)
        binding.rvPostList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvPostList.adapter = adapter

        adapter.setItemClickListener(object: PostInListAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                Log.d("자유게시판 온클릭", "${postsInList[position].postNum}")
                setFrag(FragmentFreeBoardPost(), mapOf("postNum" to postsInList[position].postNum.toString()))
            }
        })
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }
}