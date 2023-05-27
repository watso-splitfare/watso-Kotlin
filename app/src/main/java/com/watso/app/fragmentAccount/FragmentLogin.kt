package com.watso.app.fragmentAccount

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.API.LoginModel
import com.watso.app.API.UserInfo
import com.watso.app.API.VoidResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragLoginBinding
import com.watso.app.fragmentBaedal.Baedal.FragmentBaedal
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FragmentLogin :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    var mBinding: FragLoginBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragLogin"
    val api= API.create()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoginBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        /** 로그인 */
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (verifyInput("username", username) && verifyInput("password", password)) login()
        }

        binding.tvFindAccount.setOnClickListener { AC.setFrag(FragmentFindAccount()) }
        binding.btnSignup.setOnClickListener { AC.setFrag(FragmentSignUp()) }
    }

    fun login() {
        val reg = AC.getString("registration", "")
        AC.showProgressBar()
        api.login(LoginModel(binding.etUsername.text.toString(), binding.etPassword.text.toString(), reg)).enqueue(object: Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code()==200) {
                    val tokens = response.headers().get("Authentication").toString().split("/")
                    val payload = AC.decodeToken(tokens[0])
                    val dUserId = JSONObject(payload).getString("user_id")
                    val dNickname = JSONObject(payload).getString("nickname")

                    AC.setString("accessToken", tokens[0])
                    AC.setString("refreshToken", tokens[1])
                    AC.setString("userId", dUserId)

                    getUserInfo()
                } else  {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        AC.showAlert(errorResponse.msg)
                        Log.d("$TAG[login]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) { Log.e("$TAG[login]", e.toString())}
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("login Fragment - login", t.message.toString())
                AC.makeToast("다시 시도해 주세요.")
            }
        })
    }

    fun getUserInfo() {
        AC.showProgressBar()
        api.getUserInfo().enqueue(object : Callback<UserInfo> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    response.body()?.let { AC.setUserInfo(it) }
                    AC.setFrag(FragmentBaedal(), popBackStack=0, fragIndex=1)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    AC.logOut(errorResponse.msg)
                    Log.d("$TAG[getUserInfo]", "${errorResponse.code}: ${errorResponse.msg}")
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("$TAG[getUserInfo]", t.message.toString())
                AC.makeToast("유저 정보 갱신 실패")
            }
        })
    }

    fun verifyInput(case: String, text: String): Boolean {
        val message = AC.verifyInput(case, text)
        return if (message == "") {
            true
        } else {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setMessage(message)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                .show()
            false
        }
    }
}