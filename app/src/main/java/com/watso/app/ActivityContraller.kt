package com.watso.app

import android.util.Log
import androidx.fragment.app.Fragment

class ActivityController(private val activity: MainActivity) {
    private var progressStack = 0

    fun showProgressBar() {
        if (progressStack == 0) {
            val mActivity = activity
            mActivity.showProgress()
        }
        progressStack += 1
        Log.d("엑티비티 컨트롤러 + ", progressStack.toString())
    }

    fun hideProgressBar() {
        progressStack -= 1
        if (progressStack == 0) {
            val mActivity = activity
            mActivity.hideProgress()
        }
        Log.d("엑티비티 컨트롤러 - ", progressStack.toString())
    }

    fun makeToast(message: String){
        val mActivity = activity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex:Int = 1) {
        val mActivity = activity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}