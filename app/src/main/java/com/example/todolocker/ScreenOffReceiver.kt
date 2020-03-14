package com.example.todolocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class ScreenOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when{
            intent?.action == Intent.ACTION_SCREEN_OFF ->{
                Log.d("eeee", "화면꺼짐")
                Toast.makeText(context, "화면꺼짐 ", Toast.LENGTH_LONG).show()

                // 화면꺼지면 locker 액티비티 실행
                val intent = Intent(context, ToDoLockerActivity::class.java)

                // 액티비티 실행
                context?.startActivity(intent)

            }
        }

    }
}