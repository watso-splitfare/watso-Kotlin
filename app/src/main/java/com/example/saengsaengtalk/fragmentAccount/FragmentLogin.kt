package com.example.saengsaengtalk.fragmentAccount

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.APIS.LoginModel
import com.example.saengsaengtalk.APIS.LoginResult
import com.example.saengsaengtalk.LoopingDialog
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragLoginBinding
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
            api.login(LoginModel(binding.etId.text.toString(), binding.etPw.text.toString())).enqueue(object: Callback<LoginResult> {
                override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                    if (response.code()==200) {
                        if (response.body()!!.success!!) {
                            val payload = decodeToken(response.headers().get("Authentication").toString())
                            val dId = JSONObject(payload).getString("id")
                            val dNickname = JSONObject(payload).getString("nick_name")

                            MainActivity.prefs.setString("Authentication", response.headers().get("Authentication").toString())
                            MainActivity.prefs.setString("userId", dId)
                            MainActivity.prefs.setString("nickname", dNickname)

                            onBackPressed()
                            looping(false, loopingDialog)
                        } else makeToast("등록된 계정 정보가 일치하지 않습니다.")
                    } else {
                        Log.e("login Fragment - login", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                    looping(false, loopingDialog)
                }

                override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                    Log.e("login Fragment - login", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                    looping(false, loopingDialog)
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