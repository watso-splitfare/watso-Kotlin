package com.watso.app.fragmentAccount

import android.app.AlertDialog
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
import com.watso.app.LoopingDialog
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragUpdateAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FragmentUpdateAccount :Fragment() {
    val TAG = "FragUpdateAccount"
    var taget = ""
    var checkedPassword = ""

    private var mBinding: FragUpdateAccountBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

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
            "password" -> setLytPassword()
            "nickname" -> setLytNickname()
            else -> setLytAccountNum()
        }
        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    fun setLytPassword() {
        binding.tvTitle.text = "비밀번호 변경"
        binding.lytAccountNum.visibility = View.GONE
        binding.lytNickname.visibility = View.GONE

        binding.etPasswordConfirm.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.etNewPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { onChangedPassword() }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnUpdatePassword.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString()
            if (currentPassword != "" && checkedPassword != "") {
                if (verifyInput("password", checkedPassword)) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("비밀번호 변경")
                        .setMessage("비밀번호를 변경하시겠습니까?")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            updatePassword(currentPassword) })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
                    builder.show()
                }
            }
        }
    }

    fun updatePassword(currentPassword: String) {
        val updatePassword = UpdatePassword(currentPassword, checkedPassword)
        val loopingDialog = looping()
        api.updatePassword(updatePassword).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                looping(false, loopingDialog)
                if (response.code() == 204) {
                    makeToast("비밀번호가 변경되었습니다.")
                    setFrag(FragmentAccount())
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[username check]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) { Log.e("$TAG[username check]", e.toString()) }
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("FragUpdateAccount password", t.message.toString())
                makeToast("다시 시도해 주세요.")
            }
        })
    }

    fun setLytNickname() {
        binding.tvTitle.text = "닉네임 변경"
        binding.lytAccountNum.visibility = View.GONE
        binding.lytPassword.visibility = View.GONE

        var checkedNickname:String? = null
        binding.etNickname.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (checkedNickname != null && binding.etNickname.text.toString() == checkedNickname) {
                    binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                    binding.tvNicknameConfirm.setTextColor(Color.BLACK)
                } else {
                    binding.tvNicknameConfirm.text = "닉네임 중복확인이 필요합니다."
                    binding.tvNicknameConfirm.setTextColor(Color.RED)
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.btnNicknameDuplicationCheck.setOnClickListener {
            if (verifyInput("nickname", binding.etNickname.text.toString())) {
                val loopingDialog = looping()
                api.checkDuplication("nickname", binding.etNickname.text.toString()).enqueue(object : Callback<DuplicationCheckResult> {
                    override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                        looping(false, loopingDialog)
                        if (response.code() == 200) {
                            if (response.body()!!.isDuplicated) {
                                binding.tvNicknameConfirm.text = "사용 불가능한 닉네임입니다."
                                binding.tvNicknameConfirm.setTextColor(Color.RED)
                            } else {
                                binding.tvNicknameConfirm.text = "사용 가능한 닉네임입니다."
                                binding.tvNicknameConfirm.setTextColor(Color.BLACK)
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
        }
        binding.btnUpdateNickname.setOnClickListener {
            if (checkedNickname != null && binding.etNickname.text.toString() == checkedNickname) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("닉네임 변경")
                    .setMessage("닉네임을 변경하시겠습니까?")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        updateNickname(checkedNickname!!) })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
                builder.show()
            }
        }
    }

    fun updateNickname(checkedNickname: String) {
        val loopingDialog = looping()
        api.updateNickname(UpdateNickname(checkedNickname)).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                looping(false, loopingDialog)
                if (response.code() == 204) {
                    makeToast("닉네임이 변경되었습니다.")
                    setFrag(FragmentAccount())
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[nickname check]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) { Log.e("$TAG[nickname check]", e.toString()) }
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("FragUpdateAccount nickname", t.message.toString())
                makeToast("다시 시도해 주세요.")
            }
        })
    }

    fun setLytAccountNum() {
        binding.tvTitle.text = "계좌 정보 변경"
        binding.lytNickname.visibility = View.GONE
        binding.lytPassword.visibility = View.GONE

        /*val banks = resources.getStringArray(R.array.banks)
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
        }*/

        binding.btnUpdateAccountNum.setOnClickListener {
            if (verifyInput("accountNum", binding.etAccountNum.text.toString())) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("계좌번호 변경")
                    .setMessage("계좌번호를 변경하시겠습니까?")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        updateAccountNum() })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
                builder.show()
            }
        }
    }

    fun updateAccountNum() {
        val accountNumber = UpdateAccountNumber(binding.etAccountNum.text.toString())
        val loopingDialog = looping()
        api.updateAccountNumber(accountNumber).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                looping(false, loopingDialog)
                if (response.code() == 204) {
                    makeToast("계좌 정보가 변경되었습니다.")
                    setFrag(FragmentAccount())
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[updateAccountNum]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) {
                        Log.e("$TAG[updateAccountNum]", e.toString())
                    }
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("FragUpdateAccount AccountNum", t.message.toString())
                makeToast("다시 시도해 주세요.")
            }
        })
    }

    fun onChangedPassword() {
        if (binding.etNewPassword.text.toString() != "" && binding.etPasswordConfirm.text.toString() != "") {
            if (binding.etNewPassword.text.toString().equals(binding.etPasswordConfirm.text.toString())) {
                binding.tvPasswordConfirm.text = "비밀번호가 일치합니다."
                binding.tvPasswordConfirm.setTextColor(Color.BLACK)
                checkedPassword = binding.etNewPassword.text.toString()
            } else {
                binding.tvPasswordConfirm.text = "비밀번호가 일치하지 않습니다."
                binding.tvPasswordConfirm.setTextColor(Color.RED)
                checkedPassword = ""
            }
        } else {
            binding.tvPasswordConfirm.text = ""
            checkedPassword = ""
        }
    }

    fun verifyInput(case: String, text: String): Boolean {
        val builder = AlertDialog.Builder(requireContext())
        if (verifyInputFormat(case, text)) {
            return true
        } else {
            val message = when(case) {
                "nickname" -> {"사용할 수 없는 닉네임 형식입니다."}
                "accountNum" -> {"사용할 수 없는 계좌번호 형식입니다."}
                else -> {"비밀번호는 숫자, 영문자, 특수문자(~!@#\$%^&)를 각각 하나이상 포함하여 8~16자여야 합니다."}
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