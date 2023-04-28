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
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragAccountBinding
import com.saengsaengtalk.app.databinding.FragUpdateAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentUpdateAccount :Fragment() {
    var taget = ""
    var checkedPW = ""

    private var mBinding: FragUpdateAccountBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taget = it.getString("target")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragUpdateAccountBinding.inflate(inflater, container, false)

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        when (taget) {
            "pw" -> setLytPw()
            "nickname" -> setLytNickname()
            else -> setLytAccountNum()
        }
        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    fun setLytPw() {
        binding.tvTitle.text = "비밀번호 변경"
        binding.lytAccountNum.visibility = View.GONE
        binding.lytNickname.visibility = View.GONE

        binding.etPwConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPW() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etNewPw.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPW() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnUpdatePw.setOnClickListener {
            if (checkedPW != "") {
                val updatePassword = UpdatePassword(binding.etCurrentPw.text.toString(), checkedPW)
                val loopingDialog = looping()
                api.updatePassword(updatePassword).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            if (response.code() == 204) {
                                makeToast("비밀번호가 변경되었습니다.")
                                setFrag(FragmentAccount())
                            } else {
                                Log.e("FragUpdateAccount password", response.toString())
                                makeToast("다시 시도해 주세요.")
                            }
                        } else {
                            Log.e("FragUpdateAccount password", response.toString())
                            makeToast("다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e("FragUpdateAccount password", t.message.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }
    }

    fun setLytNickname() {
        binding.tvTitle.text = "닉네임 변경"
        binding.lytAccountNum.visibility = View.GONE
        binding.lytPw.visibility = View.GONE

        var checkedNickname:String? = null
        binding.etNickname.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedNickname != null && binding.etNickname.text.toString() == checkedNickname) {
                    binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                } else {
                    binding.tvNicknameConfirm.text = "닉네임 중복확인이 필요합니다."
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnNicknameDuplicationCheck.setOnClickListener {
            val loopingDialog = looping()
            api.checkDuplication("nickname", binding.etNickname.text.toString()).enqueue(object : Callback<DuplicationCheckResult> {
                override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                    looping(false, loopingDialog)
                    if (response.code() == 200) {
                        if (response.body()!!.isDuplicated) {
                            binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
                        } else {
                            binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                            checkedNickname = binding.etNickname.text.toString()
                        }
                    } else {
                        Log.e("FragUpdateAccount nicknameCheck", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                }

                override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragUpdateAccount nicknameCheck", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                }
            })
        }
        binding.btnUpdateNickname.setOnClickListener {
            if (checkedNickname != null && binding.etNickname.text.toString() == checkedNickname) {
                val loopingDialog = looping()
                api.updateNickname(UpdateNickname(checkedNickname!!)).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            if (response.code() == 204) {
                                makeToast("닉네임이 변경되었습니다.")
                                setFrag(FragmentAccount())
                            } else {
                                Log.e("FragUpdateAccount nickname", response.toString())
                                makeToast("다시 시도해 주세요.")
                            }
                        } else {
                            Log.e("FragUpdateAccount nickname", response.toString())
                            makeToast("다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e("FragUpdateAccount nickname", t.message.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }
    }

    fun setLytAccountNum() {
        binding.tvTitle.text = "계좌 정보 변경"
        binding.lytNickname.visibility = View.GONE
        binding.lytPw.visibility = View.GONE

        val banks = resources.getStringArray(R.array.banks)
        var bankName = banks[0]

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

        binding.btnUpdateAccountNum.setOnClickListener {
            val accountNumber = UpdateAccountNumber(binding.etAccountNum.text.toString() + " " + bankName)
            val loopingDialog = looping()
            api.updateAccountNumber(accountNumber).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 204) {
                        makeToast("계좌 정보가 변경되었습니다.")
                        setFrag(FragmentAccount())
                    } else {
                        Log.e("FragUpdateAccount AccountNum", response.toString())
                        makeToast("다시 시도해 주세요.")
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("FragUpdateAccount AccountNum", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                }
            })
        }
    }


    fun onChangedPW() {
        if (binding.etNewPw.text.toString() != "" && binding.etPwConfirm.text.toString() != "") {
            if (binding.etNewPw.text.toString().equals(binding.etPwConfirm.text.toString())) {
                binding.tvPwConfirm.text = "비밀번호가 일치합니다."
                //binding.tvPwConfirm.setTextColor()
                checkedPW = binding.etNewPw.text.toString()
            } else {
                binding.tvPwConfirm.text = "비밀번호가 일치하지 않습니다."
                checkedPW = ""
            }
        } else {
            binding.tvPwConfirm.text = ""
            checkedPW = ""
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

    fun setFrag(fragment: Fragment) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, popBackStack=2)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}