package com.watso.app.fragmentAccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.watso.app.API.ForgotPassword
import com.watso.app.API.VoidResponse
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragFindAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentFindAccount :Fragment() {
    val TAG = "FragFindAccount"
    var forgot = "username"
    var isSendAble = true

    private var mBinding: FragFindAccountBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragFindAccountBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
        hideSoftInput()
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.lytFindPassword.visibility = View.GONE

        binding.tvFindUsername.setOnClickListener {
            forgot = "username"
            hideSoftInput()
            binding.etMailPassword.setText("")
            binding.etUsername.setText("")
            binding.lytFindUsername.visibility = View.VISIBLE
            binding.lytFindPassword.visibility = View.GONE
            binding.tvFindUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            binding.tvFindPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        binding.tvFindPassword.setOnClickListener {
            forgot = "password"
            hideSoftInput()
            binding.etMailUsername.setText("")
            binding.lytFindUsername.visibility = View.GONE
            binding.lytFindPassword.visibility = View.VISIBLE
            binding.tvFindUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.tvFindPassword.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        }
        binding.btnFindUsername.setOnClickListener { findUsername() }
        binding.btnIssueTempPassword.setOnClickListener { issueTmpePassword() }
    }

    fun findUsername() {
        if (binding.etMailUsername.text.toString() != "") {
            val loopingDialog = looping()
            api.sendForgotUsername(binding.etMailUsername.text.toString()).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>,response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 204) {
                        binding.tvResultUsername.text = "입력하신 메일로 아이디가 전송되었습니다."
                    } else {
                        Log.e("FragFindAccount username", response.toString())
                        binding.tvResultUsername.text = ""
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragFindAccount username", t.message.toString())
                    binding.tvResultUsername.text = ""
                }
            })
        }
    }

    fun issueTmpePassword() {
        val username = binding.etUsername.text.toString()
        val mail = binding.etMailPassword.text.toString()
        if (isSendAble && username != "" && mail != "") {
            isSendAble = false
            val loopingDialog = looping()
            api.issueTempToken(ForgotPassword(username, mail)).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>,response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 204) {
                        binding.tvResultPassword.text = "입력하신 메일로 임시 비밀번호가 전송되었습니다."
                    } else {
                        Log.e("FragFindAccount password", response.toString())
                        binding.tvResultPassword.text = "다시 시도해주세요"
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragFindAccount username", t.message.toString())
                    binding.tvResultPassword.text = "다시 시도해주세요"
                }
            })
        }
    }

    fun hideSoftInput() {
        Log.d(TAG, "키보드 숨기기")
        Log.d(TAG, view.toString())
        val mActivity = activity as MainActivity
        return mActivity.hideSoftInput()
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
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