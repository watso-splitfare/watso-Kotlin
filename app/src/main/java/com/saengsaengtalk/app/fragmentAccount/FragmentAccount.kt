package com.saengsaengtalk.app.fragmentAccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.saengsaengtalk.app.APIS.VoidResponse
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragAccountBinding
import com.saengsaengtalk.app.fragmentAccount.admin.FragmentAdmin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentAccount :Fragment() {
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

    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        binding.tvAuth.text = MainActivity.prefs.getString("accessToken", "")
        binding.tvUsername.text = MainActivity.prefs.getString("userId", "")
        binding.tvNickname.text = MainActivity.prefs.getString("nickname", "")

        binding.btnLogout.setOnClickListener {
            val refreshToken = MainActivity.prefs.getString("refreshToken", "")
            api.logout().enqueue(object: Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    if (response.code() == 204) {
                        MainActivity.prefs.removeString("accessToken")
                        MainActivity.prefs.removeString("refreshToken")
                        MainActivity.prefs.removeString("userId")
                        MainActivity.prefs.removeString("nickname")
                        makeToast("로그아웃 되었습니다.")
                        onBackPressed()
                    } else {
                        Log.e("로그아웃 에러", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                    Log.d("로그아웃", response.toString())
                    Log.d("로그아웃", response.body().toString())
                    Log.d("로그아웃", response.headers().toString())
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    Log.d("로그아웃",t.message.toString())
                    Log.d("로그아웃","fail")
                }
            })
        }

        binding.btnRemoveCache.setOnClickListener {
            MainActivity.prefs.removeString("accessToken")
            MainActivity.prefs.removeString("refreshToken")
            MainActivity.prefs.removeString("userId")
            MainActivity.prefs.removeString("nickname")

        }
        // 빈칸에 a를 입력 후 버튼을 누르면 어드민 화면에 진입합니다.
        // 어드민 계정 인증 관련해서 논의 필요
        binding.btnAdmin.setOnClickListener {
            if (binding.etAdmin.text.toString() == "a") setFrag(FragmentAdmin())
        }
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
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