package com.watso.app.fragmentAccount

import android.app.AlertDialog
import android.content.DialogInterface

class VerifyInputFormat {
    fun verifyInputFormat(case: String, text: String): Boolean {
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
}