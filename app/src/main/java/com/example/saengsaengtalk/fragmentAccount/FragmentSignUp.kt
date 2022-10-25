package com.example.saengsaengtalk.fragmentAccount

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
import com.example.saengsaengtalk.APIS.OverlapResult
import com.example.saengsaengtalk.APIS.SignUpModel
import com.example.saengsaengtalk.APIS.SignUpResult
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
    var signUpCheck = mutableMapOf("username" to false, "password" to false, "nickname" to false)

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


        /** 아이디 중복확인*/
        binding.btnUsernameOverlapCheck.setOnClickListener {

            api.usernameOverlapCheck(binding.etId.text.toString()).enqueue(object : Callback<OverlapResult> {
                override fun onResponse(call: Call<OverlapResult>, response: Response<OverlapResult>) {
                    Log.d("아이디 중복확인",response.toString())
                    Log.d("아이디 중복확인", response.body().toString())
                    /*if(!response.body().toString().isEmpty())
                        binding.tvTest.setText(response.body().toString());*/
                }

                override fun onFailure(call: Call<OverlapResult>, t: Throwable) {
                    // 실패
                    Log.d("아이디 중복확인",t.message.toString())
                    Log.d("아이디 중복확인","fail")
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
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(binding.etPw.text.toString().equals(binding.etPwConfirm.text.toString())) {
                    binding.tvPwConfirm.text = "비밀번호가 일치합니다."
                    signUpCheck["password"] = true
                } else {
                    binding.tvPwConfirm.text = "비밀번호가 일치하지 않습니다."
                    signUpCheck["password"] = false
                }
            }
        })

        /** 닉네임 중복확인*/
        binding.btnNicknameOverlapCheck.setOnClickListener {

            api.nicknameOverlapCheck(binding.etNickname.text.toString()).enqueue(object : Callback<OverlapResult> {
                override fun onResponse(call: Call<OverlapResult>, response: Response<OverlapResult>) {
                    Log.d("닉네임 중복확인",response.toString())
                    Log.d("닉네임 중복확인", response.body().toString())
                    /*if(!response.body().toString().isEmpty())
                        binding.tvTest.setText(response.body().toString());*/
                }

                override fun onFailure(call: Call<OverlapResult>, t: Throwable) {
                    // 실패
                    Log.d("닉네임 중복확인",t.message.toString())
                    Log.d("닉네임 중복확인","fail")
                }
            })
        }

        /** 메일 관련 */
        binding.lytMail.visibility = View.GONE
        val domains = resources.getStringArray(R.array.domains)

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
        }

        /** 회원가입 */
        binding.btnNext.setOnClickListener {
            var data = SignUpModel(
                binding.etId.text.toString(),
                binding.etPw.text.toString(),
                Integer.parseInt(binding.etMail.text.toString()),   //입력값 꼭 있어야함
                binding.etNickname.text.toString()
            )
            api.signup(data).enqueue(object : Callback<SignUpResult> {
                override fun onResponse(call: Call<SignUpResult>, response: Response<SignUpResult>) {
                    Log.d("회원가입",response.toString())
                    Log.d("회원가입", response.body().toString())
                    onBackPressed()
                    /*if(!response.body().toString().isEmpty())
                        binding.tvTest.setText(response.body().toString());*/
                }

                override fun onFailure(call: Call<SignUpResult>, t: Throwable) {
                    // 실패
                    Log.d("회원가입",t.message.toString())
                    Log.d("회원가입","fail")
                    onBackPressed()
                }
            })
        }

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