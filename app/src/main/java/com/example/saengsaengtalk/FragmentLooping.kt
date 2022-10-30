package com.example.saengsaengtalk

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.databinding.FragLoopingBinding

class FragmentLooping :Fragment() {

    private var mBinding: FragLoopingBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoopingBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    fun refreshView() {

        val loop = binding.lottie
        loop.setAnimation("looping-loader-animation.json")
        loop.repeatCount = ValueAnimator.INFINITE
        loop.playAnimation()

    }

    /*fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex: Int) {
        println("setIndex = ${fragIndex}")
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }*/
}