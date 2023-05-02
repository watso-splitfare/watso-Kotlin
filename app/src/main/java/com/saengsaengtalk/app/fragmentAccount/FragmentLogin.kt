package com.saengsaengtalk.app.fragmentAccount

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
import com.saengsaengtalk.app.API.DataModels.ErrorResponse
import com.saengsaengtalk.app.API.LoginModel
import com.saengsaengtalk.app.API.VoidResponse
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragLoginBinding
import com.saengsaengtalk.app.fragmentBaedal.Baedal.FragmentBaedal
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentLogin :Fragment() {

    private var mBinding: FragLoginBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoginBinding.inflate(inflater, container, false)

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
            val loopingDialog = looping()
            val prefs = MainActivity.prefs
            val reg = prefs.getString("registration", "")
            api.login(LoginModel(binding.etId.text.toString(), binding.etPw.text.toString(), reg)).enqueue(object: Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
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
                    } else if (response.code() == 401) {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        showAlert(errorResponse.msg)
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("login Fragment - login", t.message.toString())
                    makeToast("다시 시도해 주세요.")
                }
            })
        }

        binding.tvFindAccount.setOnClickListener {
            setFrag(FragmentFindAccount())
        }
        binding.tvSignUp.setOnClickListener { setFrag(FragmentSignUp()) }
    }

    fun showAlert(msg: String) {
        AlertDialog.Builder(requireContext()).setTitle("로그인 실패")
        .setMessage(msg)
        .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ -> })
        .show()
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
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