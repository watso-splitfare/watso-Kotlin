package com.saengsaengtalk.app.fragmentAccount

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
import com.saengsaengtalk.app.APIS.*
import kotlinx.coroutines.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragSignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FragmentSignUp :Fragment() {
    var remainingSeconds = 0

    private var mBinding: FragSignUpBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val signUpCheck = mutableMapOf("username" to false, "password" to false, "nickname" to false, "email" to false)
    var checkedUsername: String? = null
    var checkedNickname: String? = null
    var verifingEmail: String? = null
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
            val loopingDialog = looping()
            api.checkDuplication("username", binding.etUsername.text.toString()).enqueue(object : Callback<DuplicationCheckResult> {
                override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                    looping(false, loopingDialog)
                    Log.d("아이디 중복확인",response.toString())
                    Log.d("아이디 중복확인", response.body().toString())
                    if (response.code() == 200) {
                        if (response.body()!!.isDuplicated) {
                            signUpCheck["username"] = false
                            binding.tvUsernameConfirm.text = "사용 불가능한 아이디입니다."
                        } else {
                            binding.tvUsernameConfirm.text = "사용 가능한 아이디입니다."
                            signUpCheck["username"] = true
                            checkedUsername = binding.etUsername.text.toString()
                        }
                        setSignupBtnAble()
                    } else {
                        Log.e("signUp Fragment - usernameDuplicationCheck", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                }

                override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("signUp Fragment - usernameDuplicationCheck", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                }
            })

        }

        /** 비밀번호 확인 */
        binding.etPwConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPW() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etPw.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPW() }
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
            val loopingDialog = looping()
            api.checkDuplication("nickname", binding.etNickname.text.toString()).enqueue(object : Callback<DuplicationCheckResult> {
                override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                    looping(false, loopingDialog)
                    Log.d("닉네임 중복확인",response.toString())
                    Log.d("닉네임 중복확인", response.body().toString())
                    if (response.code() == 200) {
                        if (response.body()!!.isDuplicated) {
                            signUpCheck["nickname"] = false
                            binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
                        } else {
                            binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                            signUpCheck["nickname"] = true
                            checkedNickname = binding.etNickname.text.toString()
                        }
                        setSignupBtnAble()
                    } else {
                        Log.e("signUp Fragment - nicknameDuplicationCheck", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                }

                override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("signUp Fragment - nicknameDuplicationCheck", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                }
            })
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
                signUpCheck["email"] = verifingEmail != null && binding.etEmail.text.toString() == verifingEmail!!
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnSendCode.setOnClickListener {
            verifingEmail = binding.etEmail.text.toString()
            if (verifingEmail != null) {

                val loopingDialog = looping()
                api.sendVerificationCode(verifingEmail!!).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            GlobalScope.launch { countDown(300) }
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
        }

        binding.btnVerifyEmail.setOnClickListener {
            if (binding.etVerifyCode.text.toString() != "") {
                val loopingDialog = looping()
                api.checkVerificationCode(verifingEmail!!, binding.etVerifyCode.text.toString()).enqueue(object : Callback<VerificationResponse> {
                    override fun onResponse(call: Call<VerificationResponse>, response: Response<VerificationResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 200) {
                            remainingSeconds = 0
                            signUpCheck["email"] = true
                            authToken = response.body()!!.authToken
                            setSignupBtnAble()
                            Log.d("signUp Fragment - signUpCheck", signUpCheck.toString())
                        } else {
                            Log.e("signUp Fragment - sendMail", response.toString())
                            makeToast("다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
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
                binding.etPw.text.toString(),
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

    fun onChangedPW() {
        if (binding.etPw.text.toString() != "" && binding.etPwConfirm.text.toString() != "") {
            if (binding.etPw.text.toString().equals(binding.etPwConfirm.text.toString())) {
                binding.tvPwConfirm.text = "비밀번호가 일치합니다."
                //binding.tvPwConfirm.setTextColor()
                signUpCheck["password"] = true
            } else {
                binding.tvPwConfirm.text = "비밀번호가 일치하지 않습니다."
                signUpCheck["password"] = false
            }
        } else {
            binding.tvPwConfirm.text = ""
            signUpCheck["password"] = false
        }
        setSignupBtnAble()
    }

    fun setSignupBtnAble() {
        if (signUpCheck["username"]!! && signUpCheck["password"]!! && signUpCheck["nickname"]!! && signUpCheck["email"]!!) {
            binding.btnSignup.setEnabled(true)
            binding.btnSignup.setBackgroundResource(R.drawable.btn_taxi_propose)
        } else {
            binding.btnSignup.setEnabled(false)
            binding.btnSignup.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
        }
    }

    suspend fun countDown(seconds: Int) {
        remainingSeconds = seconds

        while (remainingSeconds >= 0) {
            withContext(Dispatchers.Main) {
                binding.tvVerifyCountdown.text = countDownStr(remainingSeconds)
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