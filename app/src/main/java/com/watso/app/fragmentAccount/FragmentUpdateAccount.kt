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
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragUpdateAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FragmentUpdateAccount :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    var mBinding: FragUpdateAccountBinding? = null
    val binding get() = mBinding!!
    val TAG = "FragUpdateAccount"
    val api= API.create()

    var taget = ""
    var checkedPassword = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taget = it.getString("target")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragUpdateAccountBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }

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
                    val builder = AlertDialog.Builder(fragmentContext)
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
        AC.showProgressBar()
        api.updatePassword(updatePassword).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code() == 204) {
                    AC.makeToast("비밀번호가 변경되었습니다.")
                    AC.setFrag(FragmentAccount(), popBackStack = 2)
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        AC.makeToast(errorResponse.msg)
                        Log.d("$TAG[username check]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) { Log.e("$TAG[username check]", e.toString()) }
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("FragUpdateAccount password", t.message.toString())
                AC.makeToast("다시 시도해 주세요.")
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
                AC.showProgressBar()
                api.checkDuplication("nickname", binding.etNickname.text.toString()).enqueue(object : Callback<DuplicationCheckResult> {
                    override fun onResponse(call: Call<DuplicationCheckResult>, response: Response<DuplicationCheckResult>) {
                        AC.hideProgressBar()
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
                            AC.makeToast("다시 시도해 주세요.")
                        }
                    }

                    override fun onFailure(call: Call<DuplicationCheckResult>, t: Throwable) {
                        AC.hideProgressBar()
                        Log.e("FragUpdateAccount nicknameCheck", t.message.toString())
                        AC.makeToast("다시 시도해 주세요.")
                    }
                })
            }
        }
        binding.btnUpdateNickname.setOnClickListener {
            if (checkedNickname != null && binding.etNickname.text.toString() == checkedNickname) {
                val builder = AlertDialog.Builder(fragmentContext)
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
        AC.showProgressBar()
        api.updateNickname(UpdateNickname(checkedNickname)).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code() == 204) {
                    AC.makeToast("닉네임이 변경되었습니다.")
                    refreshToken()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        AC.makeToast(errorResponse.msg)
                        Log.d("$TAG[nickname check]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) { Log.e("$TAG[nickname check]", e.toString()) }
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("$TAG[nickname check]", t.message.toString())
                AC.makeToast("다시 시도해 주세요.")
            }
        })
    }
    fun refreshToken() {
        AC.showProgressBar()
        api.refreshToken().enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                AC.setFrag(FragmentAccount(), popBackStack = 2)
                if (response.code() == 200)
                    Log.d("$TAG[refreshToken]토큰 재발급 완료", "")
                else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        Log.d("$TAG[refreshToken]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) {
                        Log.e("$TAG[refreshToken]", e.toString())
                        Log.d("$TAG[refreshToken]", response.errorBody()?.string().toString())
                    }
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("$TAG[refreshToken]", t.message.toString())
                AC.makeToast("다시 시도해 주세요.")
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
        }*/

        binding.btnUpdateAccountNum.setOnClickListener {
            if (verifyInput("accountNum", binding.etAccountNum.text.toString())) {
                val builder = AlertDialog.Builder(fragmentContext)
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
        AC.showProgressBar()
        api.updateAccountNumber(accountNumber).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code() == 204) {
                    AC.makeToast("계좌 정보가 변경되었습니다.")
                    AC.setFrag(FragmentAccount(), popBackStack = 2)
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        AC.makeToast(errorResponse.msg)
                        Log.d("$TAG[updateAccountNum]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) {
                        Log.e("$TAG[updateAccountNum]", e.toString())
                    }
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("FragUpdateAccount AccountNum", t.message.toString())
                AC.makeToast("다시 시도해 주세요.")
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