package com.example.saengsaengtalk.fragmentAccount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.APIS.DuplicationResult
import com.example.saengsaengtalk.APIS.SignUpModel
import com.example.saengsaengtalk.APIS.SignUpResult
import com.example.saengsaengtalk.LoopingDialog
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragSignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FragmentSignUp :Fragment() {

    private var mBinding: FragSignUpBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val signUpCheck = mutableMapOf("username" to false, "password" to false, "nickname" to false, "studentnum" to false)
    var checkedUsername: String? = null
    var checkedNickname: String? = null
    var checkedStudentnum: String? = null

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
                if (checkedUsername != null && binding.tvUsernameConfirm.text == checkedUsername) {
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
            api.usernameDuplicationCheck(binding.etUsername.text.toString()).enqueue(object : Callback<DuplicationResult> {
                override fun onResponse(call: Call<DuplicationResult>, response: Response<DuplicationResult>) {
                    Log.d("아이디 중복확인",response.toString())
                    Log.d("아이디 중복확인", response.body().toString())
                    if (response.code() == 200) {
                        if (response.body()!!.is_duplicated!!) {
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
                    looping(false, loopingDialog)
                }

                override fun onFailure(call: Call<DuplicationResult>, t: Throwable) {
                    Log.e("signUp Fragment - usernameDuplicationCheck", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                    looping(false, loopingDialog)
                }
            })

        }

        /** 비밀번호 확인 */
        binding.etPwConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(binding.etPw.text.toString().equals(binding.etPwConfirm.text.toString())) {
                    binding.tvPwConfirm.text = "비밀번호가 일치합니다."
                    //binding.tvPwConfirm.setTextColor()
                    signUpCheck["password"] = true
                } else {
                    binding.tvPwConfirm.text = "비밀번호가 일치하지 않습니다."
                    signUpCheck["password"] = false
                }
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        /** 닉네임 중복확인*/
        binding.etNickname.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedNickname != null && binding.tvNicknameConfirm.text == checkedNickname) {
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
            api.nicknameDuplicationCheck(binding.etNickname.text.toString()).enqueue(object : Callback<DuplicationResult> {
                override fun onResponse(call: Call<DuplicationResult>, response: Response<DuplicationResult>) {
                    Log.d("닉네임 중복확인",response.toString())
                    Log.d("닉네임 중복확인", response.body().toString())
                    if (response.code() == 200) {
                        if (response.body()!!.is_duplicated!!) {
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
                    looping(false, loopingDialog)
                }

                override fun onFailure(call: Call<DuplicationResult>, t: Throwable) {
                    Log.e("signUp Fragment - nicknameDuplicationCheck", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                    looping(false, loopingDialog)
                }
            })
        }

        /** 메일 관련 */
        //binding.lytMail.visibility = View.GONE
        /*val domains = resources.getStringArray(R.array.domains)

        binding.spnMailDomain.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.domains, android.R.layout.simple_spinner_item)
        binding.spnMailDomain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) binding.etMailDomain.setText(domains[position])
                else binding.etMailDomain.setText(null)
            }
        }*/

        /** 학번 */
        binding.etStudentnum.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedStudentnum != null && binding.tvStudentnumConfirm.text == checkedStudentnum) {
                    binding.tvStudentnumConfirm.text = "사용 가능한 학번입니다."
                    signUpCheck["studentnum"] = true
                } else {
                    binding.tvStudentnumConfirm.text = "학번 중복확인이 필요합니다."
                    signUpCheck["studentnum"] = false
                }
                setSignupBtnAble()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnStudentnumDuplicationCheck.setOnClickListener {
            val loopingDialog = looping()
            api.studentnumDuplicationCheck(binding.etStudentnum.text.toString()).enqueue(object : Callback<DuplicationResult> {
                override fun onResponse(call: Call<DuplicationResult>, response: Response<DuplicationResult>) {
                    Log.d("학번 중복확인",response.toString())
                    Log.d("학번 중복확인", response.body().toString())
                    if (response.code() == 200) {
                        if (response.body()!!.is_duplicated!!) {
                            signUpCheck["studentnum"] = false
                            binding.tvStudentnumConfirm.text = "사용 불가능한 학번입니다."
                        } else {
                            binding.tvStudentnumConfirm.text = "사용 가능한 학번입니다."
                            signUpCheck["studentnum"] = true
                            checkedStudentnum = binding.etStudentnum.text.toString()
                        }
                        setSignupBtnAble()
                    } else if (response.code() == 500) {
                        Log.e("signUp Fragment - studentnumDuplicationCheck", response.body()!!.message!!)
                    } else {
                        Log.e("signUp Fragment - studentnumDuplicationCheck", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                    looping(false, loopingDialog)
                }

                override fun onFailure(call: Call<DuplicationResult>, t: Throwable) {
                    Log.e("signUp Fragment - studentnumDuplicationCheck", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                    looping(false, loopingDialog)
                }
            })
        }

        /** 회원가입 */
        binding.btnSignup.setOnClickListener {
            val loopingDialog = looping()
            var data = SignUpModel(
                binding.etUsername.text.toString(),
                binding.etPw.text.toString(),
                binding.etNickname.text.toString(),
                binding.etStudentnum.text.toString(),
                "123 농협",
                binding.etMail.text.toString() + "@" + binding.etMailDomain.text.toString()
            )
            api.signup(data).enqueue(object : Callback<SignUpResult> {
                override fun onResponse(call: Call<SignUpResult>, response: Response<SignUpResult>) {
                    if (response.code() == 204) {
                        makeToast("회원가입에 성공하였습니다.")
                        onBackPressed()
                    } else {
                        Log.e("signUp Fragment - signup", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                    looping(false, loopingDialog)
                }

                override fun onFailure(call: Call<SignUpResult>, t: Throwable) {
                    Log.e("signUp Fragment - nicknameDuplicationCheck", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                    looping(false, loopingDialog)
                }
            })
        }
    }

    fun setSignupBtnAble() {
        if (signUpCheck["username"]!! && signUpCheck["password"]!! && signUpCheck["nickname"]!!) {
            binding.btnSignup.setEnabled(true)
            binding.btnSignup.setBackgroundResource(R.drawable.btn_taxi_propose)
        } else {
            binding.btnSignup.setEnabled(false)
            binding.btnSignup.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
        }
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