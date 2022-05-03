package com.example.saengsaengtalk

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.databinding.ActivityMainBinding
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    public val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*setFrag(0)

        binding.btnHome.setOnClickListener { setFrag(0) }
        binding.btnBaedal.setOnClickListener { setFrag(1) }
        binding.btnTaxi.setOnClickListener { setFrag(100) }
        binding.btnKara.setOnClickListener { setFrag(3) }
        binding.btnFreeBoard.setOnClickListener { setFrag(4) }*/

        setFrag(FragmentHome())

        binding.btnHome.setOnClickListener { setFrag(FragmentHome()) }
        binding.btnBaedal.setOnClickListener { setFrag(FragmentBaedal()) }
        binding.btnTaxi.setOnClickListener { setFrag(FragmentBaedalPost()) }
        binding.btnKara.setOnClickListener { setFrag(FragmentKara()) }
        binding.btnFreeBoard.setOnClickListener { setFrag(FragmentFreeBoard()) }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    fun setFrag(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        // Log.d("함수 호출", fragNum.toString())
        transaction.replace(R.id.main_frame, fragment)
        transaction.commit()
    }

    fun setDataAtFrag(fragment: Fragment, postNum:String="") {
        val bundle = Bundle()
        bundle.putString("postNum", postNum)

        fragment.arguments = bundle
        setFrag(fragment)
    }
}