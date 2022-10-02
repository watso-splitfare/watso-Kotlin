package com.example.saengsaengtalk.fragmentAccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.APIS.LoginModel
import com.example.saengsaengtalk.APIS.LoginResult
import com.example.saengsaengtalk.APIS.TestResult
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentLogin :Fragment() {

    private var mBinding: FragLoginBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoginBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        /** 로그인 */
        binding.btnLogin.setOnClickListener {
            api.login(LoginModel(binding.etId.text.toString(), binding.etPw.text.toString())).enqueue(object: Callback<LoginResult> {
                override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                    Log.d("로그인", response.toString())
                    Log.d("로그인", response.body().toString())
                    Log.d("로그인응답 헤더", response.headers().toString())
                    MainActivity.prefs.setString(
                        "Authentication", response.headers().get("Authentication").toString())
                }

                override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                    // 실패
                    Log.d("로그인",t.message.toString())
                    Log.d("로그인","fail")
                }
            })
        }

        binding.tvFindAccount.setOnClickListener {
            setFrag(FragmentFindAccount())
        }
        binding.tvSignUp.setOnClickListener { setFrag(FragmentSignUp())}
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}