package com.example.saengsaengtalk

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.util.TypedValue
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.databinding.ActivityMainBinding
import com.example.saengsaengtalk.fragmentBaedal.Baedal.FragmentBaedal
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoard
import com.example.saengsaengtalk.fragmentKara.FragmentKara
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxi


class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!
    var mBackWait:Long = 0
    var bottomBarIndex:Int = 0

    companion object {
        lateinit var prefs: PreferenceUtil
    }

    //val sharedPreference = getSharedPreferences("cache", 0)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)
        //val authDebug = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTY2Mzk4OTg4OTgyNiwibmlja19uYW1lIjoiYm9uZyJ9.FULK5UjhV7UnoRa8lUP7MrW0wccROJf9GUp7bac1tvo"
        //prefs.setString("Authentication", authDebug)

        println("유저 id: ${prefs.getString("userId", "")}")
        println("인증 토큰: ${prefs.getString("Authentication", "")}")
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFrag(FragmentHome())

        binding.btnHome.setOnClickListener { setFrag(FragmentHome(), popBackStack = 0, fragIndex=0) }
        binding.btnBaedal.setOnClickListener { setFrag(FragmentBaedal(), popBackStack = 0, fragIndex=1) }
        binding.btnTaxi.setOnClickListener { setFrag(FragmentTaxi(),popBackStack = 0, fragIndex=2) }
        binding.btnKara.setOnClickListener { makeToast("게시판 준비중입니다.")/*setFrag(FragmentKara(), popBackStack = 0, fragIndex=3)*/ }
        binding.btnFreeBoard.setOnClickListener { makeToast("게시판 준비중입니다.") /*setFrag(FragmentFreeBoard(), popBackStack = 0, fragIndex=4)*/ }
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }


    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1, fragIndex:Int = 0) {
        println("argument = ${arguments}")
        println("popBackStack = ${popBackStack}")
        println("bottomBarIndex = ${bottomBarIndex}, fragIndex = ${fragIndex}")
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

        if (popBackStack == -1) {   // -1 일 경우 새로운 화면으로 이동, 0은 뒤로가기, else 각 탭 대표화면 (홈, 배달, 택시 등)
            transaction.setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_to_right)
            transaction.add(R.id.main_frame, fragment).addToBackStack(null)
        } else if (popBackStack == 0) {
            val count = fm.backStackEntryCount
            for (i in 0 until count) { fm.popBackStack() }
            transaction.replace(R.id.main_frame, fragment)
        } else {
            for (i in 0 until popBackStack) { fm.popBackStack() }
            transaction.replace(R.id.main_frame, fragment)
        }

        transaction.commit()
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        //println("루핑 호출 loopStart: ${loopStart}")
        return if (loopStart) {
            val newLoopingDialog = LoopingDialog(this)
            newLoopingDialog.show()
            newLoopingDialog
        } else {
            loopingDialog!!.dismiss()
            null
        }
    }

    fun setBottomBarSize(fragindex: Int) {
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

    fun makeToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}