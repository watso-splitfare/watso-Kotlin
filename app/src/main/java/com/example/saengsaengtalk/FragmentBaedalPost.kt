package com.example.saengsaengtalk

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.adapterBaedal.BaedalComment
import com.example.saengsaengtalk.adapterBaedal.BaedalCommentAdapter
import com.example.saengsaengtalk.adapterBaedal.BaedalList
import com.example.saengsaengtalk.adapterBaedal.BaedalListAdapter
import com.example.saengsaengtalk.databinding.FragBaedalBinding
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
import java.time.LocalDateTime

class FragmentBaedalPost :Fragment() {
    private var postNum: String? = null

    private var mBinding: FragBaedalPostBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
        }

        Log.d("배달 포스트", "게시물 번호: ${postNum}")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //val view = inflater.inflate(R.layout.frag_baedal, container, false)

        mBinding = FragBaedalPostBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
        //return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {

        /* 배달 */
        val comment = arrayListOf(
            BaedalComment("동동이", "네네치킨 먹을 사람 드루와~", LocalDateTime.now(), "test1"),
            BaedalComment("주넝이", "네네치킨 먹을 사람~", LocalDateTime.now(), "test2"),
            BaedalComment("동동이", "먹을 사람 드루와~", LocalDateTime.now(), "test3"),
            BaedalComment("동동이", "네네치킨~", LocalDateTime.now(), "test4")

        )
        binding.rvComment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvComment.setHasFixedSize(true)
        binding.rvComment.adapter = BaedalCommentAdapter(comment)
    }
}