package com.example.saengsaengtalk.fragmentBaedal

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
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterBaedal.BaedalList
import com.example.saengsaengtalk.adapterBaedal.BaedalListAdapter
import com.example.saengsaengtalk.databinding.FragBaedalAddBinding
import com.example.saengsaengtalk.databinding.FragBaedalListBinding
import java.time.LocalDateTime

class FragmentBaedalAdd :Fragment() {

    private var mBinding: FragBaedalAddBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalAddBinding.inflate(inflater, container, false)

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