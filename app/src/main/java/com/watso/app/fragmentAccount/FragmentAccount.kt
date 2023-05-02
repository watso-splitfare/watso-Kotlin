package com.watso.app.fragmentAccount

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.watso.app.API.UserInfo
import com.watso.app.API.VoidResponse
import com.watso.app.MainActivity
import com.watso.app.databinding.FragAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentAccount :Fragment() {
    lateinit var userInfo: UserInfo
    private var mBinding: FragAccountBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragAccountBinding.inflate(inflater, container, false)

        getUserInfo()
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

        binding.lytLogout.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("로그아웃하기")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> logout() })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }

        binding.lytPw.setOnClickListener { setFrag(FragmentUpdateAccount(), mapOf("target" to "pw")) }
        binding.lytNickname.setOnClickListener { setFrag(FragmentUpdateAccount(), mapOf("target" to "nickname")) }
        binding.lytAccountNum.setOnClickListener { setFrag(FragmentUpdateAccount(), mapOf("target" to "accountNum")) }
        binding.lytDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("회원 탈퇴하기")
                .setMessage("탈퇴 하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    deleteAccount() })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
    }

    fun getUserInfo() {
        api.getUserInfo().enqueue(object: Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.code()==200) {
                    userInfo = response.body()!!
                    binding.tvRealName.text = userInfo.name
                    binding.tvUsername.text = userInfo.username
                    binding.tvEmail.text = userInfo.email
                    binding.tvNickname.text = userInfo.nickname
                    binding.tvAccountNum.text = userInfo.accountNumber
                } else {
                    Log.e("FragAccount getUserInfo", response.toString())
                    binding.tvUsername.text = "ID"
                    binding.tvNickname.text = "닉네임"
                    makeToast("다시 시도해 주세요.")
                    //onBackPressed()
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                Log.e("FragAccount getUserInfo", t.message.toString())
                binding.tvUsername.text = "ID"
                binding.tvNickname.text = "닉네임"
                makeToast("다시 시도해 주세요.")
                //onBackPressed()
            }
        })
    }

    fun logout() {
        api.logout().enqueue(object: Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {}
                else Log.e("로그아웃 에러", response.toString())
                removePrefs()
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.d("로그아웃",t.message.toString())
                removePrefs()
            }
        })
    }

    fun removePrefs(makeToast: Boolean = true) {
        if (makeToast)
            makeToast("로그아웃 되었습니다.")
        MainActivity.prefs.removeString("accessToken")
        MainActivity.prefs.removeString("refreshToken")
        MainActivity.prefs.removeString("userId")
        MainActivity.prefs.removeString("nickname")
        setFrag(FragmentLogin(), null, 0)
    }

    fun deleteAccount() {
        api.deleteAccount().enqueue(object: Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code()==204) {
                    makeToast("탈퇴되었습니다.")
                    removePrefs(false)
                } else {
                    Log.e("FragAccount deleteAccount", response.toString())
                    makeToast("다시 시도해 주세요.")
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.e("FragAccount deleteAccount", t.message.toString())
                makeToast("다시 시도해 주세요.")
            }
        })
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, popBackStack)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}