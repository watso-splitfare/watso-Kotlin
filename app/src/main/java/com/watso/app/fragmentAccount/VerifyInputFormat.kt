package com.watso.app.fragmentAccount

class VerifyInputFormat {
    fun verifyInputFormat(case: String, text: String): Boolean {
        var regexPattern = Regex("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,16}$")
        when (case) {
            "realName" -> { regexPattern = Regex("^[a-zA-Z가-힣\\s]{1,20}$") }
            "nickname" -> { regexPattern = Regex("^[a-zA-Z가-힣0-9]{2,10}$") }
            "username" -> { regexPattern = Regex("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,16}$") }
            "password" -> { regexPattern = Regex("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[~!@#$%^&])[a-zA-Z0-9~!@#$%^&]{8,16}$") }
            "accountNum" -> { regexPattern = Regex("^[a-zA-Z0-9가-힣-\\s]{8,30}$") }
            "email" -> { regexPattern = Regex("^[a-zA-Z0-9]{2,40}@[a-zA-Z0-9.]{2,20}$") }
        }
        return regexPattern.matches(text)
    }
}