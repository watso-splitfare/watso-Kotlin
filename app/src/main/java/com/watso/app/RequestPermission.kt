package com.watso.app

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService

class RequestPermission(val activity: MainActivity) {
    private val TAG = "권한 요청"
    private val PERMISSIONS_REQUEST_CODE = 123

    fun checkNotificationPermission() {
        Log.d(TAG, "1")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "2")
            checkAndroid13()
        } else callNotiPermissionDialog()
//        if (!isNotificationEnabled()) {
//            requestNotificationPermission()
//        }
    }

    private fun checkAndroid13() {
        // 알림 권한 활성화 체크
        if ( !NotificationManagerCompat.from(activity).areNotificationsEnabled() ) {
            Log.d(TAG, "3")
            callNotiPermissionDialog()
        }
    }

    private fun callNotiPermissionDialog() {
        Log.d(TAG, "4")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("알림 권한 요청")
            .setMessage("게시글 관련 안내사항이나 댓글을\n알림으로 받아 보세요!")
            .setPositiveButton("알림 받기", DialogInterface.OnClickListener { dialog, id ->
                requestNotiPermission()
            })
            .setNegativeButton("거절", DialogInterface.OnClickListener { dialog, id -> })
        builder.show()
    }

    private fun requestNotiPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            PERMISSIONS_REQUEST_CODE
        )
    }

//    private fun isNotificationEnabled(): Boolean {
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            notificationManager.areNotificationsEnabled()
//        } else {
//            NotificationManagerCompat.from(context).areNotificationsEnabled()
//        }
//    }
//
//    private fun requestNotificationPermission() {
//        if (
//            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
////                checkAndroid13()
////            }
//        }
//    }
}