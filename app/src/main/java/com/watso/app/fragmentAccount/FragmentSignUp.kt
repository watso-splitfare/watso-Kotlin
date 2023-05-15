package com.watso.app.fragmentAccount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.API.DataModels.ErrorResponse
import kotlinx.coroutines.*
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragSignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class FragmentSignUp :Fragment() {
    val TAG = "FragSignUp"
    var remainingSeconds = 0
    var valifyTime = 300
    var sendCoolTime = 10
    var isSendAble = true
    lateinit var job: Job

    private var mBinding: FragSignUpBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()
    val signUpCheck = mutableMapOf("username" to false, "password" to false, "nickname" to false, "email" to false)
    var checkedUsername: String? = null
    var checkedNickname: String? = null
    var verifingEmail = ""
    var authToken = ""
    var bankName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragSignUpBinding.inflate(inflater, container, false)
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
        binding.btnSignup.setEnabled(false)

        /** 아이디 중복확인*/
        binding.etUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedUsername != null && binding.etUsername.text.toString() == checkedUsername) {
                    binding.tvUsernameConfirm.text = "사용가능한 아이디입니다."
                    signUpCheck["username"] = true
                } else {
                    binding.tvUsernameConfirm.text = "아이디 중복확인이 필요합니다."
                    signUpCheck["username"] = false
                }
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        binding.btnUsernameDuplicationCheck.setOnClickListener {
            val username = binding.etUsername.text.toString()
            if (username != "" && !username.contains(" ")) {
                val loopingDialog = looping()
                api.checkDuplication("username", username).enqueue(object : Callback<DuplicationCheckResult> {
                    override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                        looping(false, loopingDialog)
                        Log.d("아이디 중복확인", response.toString())
                        Log.d("아이디 중복확인", response.body().toString())
                        if (response.code() == 200) {
                            if (response.body()!!.isDuplicated) {
                                signUpCheck["username"] = false
                                binding.tvUsernameConfirm.text = "사용 불가능한 아이디입니다."
                            } else {
                                binding.tvUsernameConfirm.text = "사용 가능한 아이디입니다."
                                signUpCheck["username"] = true
                                checkedUsername = username
                            }
                            setSignupBtnAble()
                        } else {
                            Log.e(
                                "signUp Fragment - usernameDuplicationCheck",
                                response.toString()
                            )
                            makeToast("다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e(
                            "signUp Fragment - usernameDuplicationCheck",
                            t.message.toString()
                        )
                        makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }

        /** 비밀번호 확인 */
        binding.etPasswordConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        /** 닉네임 중복확인*/
        binding.etNickname.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedNickname != null && binding.etNickname.text.toString() == checkedNickname) {
                    binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                    signUpCheck["nickname"] = true
                } else {
                    binding.tvNicknameConfirm.text = "닉네임 중복확인이 필요합니다."
                    signUpCheck["nickname"] = false
                }
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnNicknameDuplicationCheck.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            if (nickname != "" && !nickname.contains(" ")) {
                val loopingDialog = looping()
                api.checkDuplication("nickname", nickname).enqueue(object : Callback<DuplicationCheckResult> {
                    override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                        looping(false, loopingDialog)
                        Log.d("닉네임 중복확인", response.toString())
                        Log.d("닉네임 중복확인", response.body().toString())
                        if (response.code() == 200) {
                            if (response.body()!!.isDuplicated) {
                                signUpCheck["nickname"] = false
                                binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
                            } else {
                                binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                                signUpCheck["nickname"] = true
                                checkedNickname = nickname
                            }
                            setSignupBtnAble()
                        } else {
                            Log.e(
                                "signUp Fragment - nicknameDuplicationCheck",
                                response.toString()
                            )
                            makeToast("다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e(
                            "signUp Fragment - nicknameDuplicationCheck",
                            t.message.toString()
                        )
                        makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }

        /** 계좌 */
        binding.btnAccountNumDuplicationCheck.visibility = View.GONE
        binding.tvAccountNumConfirm.visibility = View.GONE
        val banks = resources.getStringArray(R.array.banks)
        bankName = banks[0]

        binding.spnAccountNum.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.banks, android.R.layout.simple_spinner_item)
        binding.spnAccountNum.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                bankName = (banks[position])
            }
        }

        /** 메일 */
        //binding.btnSendMail.isEnabled = false
        //binding.btnVerifyMail.isEnabled = false

        binding.etEmail.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                signUpCheck["email"] = verifingEmail != "" && binding.etEmail.text.toString() == verifingEmail
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.tvCoolTime.visibility = View.GONE
        binding.btnSendCode.setOnClickListener {
            verifingEmail = binding.etEmail.text.toString()
            if (verifingEmail != "" && isSendAble) {

                val loopingDialog = looping()
                api.sendVerificationCode(verifingEmail).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            if (::job.isInitialized && job.isActive)
                                job.cancel()
                            job = GlobalScope.launch { countDown(valifyTime) }
                        } else {
                            Log.e("signUp Fragment - sendMail", response.toString())
                            makeToast("다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e("signUp Fragment - sendMail", t.message.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                })
            }
            else if (verifingEmail != "" && !isSendAble ) binding.tvCoolTime.visibility = View.VISIBLE

        }

        binding.btnVerifyEmail.setOnClickListener {
            Log.d("$TAG[메일 인증]", binding.etEmail.text.toString())
            if (verifingEmail != "" && binding.etVerifyCode.text.toString() != "") {
                val loopingDialog = looping()
                api.checkVerificationCode("a", binding.etVerifyCode.text.toString()).enqueue(object : Callback<VerificationResponse> {
                    override fun onResponse(call: Call<VerificationResponse>, response: Response<VerificationResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 200) {
                            remainingSeconds = 0
                            signUpCheck["email"] = true
                            authToken = response.body()!!.authToken
                            setSignupBtnAble()
                            Log.d("signUp Fragment - signUpCheck", signUpCheck.toString())
                        } else {
                            Log.d("$TAG[onResponse]", "")
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                makeToast(errorResponse.msg)
                                Log.d("$TAG[verifyMail]", errorResponse.msg)
                            } catch (e: Exception) {
                                Log.e("$TAG[verifyMail]", e.toString())
                                Log.d("$TAG[verifyMail]", response.errorBody()?.string().toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
                        Log.d("$TAG[onFailure]", "")
                        looping(false, loopingDialog)
                        Log.e("signUp Fragment - sendMail", t.message.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }

        /** 회원가입 */
        binding.btnSignup.setOnClickListener {
            val loopingDialog = looping()
            var data = SignUpModel(
                authToken,
                binding.etRealName.text.toString(),
                binding.etUsername.text.toString(),
                binding.etPassword.text.toString(),
                binding.etNickname.text.toString(),
                binding.etAccountNum.text.toString() + " "+ bankName,
                binding.etEmail.text.toString()// + "@pusan.ac.kr"
            )
            api.signup(data).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 201) {
                        makeToast("회원가입에 성공하였습니다.")
                        onBackPressed()
                    } else {
                        Log.e("signUp Fragment - signup", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("signUp Fragment - signup", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                }
            })
        }
    }

    fun onChangedPassword() {
        if (binding.etPassword.text.toString() != "" && binding.etPasswordConfirm.text.toString() != "") {
            if (binding.etPassword.text.toString().equals(binding.etPasswordConfirm.text.toString())) {
                binding.tvPasswordConfirm.text = "비밀번호가 일치합니다."
                //binding.tvPasswordConfirm.setTextColor()
                signUpCheck["password"] = true
            } else {
                binding.tvPasswordConfirm.text = "비밀번호가 일치하지 않습니다."
                signUpCheck["password"] = false
            }
        } else {
            binding.tvPasswordConfirm.text = ""
            signUpCheck["password"] = false
        }
        setSignupBtnAble()
    }

    fun setSignupBtnAble() {
        if (signUpCheck["username"]!! && signUpCheck["password"]!! && signUpCheck["nickname"]!! && signUpCheck["email"]!!) {
            binding.btnSignup.setEnabled(true)
            binding.btnSignup.setBackgroundResource(R.drawable.stroked_white_10_primary_2)
        } else {
            binding.btnSignup.setEnabled(false)
            binding.btnSignup.setBackgroundResource(R.drawable.solid_lightgray_10)
        }
    }

    suspend fun countDown(seconds: Int) {
        remainingSeconds = seconds
        isSendAble = false
        var remaingCoolTime = sendCoolTime

        while (remainingSeconds > 0) {
            withContext(Dispatchers.Main) {
                binding.tvVerifyCountdown.text = countDownStr(remainingSeconds)
                if (remaingCoolTime > 0) {
                    binding.tvCoolTime.text = "${remaingCoolTime}초 후에 재전송 가능합니다."
                    remaingCoolTime--
                } else {
                    binding.tvCoolTime.visibility = View.GONE
                    isSendAble = true
                }
            }
            delay(1000)

            remainingSeconds--
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