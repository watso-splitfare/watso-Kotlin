package com.saengsaengtalk.app

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView

class LoopingDialog(context: Context): Dialog(context) {
    lateinit var animationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_looping)
        animationView = findViewById(R.id.lottie)
        animationView.setAnimation("looping-loader-animation.json")
        animationView.repeatCount = ValueAnimator.INFINITE
        animationView.playAnimation()
    }

}

interface lopingInterface {
    fun endLoop()
}