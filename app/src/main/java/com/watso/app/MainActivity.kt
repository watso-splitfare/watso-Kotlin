package com.watso.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.API.FcmToken
import com.watso.app.API.UserInfo
import com.watso.app.API.VoidResponse
import com.watso.app.databinding.ActivityMainBinding
import com.watso.app.fragmentAccount.FragmentLogin
import com.watso.app.fragmentBaedal.Baedal.FragmentBaedal
import com.watso.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    var mBinding: ActivityMainBinding? = null
    val binding get() = mBinding!!
    val TAG = "MainActivity"
    val api = API.create()

    companion object { lateinit var prefs: PreferenceUtil }

    var mBackWait:Long = 0
    var bottomBarIndex:Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferenceUtil(applicationContext)

        /** FCM설정, Token값 가져오기 */
        MyFirebaseMessagingService().getFirebaseToken()

        prefs.setString("deviceInited", "false")
        initDeviceInfo()


        if (prefs.getString("refreshToken", "") == "") {
            setFrag(FragmentLogin(), popBackStack=0, fragIndex=0)
        } else setFrag(FragmentBaedal(), popBackStack=0, fragIndex=1)

        binding.linearLayout.visibility = View.GONE
        binding.btnHome.setOnClickListener { makeToast("게시판 준비중입니다.")/*setFrag(FragmentHome(), popBackStack = 0, fragIndex=0)*/ }
        binding.btnBaedal.setOnClickListener { setFrag(FragmentBaedal(), popBackStack = 0, fragIndex=1) }
        binding.btnTaxi.setOnClickListener { makeToast("게시판 준비중입니다.")/*setFrag(FragmentTaxi(),popBackStack = 0, fragIndex=2)*/ }
    }

    override fun onResume() {
        super.onResume()
        Log.d("[$TAG] onResume", "")
        initDynamicLink()
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        val count = fm.backStackEntryCount
        if (count > 0)
            super.onBackPressed()
        else {
            if(System.currentTimeMillis() - mBackWait >= 2000) {
                mBackWait = System.currentTimeMillis()
                Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }

    fun initDeviceInfo() {
        Log.d("[$TAG][initDeviceInfo]", prefs.getString("deviceInited", "false"))

        if (prefs.getString("deviceInited", "false") != "true") {
            val refreshToken = prefs.getString("refreshToken", "")
            Log.d("[$TAG]access token", prefs.getString("accessToken", ""))
            Log.d("[$TAG]refresh token", refreshToken)

            if (refreshToken != "") {
                showProgress()
                api.getUserInfo().enqueue(object : Callback<UserInfo> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                        hideProgress()
                        if (response.code() == 200) {
                            response.body()?.let { setUserInfo(it) }
                            sendFcmToken()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            logOut(errorResponse.msg)
                            Log.d("$TAG[getUserInfo]", "${errorResponse.code}: ${errorResponse.msg}")
                        }
                    }

                    override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                        hideProgress()
                        Log.e("$TAG[getUserInfo]", t.message.toString())
                        makeToast("유저 정보 갱신 실패")
                    }
                })
            }
        }
    }

    fun sendFcmToken() {
        val previous = prefs.getString("previousFcmToken", "")
        val fcmToken = prefs.getString("fcmToken", "")
        Log.d("[$TAG][sendFcmToken]", "previous: $previous, current: $fcmToken")
        if (fcmToken == "") {
            Log.d(TAG, "fcm 토큰 조회 실패")
            return
        }
        if (previous==fcmToken) {
            Log.d(TAG, "토큰 일치")
            return
        }
        Log.d(TAG,"토큰 갱신")
        showProgress()
        api.sendFcmToken(FcmToken(fcmToken)).enqueue(object : Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                hideProgress()
                if (response.code() == 204) {
                    prefs.setString("previousFcmToken", fcmToken)
                    setFrag(FragmentBaedal(), popBackStack = 0, fragIndex = 1)
                }
                else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    logOut(errorResponse.msg)
                    Log.d("$TAG[sendFcmToken]", "${errorResponse.code}: ${errorResponse.msg}")
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                hideProgress()
                Log.e("$TAG[sendFcmToken]", t.message.toString())
                makeToast("fcm 토큰 전송 실패")
            }
        })
    }

    /** DynamicLink */
    private fun initDynamicLink() {
        val dynamicLinkData = intent.extras
        if (dynamicLinkData != null) {
            val postId = dynamicLinkData.getString("post_id")
            if (postId != null) {
                setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
                intent.removeExtra("post_id")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != -1) {
            val requestPermission = RequestPermission(this)
            when (requestCode) {
                requestPermission.PERMISSIONS_REQUEST_NOTIFICATION -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        prefs.setString("notificationPermission", "true")
                    } else {
                        prefs.setString("notificationPermission", "denied")
                    }
                }
                else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    /**
     * fragment: 이동할 프래그먼트,
     * arguments: 이동한 프래그먼트에 전달할 인자,
     * popBackStack: 새로운 frag 일시에 -1, 다른탭으로 이동시에 0 (frag stack 초기화),
     * fragIndex: 하단바 번호(홈:0, 배달:1, 택시:2)
     */
    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1, fragIndex:Int = bottomBarIndex) {
        // fragIndex와 하단바 인덱스를 비교하여 다른 탭으로 이동하였을 경우 해당 탭 강조
        if (bottomBarIndex != fragIndex) setBottomBarSize(fragIndex)

        if (arguments != null) {        // 넘겨줄 인자가 있나 체크
            val bundle = Bundle()
            for (i in arguments.keys) {
                bundle.putString(i, arguments[i])
            }
            fragment.arguments = bundle
        }

        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        // -1: 새로운 frag 추가, 0: 탭이동 (스택 초기화), else: 횟수만큼 뒤로 가기
        when (popBackStack) {
            -1 -> {
                //transaction.setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_to_right)
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.add(R.id.main_frame, fragment).addToBackStack(null)
            }
            0 -> {
                val count = fm.backStackEntryCount
                for (i in 0 until count) { fm.popBackStack() }
                transaction.replace(R.id.main_frame, fragment)
            }
            else -> {
                for (i in 0 until popBackStack) { fm.popBackStack() }
                //transaction.replace(R.id.main_frame, fragment)
                transaction.add(R.id.main_frame, fragment).addToBackStack(null)
            }
        }
        transaction.commit()
    }

    /**
     * fragindex: 강조할 탭 인덱스
     */
    private fun setBottomBarSize(fragindex: Int) {
        val r: Resources = resources
        var smallSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,30F, r.displayMetrics).toInt()
        var bigSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,40F, r.displayMetrics).toInt()

        when (bottomBarIndex) {
            0 -> binding.btnHome.updateLayoutParams {
                width = smallSize
                height = smallSize
            }

            1 -> binding.btnBaedal.updateLayoutParams {
                width = smallSize
                height = smallSize
            }
            2 -> binding.btnTaxi.updateLayoutParams {
                width = smallSize
                height = smallSize
            }
            3 -> binding.btnKara.updateLayoutParams {
                width = smallSize
                height = smallSize
            }
            4 -> binding.btnFreeBoard.updateLayoutParams {
                width = smallSize
                height = smallSize
            }
        }

        when (fragindex) {
            0 -> binding.btnHome.updateLayoutParams {
                width = bigSize
                height = bigSize
            }
            1 -> binding.btnBaedal.updateLayoutParams {
                width = bigSize
                height = bigSize
            }
            2 -> binding.btnTaxi.updateLayoutParams {
                width = bigSize
                height = bigSize
            }
            3 -> binding.btnKara.updateLayoutParams {
                width = bigSize
                height = bigSize
            }
            4 -> binding.btnFreeBoard.updateLayoutParams {
                width = bigSize
                height = bigSize
            }
        }
        bottomBarIndex = fragindex
    }

    fun showSoftInput(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    fun hideSoftInput() {
        val currentFocus = this.currentFocus
        if (currentFocus is EditText) {
            currentFocus.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    fun showProgress() { binding.progressBackground.visibility = View.VISIBLE }

    fun hideProgress() { binding.progressBackground.visibility = View.GONE }

    fun copyToClipboard(label:String, content: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, content)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun makeToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun decodeToken(jwt: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "Requires SDK 26"
        val parts = jwt.split(".")
        return try {
            val charset = charset("UTF-8")
            val header = String(Base64.getUrlDecoder().decode(parts[0].toByteArray(charset)), charset)
            val payload = String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
            "$header"
            "$payload"
        } catch (e: Exception) {
            "Error parsing JWT: $e"
        }
    }

    fun setUserInfo(userInfo: UserInfo) {
        prefs.setString("name", userInfo.name)
        prefs.setString("nickname", userInfo.nickname)
        prefs.setString("accountNum", userInfo.accountNumber)
    }

    fun logOut(message: String?=null) {
        message?.let { makeToast(it) }

        prefs.removeString("accessToken")
        prefs.removeString("refreshToken")
        prefs.removeString("userId")
        prefs.removeString("nickname")
        prefs.removeString("name")
        prefs.removeString("accountNum")
        prefs.removeString("deviceInited")
        setFrag(FragmentLogin(), null, 0)
    }
}