package com.watso.app.fragmentAccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.watso.app.API.TempAuthCode
import com.watso.app.API.VoidResponse
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragFindAccountBinding
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentFindAccount :Fragment() {
    var forgot = "id"
    var remainingSeconds = 0
    var valifyTime = 300
    var sendCoolTime = 10
    var isSendAble = true
    lateinit var job: Job

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
        if (::job.isInitialized && job.isActive)
            job.cancel()
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.lytFindPw.visibility = View.GONE

        binding.tvFindId.setOnClickListener {
            forgot = "id"
            binding.lytFindUsername.visibility = View.VISIBLE
            binding.lytFindPw.visibility = View.GONE
            binding.tvFindId.setTextColor(ContextCompat.getColor(requireContext(), R.color.kara))
            binding.tvFindPw.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.etInputMailUsername.setText("")
        }
        binding.tvFindPw.setOnClickListener {
            forgot = "pw"
            binding.lytFindUsername.visibility = View.GONE
            binding.lytFindPw.visibility = View.VISIBLE
            binding.tvFindId.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.tvFindPw.setTextColor(ContextCompat.getColor(requireContext(), R.color.kara))
            binding.etInputMailPw.setText("")
            binding.etVerifyCode.setText("")
        }
        binding.btnFindUsername.setOnClickListener { findUsername() }
        binding.tvCoolTime.visibility = View.GONE
        binding.btnSendCode.setOnClickListener { findPw() }
        binding.btnVerifyCode.setOnClickListener { verifyCode() }
    }

    fun findUsername() {
        if (binding.etInputMailUsername.text.toString() != "") {
            val loopingDialog = looping()
            api.sendForgotUsername(binding.etInputMailUsername.text.toString()).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>,response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 204) {
                        binding.tvResult.text = "입력하신 메일로 아이디가 전송되었습니다."
                    } else {
                        Log.e("FragFindAccount username", response.toString())
                        binding.tvResult.text = ""
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragFindAccount username", t.message.toString())
                    binding.tvResult.text = ""
                }
            })
        }
    }

    fun findPw() {
        if (binding.etInputMailPw.text.toString() != "") {
            if (isSendAble) {
                val loopingDialog = looping()
                api.sendForgotPasswordToken(binding.etInputMailPw.text.toString()).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            binding.tvResult.text = "입력하신 메일로 인증코드가 전송되었습니다."
                            job = GlobalScope.launch { countDown(valifyTime) }
                        } else {
                            Log.e("FragFindAccount pw", response.toString())
                            binding.tvResult.text = ""
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e("FragFindAccount pw", t.message.toString())
                        binding.tvResult.text = ""
                    }
                })
            } else binding.tvCoolTime.visibility = View.VISIBLE
        }
    }

    fun verifyCode() {
        if (binding.etVerifyCode.text.toString().length == 4) {
            val tempAuthCode = TempAuthCode(
                binding.etVerifyCode.text.toString(),
                binding.etInputMailPw.text.toString()
            )
            val loopingDialog = looping()
            api.issueTempToken(tempAuthCode).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 200) {
                        remainingSeconds = -1
                        val tempAccessToken = response.headers().get("Authentication")
                        Log.d("FragFindAccount 헤더", response.headers().toString())
                        Log.d("FragFindAccount 임시토큰", tempAccessToken!!)
                        MainActivity.prefs.setString("accessToken", tempAccessToken!!)
                        setFrag(FragmentUpdateAccount(), mapOf("target" to "pw"))
                    } else {
                        Log.d("response.toString", response.toString())
                        Log.d("response.body.toString", response.body().toString())

                        Log.d("response.message()", response.message())
                        Log.d("response.headers()", response.headers().toString())
                        Log.d("response.errorBody()", response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragFindAccount pw", t.message.toString())
                }
            })
        }
    }

    suspend fun countDown(seconds: Int) {
        remainingSeconds = seconds
        isSendAble = false
        var remaingCoolTime = sendCoolTime

        while (remainingSeconds > 0) {
            withContext(Dispatchers.Main) {
                binding.tvVerifyCountdown.text = countDownStr(remainingSeconds)
                if (remaingCoolTime > 0)
                    binding.tvCoolTime.text = "${remaingCoolTime}초 후에 재전송 가능합니다."
                else {
                    binding.tvCoolTime.visibility = View.GONE
                    isSendAble = true
                }
            }
            delay(1000)

            remainingSeconds--
            remaingCoolTime--
        }

        withContext(Dispatchers.Main) {
            when (remainingSeconds) {
                0 -> binding.tvVerifyCountdown.text = "만료되었습니다."
                -1 -> binding.tvVerifyCountdown.text = "인증되었습니다."
                else -> binding.tvVerifyCountdown.text = ""
            }
        }
    }

    fun countDownStr(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
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