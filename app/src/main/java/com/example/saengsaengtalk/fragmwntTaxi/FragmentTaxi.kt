package com.example.saengsaengtalk.fragmwntTaxi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.databinding.FragTaxiBinding

class FragmentTaxi :Fragment() {

    private var mBinding: FragTaxiBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragTaxiBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {

    }
}