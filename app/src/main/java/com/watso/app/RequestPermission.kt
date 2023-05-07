package com.watso.app

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity

class RequestPermission(val activity: MainActivity) {
    private val TAG = "권한 요청"
    val PERMISSIONS_REQUEST_NOTIFICATION = 123

    fun requestNotificationPermission() {
        if (isNotificationEnabled()) setPrefs(true)
        else callNotiPermissionDialog()
    }

    private fun isNotificationEnabled(): Boolean {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    private fun callNotiPermissionDialog() {
        Log.d(TAG, "4")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("알림 권한 요청")
            .setMessage("게시글 관련 안내사항이나 댓글소식을 알림으로 받아 보세요!")
            .setPositiveButton("알림 설정", DialogInterface.OnClickListener { dialog, id ->
                getNotiPermission()
            })
            .setNegativeButton("거절", DialogInterface.OnClickListener { dialog, id ->
                setPrefs(false)
            })
        builder.show()
    }

    private fun getNotiPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSIONS_REQUEST_NOTIFICATION
            )
        } else {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
            activity.startActivity(intent)
        }
    }

    private fun setPrefs(bool: Boolean) {
        MainActivity.prefs.setString("notificationPermission", bool.toString())
    }
}