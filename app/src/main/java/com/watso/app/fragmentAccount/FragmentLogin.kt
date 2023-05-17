package com.watso.app.fragmentAccount

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.API.LoginModel
import com.watso.app.API.VoidResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragLoginBinding
import com.watso.app.fragmentBaedal.Baedal.FragmentBaedal
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FragmentLogin :Fragment() {
    val TAG = "FragLogin"
    lateinit var AC: ActivityController

    private var mBinding: FragLoginBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoginBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        /** 로그인 */
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (verifyInput("username", username) && verifyInput("password", password)) login()
        }

        binding.tvFindAccount.setOnClickListener { setFrag(FragmentFindAccount()) }
        binding.btnSignup.setOnClickListener { setFrag(FragmentSignUp()) }
    }

    fun login() {
        val prefs = MainActivity.prefs
        val reg = prefs.getString("registration", "")
        AC.showProgressBar()
        api.login(LoginModel(binding.etUsername.text.toString(), binding.etPassword.text.toString(), reg)).enqueue(object: Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code()==200) {
                    val tokens = response.headers().get("Authentication").toString().split("/")
                    val payload = decodeToken(tokens[0])
                    val dUserId = JSONObject(payload).getString("user_id")
                    val dNickname = JSONObject(payload).getString("nickname")

                    prefs.setString("accessToken", tokens[0])
                    prefs.setString("refreshToken", tokens[1])
                    prefs.setString("userId", dUserId)
                    prefs.setString("nickname", dNickname)

                    setFrag(FragmentBaedal(), popBackStack=0, fragIndex=1)
                } else  {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        showAlert(errorResponse.msg)
                        Log.d("$TAG[login]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) { Log.e("$TAG[login]", e.toString())}
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("login Fragment - login", t.message.toString())
                makeToast("다시 시도해 주세요.")
            }
        })
    }

    fun showAlert(msg: String) {
        AlertDialog.Builder(requireContext()).setTitle("로그인 실패")
        .setMessage(msg)
        .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ -> })
        .show()
    }

    fun verifyInput(case: String, text: String): Boolean {
        val builder = AlertDialog.Builder(requireContext())
        if (verifyInputFormat(case, text)) {
            return true
        } else {
            builder.setMessage("아이디 혹은 비밀번호가 일치하지 않습니다")
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

    fun decodeToken(jwt: String): String {
        val mActivity = activity as MainActivity
        return mActivity.decodeToken(jwt)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int=-1, fragIndex:Int=0) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, popBackStack, fragIndex)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}