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
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterHome.Comment
import com.example.saengsaengtalk.adapterHome.CommentAdapter
import com.example.saengsaengtalk.databinding.FragFreeBoardPostBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentFreeBoardPost :Fragment() {
    private var postNum: String? = null

    private var mBinding: FragFreeBoardPostBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragFreeBoardPostBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
        }

        Log.d("자유게시판", "게시물 번호: ${postNum}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.tvPostTitle.text = "자유게시판 입니다."
        binding.tvPostWriter.text = "게시판관리자"

        val now = LocalDateTime.now()
        binding.tvPostCreated.text = now.format(
            DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(
                Locale.forLanguageTag("ko")))

        binding.tvContent.text = "자유게시판 이라구요"

        binding.tvLike.text = "2"
        binding.lytLike.setOnClickListener {
            if (binding.tvLike.text == "2") {
                binding.ivLike.setImageResource(R.drawable.heart_red)
                binding.tvLike.text = "3"
            } else {
                binding.ivLike.setImageResource(R.drawable.heart)
                binding.tvLike.text = "2"
            }
        }

        val comment = arrayListOf(
            Comment("동동이", "네네치킨 먹을 사람 드루와~ 123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789", LocalDateTime.now(), 1,0, 0, "동동이"),
            Comment("주넝이", "네네치킨 먹을 사람~ 123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789", LocalDateTime.now(), 2, 1, 0, "주넝이"),
            Comment("동동이", "먹을 사람 드루와~", LocalDateTime.now(), 3, 1, 0, "동동이"),
            Comment("동동이", "네네치킨~", LocalDateTime.now(), 4, 0, 1, "동동이")
        )
        binding.rvComment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvComment.setHasFixedSize(true)
        binding.rvComment.adapter = CommentAdapter(comment)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}