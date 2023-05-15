package com.watso.app

import API
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.watso.app.API.*
import com.watso.app.databinding.FragHomeBinding

class FragmentHome :Fragment() {
    private var mBinding: FragHomeBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragHomeBinding.inflate(inflater, container, false)

        refreshView()
        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
    }

    fun getBaedalPostPreview() {
    }

    fun mappingBaedalAdapter(baedalPosts: List<BaedalPost>) {
    }

    fun getTaxiPostPreview() {
    }

    fun mappingTaxiAdapter(taxiPosts: List<TaxiPostPreviewModel>) {
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex: Int) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}