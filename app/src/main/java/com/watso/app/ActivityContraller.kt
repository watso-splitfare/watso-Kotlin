package com.watso.app

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import androidx.fragment.app.Fragment
import com.watso.app.fragmentAccount.FragmentLogin

class ActivityController(private val activity: MainActivity) {
    private var progressStack = 0
    private val prefs = MainActivity.prefs

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1, fragIndex:Int = 1) {
        hideSoftInput()
        activity.setFrag(fragment, arguments, popBackStack, fragIndex)
    }

    fun onBackPressed() {
        hideSoftInput()
        activity.onBackPressed()
    }

    fun showProgressBar() {
        if (progressStack == 0) {
            activity.showProgress()
        }
        progressStack += 1
    }

    fun hideProgressBar() {
        progressStack -= 1
        if (progressStack == 0) {
            activity.hideProgress()
        }
    }

    fun setString(key: String, value: String) {
        prefs.setString(key, value)
    }

    fun getString(key: String, defValue: String = ""): String {
        return prefs.getString(key, defValue)
    }

    fun removeString(key: String) {
        prefs.removeString(key)
    }

    fun makeToast(message: String){
        activity.makeToast(message)
    }

    fun requestNotiPermission() {
        activity.requestNotiPermission()
    }

    fun getNotiPermission() {
        activity.getNotiPermission()
    }

    fun showSoftInput(view: View) {
        view.requestFocus()
        activity.showSoftInput(view)
    }

    fun hideSoftInput() {
        activity.hideSoftInput()
    }

    private fun verifyInputFormat(case: String, text: String): Boolean {
        var regexPattern = Regex("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,16}$")
        when (case) {
            "realName" -> { regexPattern = Regex("^[a-zA-Z가-힣\\s]{2,20}\$") }
            "nickname" -> { regexPattern = Regex("^[a-zA-Z가-힣0-9]{2,10}\$") }
            "username" -> { regexPattern = Regex("^[a-zA-Z0-9]{5,20}\$") }
            "password" -> { regexPattern = Regex("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[~!@#\$%^&*_\\-+=`|\\\\(){}\\[\\]:;\\\"\\'<>,.?/])[a-zA-Z0-9~!@#\$%^&*_\\-+=`|\\\\(){}\\[\\]:;\\\"\\'<>,.?/]{8,40}\$")}
            "accountNum" -> { regexPattern = Regex("^[a-zA-Z0-9가-힣-\\s]{8,30}\$") }
            "email" -> { regexPattern = Regex("^[a-zA-Z0-9]{2,20}@[a-zA-Z0-9.]{2,20}\$") }
        }
        return regexPattern.matches(text)
    }

    fun verifyInput(case: String, text: String): String {
        return if (verifyInputFormat(case, text)) {
            ""
        } else {
            when(case) {
                "realName" -> {"이름은 한글 또는 영문자로 구성되어야 합니다"}
                "nickname" -> {"닉네임은 2~10자의 한글 또는 영문자, 숫자로 구성되어야 합니다."}
                "username" -> {"아이디는 5~20자의 영문자 또는 숫자로 구성되어야 합니다."}
                "password" -> {"비밀번호는 영문자, 숫자, 특수문자를 각각 하나이상 포함하여 8~40자로 구성되어야 합니다."}
                "accountNum" -> {"사용할 수 없는 계좌번호 형식입니다."}
                else -> {"이메일 형식이 옳바르지 않습니다."}
            }
        }
    }

    fun showAlert(msg: String, title: String? = null) {
        AlertDialog.Builder(activity).setTitle(title)
            .setMessage(msg)
            .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ -> })
            .show()
    }

    fun copyToClipboard(label:String="", content: String) {
        activity.copyToClipboard(label, content)
    }

    fun decodeToken(jwt: String): String {
        return activity.decodeToken(jwt)
    }

    fun logOut(message: String?=null) {
        message?.let { makeToast(it) }

        prefs.removeString("accessToken")
        prefs.removeString("refreshToken")
        prefs.removeString("userId")
        prefs.removeString("nickname")
        setFrag(FragmentLogin(), null, 0)
    }
}