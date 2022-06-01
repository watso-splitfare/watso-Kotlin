package com.example.saengsaengtalk.fragmentBaedal

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
import com.example.saengsaengtalk.adapterBaedal.BaedalComment
import com.example.saengsaengtalk.adapterBaedal.BaedalCommentAdapter
import com.example.saengsaengtalk.databinding.FragBaedalConfirmBinding
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalConfirm :Fragment() {
    private var postNum: String? = null

    private var mBinding: FragBaedalConfirmBinding? = null
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
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}