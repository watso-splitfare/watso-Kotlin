package com.saengsaengtalk.app.fragmentAccount

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.saengsaengtalk.app.APIS.LoginModel
import com.saengsaengtalk.app.APIS.LoginResult
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragLoginBinding
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentLogin :Fragment() {

    private var mBinding: FragLoginBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoginBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        /** 로그인 */
        binding.btnLogin.setOnClickListener {
            val loopingDialog = looping()
            val prefs = MainActivity.prefs
            val reg = prefs.getString("registration", "")
            api.login(LoginModel(binding.etId.text.toString(), binding.etPw.text.toString(), reg)).enqueue(object: Callback<LoginResult> {
                override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                    looping(false, loopingDialog)
                    Log.d("FragLogin response.code()", response.code().toString())
                    if (response.code()==200) {
                        val tokens = response.headers().get("Authentication").toString().split("/")
                        val payload = decodeToken(tokens[0])
                        Log.d("FragLogin token[0]", tokens[0])
                        Log.d("FragLogin payload", payload)
                        val dUserId = JSONObject(payload).getString("user_id")
                        val dNickname = JSONObject(payload).getString("nickname")

                        prefs.setString("accessToken", tokens[0])
                        prefs.setString("refreshToken", tokens[1])
                        prefs.setString("userId", dUserId)
                        prefs.setString("nickname", dNickname)

                        onBackPressed()

                        Log.d("access", prefs.getString("accessToken", ""))
                        Log.d("refresh", prefs.getString("refreshToken", ""))

                    } else {
                        Log.e("login Fragment - login", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                }

                override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("login Fragment - login", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                }
            })
        }

        binding.tvFindAccount.setOnClickListener {
            setFrag(FragmentFindAccount())
        }
        binding.tvSignUp.setOnClickListener { setFrag(FragmentSignUp()) }
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun decodeToken(jwt: String): String {
        val mActivity = activity as MainActivity
        return mActivity.decodeToken(jwt)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}