package com.watso.app.fragmentAccount

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.API.UserInfo
import com.watso.app.API.VoidResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.RequestPermission
import com.watso.app.databinding.FragAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FragmentAccount :Fragment() {
    lateinit var AC: ActivityController
    lateinit var RP: RequestPermission
    lateinit var fragmentContext: Context

    lateinit var userInfo: UserInfo

    var mBinding: FragAccountBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragAccount"
    val api= API.create()

    var notificationSwitchBefore = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onResume() {
        super.onResume()
        val requestPermitted = RP.isNotificationEnabled()
        Log.d("[$TAG]onResusme", requestPermitted.toString())
        if (requestPermitted) {
            AC.setString("notificationPermission", "true")
            notificationSwitchBefore = true
        }
        else {
            AC.setString("notificationPermission", "denied")
            notificationSwitchBefore = false
        }
        bindSWNotificationPermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragAccountBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)
        RP = RequestPermission(activity as MainActivity)

//        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("getUserInfo", this) {
//                requestKey, bundle -> getUserInfo()
//        }
        getUserInfo()
        refreshView()

        return binding.root
    }

    fun getUserInfo() {
        AC.showProgressBar()
        api.getUserInfo().enqueue(object: Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                AC.hideProgressBar()
                if (response.code()==200) {
                    userInfo = response.body()!!
                    binding.tvRealName.text = userInfo.name
                    binding.tvUsername.text = userInfo.username
                    binding.tvEmail.text = userInfo.email
                    binding.tvNickname.text = userInfo.nickname
                    binding.tvAccountNum.text = userInfo.accountNumber

                    AC.setString("nickname", userInfo.nickname)
                    AC.setString("accountNum", userInfo.accountNumber)
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        AC.makeToast(errorResponse.msg)
                        Log.d("$TAG[getUserInfo]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) {
                        Log.e("$TAG[getUserInfo]", e.toString())
                        Log.e("$TAG[getUserInfo]", response.errorBody()!!.toString())
                    }
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("FragAccount getUserInfo", t.message.toString())
                binding.tvUsername.text = "ID"
                binding.tvNickname.text = "닉네임"
                AC.makeToast("다시 시도해 주세요.")
            }
        })
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            AC.onBackPressed()
        }

        binding.lytPassword.setOnClickListener { AC.setFrag(FragmentUpdateAccount(), mapOf("target" to "password")) }
        binding.lytNickname.setOnClickListener { AC.setFrag(FragmentUpdateAccount(), mapOf("target" to "nickname")) }
        binding.lytAccountNum.setOnClickListener { AC.setFrag(FragmentUpdateAccount(), mapOf("target" to "accountNum")) }

        bindSWNotificationPermission()
        binding.swNotification.setOnCheckedChangeListener { _, _ -> setNotificationPermission()}

        binding.lytOss.setOnClickListener {
            startActivity(Intent(fragmentContext, OssLicensesMenuActivity::class.java))
            OssLicensesMenuActivity.setActivityTitle("오픈소스 라이선스")
        }
        binding.lytLogout.setOnClickListener {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setTitle("로그아웃하기")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> logout() })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
        binding.lytDeleteAccount.setOnClickListener {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setTitle("회원 탈퇴하기")
                .setMessage("탈퇴 하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    deleteAccount() })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
    }

    fun bindSWNotificationPermission() {
        when (AC.getString("notificationPermission", "")) {
            "true" -> {
                binding.swNotification.isChecked = true
                notificationSwitchBefore = true
            }
            else -> binding.swNotification.isChecked = false
        }
    }

    fun setNotificationPermission() {
        Log.d(TAG, binding.swNotification.isChecked.toString())
        if (notificationSwitchBefore != binding.swNotification.isChecked) {
            when (AC.getString("notificationPermission", "")) {
                "" -> {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle("알림 권한 요청")
                        .setMessage("게시글 관련 안내사항이나 댓글소식을 알림으로 전달받기 위해서 권한을 요청합니다.")
                        .setPositiveButton("알림 설정", DialogInterface.OnClickListener { dialog, id ->
                            RP.getNotiPermission()
                        })
                        .setNegativeButton("거절", DialogInterface.OnClickListener { dialog, id ->
                            Log.d("TAG, 거절 누름", "")
                            binding.swNotification.isChecked = false
                        })
                    builder.show()
                }
                "true" -> {
                    AC.setString("notificationPermission", "false")
                    notificationSwitchBefore = false
                }
                "denied" -> {
                    RP.makeIntent()
                }
                else -> {
                    AC.setString("notificationPermission", "true")
                    notificationSwitchBefore = true
                }
            }
        }
    }

    fun logout() {
        AC.showProgressBar()
        api.logout().enqueue(object: Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code() == 204) {}
                else Log.d("$TAG[logtout]", response.errorBody()?.string().toString())
                AC.logOut("로그아웃 되었습니다.")
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.d("$TAG[logtout]", t.message.toString())
                AC.logOut("로그아웃 되었습니다.")
            }
        })
    }

    fun deleteAccount() {
        AC.showProgressBar()
        api.deleteAccount().enqueue(object: Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code()==204) {
                    AC.logOut("탈퇴되었습니다.")
                } else {
                    Log.e("FragAccount deleteAccount", response.toString())
                    AC.makeToast("다시 시도해 주세요.")
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("FragAccount deleteAccount", t.message.toString())
                AC.makeToast("다시 시도해 주세요.")
            }
        })
    }
}