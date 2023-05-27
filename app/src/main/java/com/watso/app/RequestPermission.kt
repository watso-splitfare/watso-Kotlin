package com.watso.app

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat

class RequestPermission(val activity: MainActivity) {
    private val TAG = "권한 요청"
    val PERMISSIONS_REQUEST_NOTIFICATION = 123

    interface NotiPermitChangedListener { fun onNotiPermitChanged(permission: String) }
    private var notiPermitChangedListener: NotiPermitChangedListener? = null
    fun setNotiPermitChangedListener(listener: NotiPermitChangedListener) {
        this.notiPermitChangedListener = listener
    }

    fun requestNotificationPermission() {
        if (getPrefs() == "") showPurposeDialog()
    }

    fun changeNotificationEnabled() {
        if (isNotificationEnabled()) {
            setPrefs("false")
        } else {
            when (getPrefs()) {
                "false" -> {
                    if (isNotificationPermitted())
                        setPrefs("true")
                    else
                        setPrefs("false")
                }
                else -> showPurposeDialog()
            }
        }
    }

    fun isNotificationPermitted(): Boolean {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    fun isNotificationEnabled(): Boolean {
        Log.d("[$TAG] 알림가능여부","getPrefs: ${getPrefs()}, isPermitted: ${isNotificationPermitted()}")
        if (getPrefs() == "true") {
            if (isNotificationPermitted())
                return true
            else {
                setPrefs("denied")
                return false
            }
        }
        return false
    }

    fun showPurposeDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("알림 권한 요청")
            .setMessage("게시글 관련 안내사항이나 댓글소식을 알림으로 전달받기 위해서 권한을 요청합니다.")
            .setPositiveButton("알림 설정", DialogInterface.OnClickListener { dialog, id ->
                getNotiPermission()
                setPrefs("true")
            })
            .setNegativeButton("거절", DialogInterface.OnClickListener { dialog, id ->
                notiPermitChangedListener?.onNotiPermitChanged("")
                setPrefs("ignore")
            })
        builder.show()
    }

    fun getNotiPermission() {
        if (getPrefs() == "") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSIONS_REQUEST_NOTIFICATION
                )
            } else {
                makeIntent()
            }
        } else makeIntent()
    }

    fun makeIntent() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        activity.startActivity(intent)
    }

    fun getPrefs():String {
        return MainActivity.prefs.getString("notificationPermission", "")
    }

    fun setPrefs(status: String) {
        MainActivity.prefs.setString("notificationPermission", status)
    }
}