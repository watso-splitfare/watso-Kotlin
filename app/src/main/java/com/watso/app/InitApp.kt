package com.watso.app

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.API.FcmToken
import com.watso.app.API.UserInfo
import com.watso.app.API.VoidResponse
import com.watso.app.fragmentAccount.FragmentLogin
import com.watso.app.fragmentBaedal.Baedal.FragmentBaedal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


const val TAG = "InitApp"

fun initApp(activity: MainActivity) {
    val METHOD = "initApp"
    val refreshToken = MainActivity.prefs.getString("refreshToken", "")
    Log.d("[$TAG][$METHOD]access token", MainActivity.prefs.getString("accessToken", ""))
    Log.d("[$TAG][$METHOD]refresh token", refreshToken)

    if (refreshToken != "") {
        activity.showProgress()
        activity.api.getUserInfo().enqueue(object : Callback<UserInfo> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                activity.hideProgress()
                if (response.code() == 200) {
                    response.body()?.let { activity.setUserInfo(it) }
                    onAuthenticationSuccess(activity)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    activity.logOut(errorResponse.msg)
                    Log.d("[$TAG][$METHOD][getUserInfo]", "${errorResponse.code}: ${errorResponse.msg}")
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                activity.hideProgress()
                Log.e("[$TAG][$METHOD][getUserInfo]", t.message.toString())
                activity.logOut("유저 정보 갱신 실패")
            }
        })
    } else {
        setFragLogin(activity)
    }
}

fun onAuthenticationSuccess(activity: MainActivity) {
    val METHOD = "onAuthenticationSuccess"
    Log.d("[$TAG][$METHOD]", "")

    val postId = activity.intent.getStringExtra("post_id")
    if (postId != null) {
        setFragHome(activity)
    } else {
        checkFCMToken(activity)
    }
}

fun checkFCMToken(activity: MainActivity) {
    val METHOD = "checkFCMToken"
    Log.d("[$TAG][$METHOD]", "")

    activity.showProgress()
    val messagingService = MyFirebaseMessagingService()
    messagingService.getFirebaseToken { token ->
        if (token != null) {
            activity.hideProgress()
            val previous = MainActivity.prefs.getString("previousFcmToken", "")
            Log.d("[$TAG][$METHOD]", "previous: $previous, current: $token")
            if (token != previous) {
                sendFcmToken(activity, token)
            } else setFragHome(activity)
        } else {
            activity.hideProgress()
            Log.e("[$TAG][$METHOD]", "Error retrieving Firebase Token")
            setFragHome(activity)
        }
    }
}

fun sendFcmToken(activity: MainActivity, token: String) {
    val METHOD = "sendFcmToken"
    Log.d("[$TAG][$METHOD]", "token: $token")

    activity.showProgress()
    activity.api.sendFcmToken(FcmToken(token)).enqueue(object : Callback<VoidResponse> {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
            activity.hideProgress()
            if (response.code() == 204) {
                MainActivity.prefs.setString("previousFcmToken", token)
            }
            else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                Log.d("[$TAG][$METHOD]", "${errorResponse.code}: ${errorResponse.msg}")
            }
            setFragHome(activity)
        }

        override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
            activity.hideProgress()
            Log.e("[$TAG][$METHOD]", t.message.toString())
            activity.makeToast("fcm 토큰 전송 실패")
            setFragHome(activity)
        }
    })
}

fun setFragHome(activity: MainActivity) {
    activity.setFrag(FragmentBaedal(), popBackStack = 0, fragIndex = 1)
}

fun setFragLogin(activity: MainActivity) {
    activity.setFrag(FragmentLogin(), null, 0)
}