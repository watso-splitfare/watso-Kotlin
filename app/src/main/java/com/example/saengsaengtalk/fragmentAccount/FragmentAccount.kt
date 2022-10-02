package com.example.saengsaengtalk.fragmentAccount

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.APIS.LogoutResult
import com.example.saengsaengtalk.APIS.TestResult
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragAccountBinding
import com.example.saengsaengtalk.databinding.FragFindAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentAccount :Fragment() {
    var forgot = "id"

    private var mBinding: FragAccountBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragAccountBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @SuppressLint("ResourceAsColor")
    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        binding.btnLogout.setOnClickListener {
            api.logout().enqueue(object: Callback<LogoutResult> {
                override fun onResponse(call: Call<LogoutResult>, response: Response<LogoutResult>) {
                    Log.d("로그아웃", response.toString())
                    Log.d("로그아웃", response.body().toString())
                    Log.d("로그아웃", response.headers().toString())
                }

                override fun onFailure(call: Call<LogoutResult>, t: Throwable) {
                    // 실패
                    Log.d("로그아웃",t.message.toString())
                    Log.d("로그아웃","fail")
                }
            })
        }
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