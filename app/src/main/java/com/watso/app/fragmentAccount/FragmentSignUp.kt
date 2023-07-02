package com.watso.app.fragmentAccount

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
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
import com.watso.app.ActivityController
import kotlinx.coroutines.*
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragSignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FragmentSignUp :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context
    lateinit var job: Job

    var mBinding: FragSignUpBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragSignUp"
    val api= API.create()

    val signUpCheck = mutableMapOf(
        "realName" to false,    // verifyInputFormat
        "username" to false,    // 중복확인할 때 유효성 검사 함
        "password" to false,    // verifyInputFormat, 비밀번호 확인
        "nickname" to false,    // 중복확인할 때 유효성 검사 함
        "accountNum" to false,  // verifyInputFormat
        "email" to false        // 코드확인할 때 유효성 검사 함
    )
    var remainingSeconds = 0
    var valifyTime = 300
    var sendCoolTime = 10
    var isSendAble = true
    var checkedUsername: String? = null
    var checkedNickname: String? = null
    var verifingEmail = ""
    var verifiedEmail = ""
    var authToken = ""
    var bankName = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragSignUpBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        if (::job.isInitialized && job.isActive)
            job.cancel()
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }
        binding.btnSignup.setEnabled(false)
        bindNickname()
        bindUsername()
        bindPassword()
        bindAccountNum()
        bindEmail()
        bindSignUp()
    }

    fun bindNickname() {
        binding.etNickname.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedNickname != null && binding.etNickname.text.toString() == checkedNickname) {
                    binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                    binding.tvNicknameConfirm.setTextColor(Color.BLACK)
                    signUpCheck["nickname"] = true
                } else {
                    binding.tvNicknameConfirm.text = "닉네임 중복확인이 필요합니다."
                    binding.tvNicknameConfirm.setTextColor(Color.RED)
                    signUpCheck["nickname"] = false
                }
                if (binding.etNickname.text.toString() == "") binding.tvNicknameConfirm.text = ""
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnNicknameDuplicationCheck.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            if (verifyInput("nickname", nickname)) {
                AC.showProgressBar()
                api.checkDuplication("nickname", nickname).enqueue(object : Callback<DuplicationCheckResult> {
                    override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                        AC.hideProgressBar()
                        Log.d("닉네임 중복확인", response.toString())
                        Log.d("닉네임 중복확인", response.body().toString())
                        if (response.code() == 200) {
                            if (response.body()!!.isDuplicated) {
                                signUpCheck["nickname"] = false
                                binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
                                binding.tvNicknameConfirm.setTextColor(Color.RED)
                            } else {
                                binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                                binding.tvNicknameConfirm.setTextColor(Color.BLACK)
                                signUpCheck["nickname"] = true
                                checkedNickname = nickname
                            }
                            setSignupBtnAble()
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                AC.makeToast(errorResponse.msg)
                                Log.d("$TAG[nicknameVerify]", "${errorResponse.code}: ${errorResponse.msg}")
                            } catch (e:Exception) {
                                Log.e("$TAG[nicknameVerify]", e.toString())
                                Log.d("$TAG[nicknameVerify]", response.errorBody()?.string().toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                        AC.hideProgressBar()
                        Log.e(
                            "signUp Fragment - nicknameDuplicationCheck",
                            t.message.toString()
                        )
                        AC.makeToast("다시 시도해 주세요.")
                    }
                })
            } else {
                signUpCheck["nickname"] = false
                binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
                binding.tvNicknameConfirm.setTextColor(Color.RED)
            }
        }
    }

    fun bindUsername() {
        binding.etUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedUsername != null && binding.etUsername.text.toString() == checkedUsername) {
                    binding.tvUsernameConfirm.text = "사용가능한 아이디입니다."
                    binding.tvUsernameConfirm.setTextColor(Color.BLACK)
                    signUpCheck["username"] = true
                } else {
                    binding.tvUsernameConfirm.text = "아이디 중복확인이 필요합니다."
                    binding.tvUsernameConfirm.setTextColor(Color.RED)
                    signUpCheck["username"] = false
                }
                if (binding.etUsername.text.toString() == "") {
                    binding.tvUsernameConfirm.text = ""
                    binding.tvUsernameConfirm.setTextColor(Color.BLACK)
                    signUpCheck["username"] = false
                }
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        binding.btnUsernameDuplicationCheck.setOnClickListener {
            val username = binding.etUsername.text.toString()
            if (verifyInput("username", username)) {
                AC.showProgressBar()
                api.checkDuplication("username", username).enqueue(object : Callback<DuplicationCheckResult> {
                    override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                        AC.hideProgressBar()
                        Log.d("아이디 중복확인", response.toString())
                        Log.d("아이디 중복확인", response.body().toString())
                        if (response.code() == 200) {
                            if (response.body()!!.isDuplicated) {
                                signUpCheck["username"] = false
                                binding.tvUsernameConfirm.text = "사용 불가능한 아이디입니다."
                                binding.tvUsernameConfirm.setTextColor(Color.RED)
                            } else {
                                binding.tvUsernameConfirm.text = "사용 가능한 아이디입니다."
                                binding.tvUsernameConfirm.setTextColor(Color.BLACK)
                                signUpCheck["username"] = true
                                checkedUsername = username
                            }
                            setSignupBtnAble()
                        } else  {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                AC.makeToast(errorResponse.msg)
                                Log.d("$TAG[usernameVerify]", "${errorResponse.code}: ${errorResponse.msg}")
                            } catch (e:Exception) {
                                Log.e("$TAG[usernameVerify]", e.toString())
                                Log.d("$TAG[usernameVerify]", response.errorBody()?.string().toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                        AC.hideProgressBar()
                        Log.e(
                            "signUp Fragment - usernameDuplicationCheck",
                            t.message.toString()
                        )
                        AC.makeToast("다시 시도해 주세요.")
                    }
                })
            } else {
                signUpCheck["username"] = false
                binding.tvUsernameConfirm.text = "사용 불가능한 아이디입니다."
                binding.tvUsernameConfirm.setTextColor(Color.RED)
            }
        }
    }

    fun bindPassword() {
        binding.etPasswordConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { compairPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { compairPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    fun bindAccountNum() {
        binding.btnAccountNumDuplicationCheck.visibility = View.GONE
        binding.tvAccountNumConfirm.visibility = View.GONE
        val banks = resources.getStringArray(R.array.banks)
        bankName = banks[0]

        binding.spnAccountNum.adapter = ArrayAdapter.createFromResource(
            fragmentContext, R.array.banks, android.R.layout.simple_spinner_item)
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
        }}

    fun bindEmail() {
        binding.etEmail.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                signUpCheck["email"] = (verifiedEmail != "" && binding.etEmail.text.toString() == verifiedEmail)
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.tvCoolTime.visibility = View.GONE
        binding.btnSendCode.setOnClickListener {
            val temp = binding.etEmail.text.toString()
            if (temp != "" && isSendAble) {
                if (verifyInput("email", "${temp}@pusan.ac.kr")) {
                    verifingEmail = temp
                    AC.showProgressBar()
                    api.sendVerificationCode("${verifingEmail}@pusan.ac.kr").enqueue(object : Callback<VoidResponse> {
                        override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                            AC.hideProgressBar()
                            if (response.code() == 204) {
                                if (::job.isInitialized && job.isActive)
                                    job.cancel()
                                job = GlobalScope.launch { countDown(valifyTime) }
                                binding.btnVerifyEmail.setBackgroundResource(R.drawable.solid_primary)
                                binding.tvVerifyEmail.setTextColor(Color.WHITE)
                            } else {
                                try {
                                    val errorBody = response.errorBody()?.string()
                                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                    AC.makeToast(errorResponse.msg)
                                    Log.d("$TAG[sendCode]", "${errorResponse.code}: ${errorResponse.msg}")
                                } catch (e:Exception) {
                                    Log.e("$TAG[sendCode]", e.toString())
                                    Log.d("$TAG[sendCode]", response.errorBody()?.string().toString())
                                }
                            }
                        }

                        override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                            AC.hideProgressBar()
                            Log.e("signUp Fragment - sendMail", t.message.toString())
                            AC.makeToast("다시 시도해 주세요.")
                        }
                    })
                }
            }
            else if (verifingEmail != "" && !isSendAble ) binding.tvCoolTime.visibility = View.VISIBLE

        }

        binding.btnVerifyEmail.setOnClickListener {
            Log.d("$TAG[메일 인증]", binding.etEmail.text.toString())
            if (verifingEmail != "" && binding.etVerifyCode.text.toString() != "") {
                AC.showProgressBar()
                api.checkVerificationCode("${verifingEmail}@pusan.ac.kr", binding.etVerifyCode.text.toString()).enqueue(object : Callback<VerificationResponse> {
                    override fun onResponse(call: Call<VerificationResponse>, response: Response<VerificationResponse>) {
                        AC.hideProgressBar()
                        if (response.code() == 200) {
                            remainingSeconds = 0
                            verifiedEmail = verifingEmail
                            signUpCheck["email"] = true
                            authToken = response.body()!!.authToken
                            setSignupBtnAble()
                            Log.d("signUp Fragment - signUpCheck", signUpCheck.toString())
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                AC.makeToast(errorResponse.msg)
                                Log.d("$TAG[verifyMail]", "${errorResponse.code}: ${errorResponse.msg}")
                            } catch (e: Exception) {
                                Log.e("$TAG[verifyMail]", e.toString())
                                Log.d("$TAG[verifyMail]", response.errorBody()?.string().toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
                        Log.d("$TAG[onFailure]", "")
                        AC.hideProgressBar()
                        Log.e("signUp Fragment - sendMail", t.message.toString())
                        AC.makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }
    }

    fun bindSignUp() {
        binding.btnSignup.setOnClickListener {
            if (verifySignup()) {
                var data = SignUpModel(
                    authToken,
                    binding.etRealName.text.toString(),
                    binding.etUsername.text.toString(),
                    binding.etPassword.text.toString(),
                    binding.etNickname.text.toString(),
                    binding.etAccountNum.text.toString(),
                    "${binding.etEmail.text.toString()}@pusan.ac.kr"
                )
                AC.showProgressBar()
                api.signup(data).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        AC.hideProgressBar()
                        if (response.code() == 201) {
                            AC.makeToast("회원가입에 성공하였습니다.")
                            AC.onBackPressed()
                        } else  {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                AC.makeToast(errorResponse.msg)
                                Log.d("$TAG[signup]", "${errorResponse.code}: ${errorResponse.msg}")
                            } catch (e:Exception) {
                                Log.e("$TAG[signup]", e.toString())
                                Log.d("$TAG[signup]", response.errorBody()?.string().toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        AC.hideProgressBar()
                        Log.e("signUp Fragment - signup", t.message.toString())
                        AC.makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }
    }

    fun compairPassword() {
        if (binding.etPassword.text.toString() != "" && binding.etPasswordConfirm.text.toString() != "") {
            if (binding.etPassword.text.toString().equals(binding.etPasswordConfirm.text.toString())) {
                binding.tvPasswordConfirm.text = "비밀번호가 일치합니다."
                binding.tvPasswordConfirm.setTextColor(Color.BLACK)
                signUpCheck["password"] = true
            } else {
                binding.tvPasswordConfirm.text = "비밀번호가 일치하지 않습니다."
                binding.tvPasswordConfirm.setTextColor(Color.RED)
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
            binding.btnSignup.setBackgroundResource(R.drawable.solid_primary)
            binding.tvSignup.setTextColor(Color.WHITE)
        } else {
            binding.btnSignup.setEnabled(false)
            binding.btnSignup.setBackgroundResource(R.drawable.stroked_lightgray_silver)
            binding.tvSignup.setTextColor(Color.BLACK)
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

    fun verifySignup(): Boolean {
        val builder = AlertDialog.Builder(fragmentContext)
        if (verifyInput("realName", binding.etRealName.text.toString())) {
            if (verifyInput("password", binding.etPassword.text.toString())) {
                if (verifyInput("accountNum", binding.etAccountNum.text.toString())) {
                    return true
                } else {
                    builder.setMessage("사용할 수 없는 계좌번호 형식입니다.")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                    builder.show()
                }
            } else {
                builder.setMessage("비밀번호는 숫자, 영문자, 특수문자(~!@#\$%^&)를 각각 하나이상 포함하여 8~16자여야 합니다.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                builder.show()
            }
        } else {
            builder.setMessage("사용할 수 없는 이름 형식입니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
        return false
    }

    fun verifyInput(case: String, text: String): Boolean {
        val message = AC.verifyInput(case, text)
        return if (message == "") {
            true
        } else {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setMessage(message)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id -> })
                .show()
            false
        }
    }
}