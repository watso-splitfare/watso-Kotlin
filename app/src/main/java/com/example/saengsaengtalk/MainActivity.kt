package com.example.saengsaengtalk

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.databinding.ActivityMainBinding
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFrag(0)

        binding.btnHome.setOnClickListener { setFrag(0) }
        binding.btnBaedal.setOnClickListener { setFrag(1) }
        binding.btnTaxi.setOnClickListener { setFrag(2) }
        binding.btnKara.setOnClickListener { setFrag(3) }
        binding.btnFreeBoard.setOnClickListener { setFrag(4) }
    }

    private fun setFrag(fragNum: Int) {
        val ft = supportFragmentManager.beginTransaction()
        when(fragNum)
        {
            0 -> { ft.replace(R.id.main_frame, FragmentHome()).commit() }
            1 -> { ft.replace(R.id.main_frame, FragmentBaedal()).commit() }
            2 -> { ft.replace(R.id.main_frame, FragmentTaxi()).commit() }
            3 -> { ft.replace(R.id.main_frame, FragmentKara()).commit() }
            4 -> { ft.replace(R.id.main_frame, FragmentFreeBoard()).commit() }
        }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }


}