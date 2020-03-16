package com.example.todolocker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder

@Suppress("UNREACHABLE_CODE")
class LockScreenService :Service(){

    // 화면꺼질때 브로드케스트 msg 수신하는 리시버
    var receiver : ScreenOffReceiver? = null

    private val ANDROID_CHANNEL_ID = "com.example.todolocker"
    private val NOTIFICATION_ID = 9999

    override fun onCreate() {
        super.onCreate()

        // null인 경우만 실행
        if(receiver ==null){
            receiver = ScreenOffReceiver()
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            registerReceiver(receiver, filter)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

        if(intent != null){
            if(intent.action == null){
                // 서비스가 최초 실행이 아닌경우
                // 리시버 null이면 새로 생성하고 등록함
                if(receiver == null){
                    receiver = ScreenOffReceiver()
                    val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                    registerReceiver(receiver, filter)
                }
            }
        }

        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        if(receiver !=null)
            unregisterReceiver(receiver)

    }

    // 이놈 필수
    override fun onBind(intent: Intent?): IBinder? {
        return null //To change body of created functions use File | Settings | File Templates.

    }
}