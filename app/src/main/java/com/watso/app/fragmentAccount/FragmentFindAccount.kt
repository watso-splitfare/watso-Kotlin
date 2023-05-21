package com.watso.app.fragmentAccount

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.API.ForgotPassword
import com.watso.app.API.VoidResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragFindAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FragmentFindAccount :Fragment() {
    val TAG = "FragFindAccount"
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    var forgot = "username"
    var isSendAble = true

    private var mBinding: FragFindAccountBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragFindAccountBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

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
            binding.etEmailPassword.setText("")
            binding.etUsername.setText("")
            binding.lytFindUsername.visibility = View.VISIBLE
            binding.lytFindPassword.visibility = View.GONE
            binding.tvFindUsername.setTextColor(ContextCompat.getColor(fragmentContext, R.color.primary))
            binding.tvFindPassword.setTextColor(ContextCompat.getColor(fragmentContext, R.color.black))
        }
        binding.tvFindPassword.setOnClickListener {
            forgot = "password"
            hideSoftInput()
            binding.etEmailUsername.setText("")
            binding.lytFindUsername.visibility = View.GONE
            binding.lytFindPassword.visibility = View.VISIBLE
            binding.tvFindUsername.setTextColor(ContextCompat.getColor(fragmentContext, R.color.black))
            binding.tvFindPassword.setTextColor(ContextCompat.getColor(fragmentContext, R.color.primary))
        }
        binding.btnFindUsername.setOnClickListener { findUsername() }
        binding.btnIssueTempPassword.setOnClickListener { issueTmpePassword() }
    }

    fun findUsername() {
        if (verifyInput("email", binding.etEmailUsername.text.toString())) {
            AC.showProgressBar()
            api.sendForgotUsername(binding.etEmailUsername.text.toString()).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>,response: Response<VoidResponse>) {
                    AC.hideProgressBar()
                    if (response.code() == 204) {
                        binding.tvResultUsername.text = "입력하신 메일로 아이디가 전송되었습니다."
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[sendForgotUsername]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) {
                            Log.e("$TAG[sendForgotUsername]", e.toString())
                            Log.d("$TAG[sendForgotUsername]", response.errorBody()?.string().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    AC.hideProgressBar()
                    Log.e("FragFindAccount username", t.message.toString())
                    binding.tvResultUsername.text = ""
                }
            })
        }
    }

    fun issueTmpePassword() {
        val username = binding.etUsername.text.toString()
        val email = binding.etEmailPassword.text.toString()
        if (isSendAble && verifyInput("username", username) && verifyInput("email", email)) {
            isSendAble = false
            AC.showProgressBar()
            api.issueTempPassword(ForgotPassword(username, email)).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>,response: Response<VoidResponse>) {
                    AC.hideProgressBar()
                    if (response.code() == 204) {
                        binding.tvResultPassword.text = "입력하신 메일로 임시 비밀번호가 전송되었습니다."
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[issueTempPassword]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e: Exception) {
                            Log.e("$TAG[issueTempPassword]", e.toString())
                            Log.d("$TAG[issueTempPassword]", response.errorBody()?.string().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    AC.hideProgressBar()
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

    fun verifyInput(case: String, text: String): Boolean {
        val builder = AlertDialog.Builder(fragmentContext)
        if (verifyInputFormat(case, text)) {
            return true
        } else {
            val message = when(case) {
                "username" -> {"사용할 수 없는 아이디 형식입니다."}
                else -> {"사용할 수 없는 메일 형식입니다."}
            }
            builder.setMessage(message)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                .show()
        }
        return false
    }

    fun verifyInputFormat(case: String, text: String): Boolean {
        return VerifyInputFormat().verifyInputFormat(case, text)
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