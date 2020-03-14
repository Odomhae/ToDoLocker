package com.example.todolocker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_to_do_locker.*
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import java.util.Random


class ToDoLockerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_locker)

        // 할일 받아와서
        val listDataPref = getSharedPreferences("listData", Context.MODE_PRIVATE)


        // 아이디는 전체 리스트 갯수 중 하나로 랜덤으로 정함
        // 전체 리스트 갯수
        val listCountPref = getSharedPreferences("listCount" , Context.MODE_PRIVATE)
        val id = Random().nextInt(listCountPref.getInt("listNumber",0))

        Log.d("아이디 id : ", id.toString())
        // 아이디에 해당하는 할일 가져옴
        val item = listDataPref.getString(id.toString(), "왜안되누" )
        Log.d("item : ", item.toString())
        textView.setText(item)


        //seekbar 변경시 리스너
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when{
                    //우측 끝
                    progress >95 -> {
                        rightImageView.setImageResource(R.drawable.thumbsdown)
                        leftImageView.setImageResource(R.drawable.okayimage)
                    }
                    // 왼쪽 끝
                    progress <5 ->{
                        rightImageView.setImageResource(R.drawable.ximage)
                        leftImageView.setImageResource(R.drawable.thumbsup)
                    }
                    else -> {
                        rightImageView.setImageResource(R.drawable.ximage)
                        leftImageView.setImageResource(R.drawable.okayimage)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            // 터지 조작 다 끝낸 경우
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: 50 // 널값을 허용하지 않는 변수에 널 값이 들어 갔을때 50으로

                when{
                    // 못했으면 진동하고
                    progress > 95 -> {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        // SDk 버전에 따라
                        if(Build.VERSION.SDK_INT >= 26){
                            // 2.0초동안 200의 세기로 진동
                            vibrator.vibrate(VibrationEffect.createOneShot(2000, 200))
                        }else{
                            vibrator.vibrate(1000)
                        }

                        Toast.makeText(applicationContext, "아따 오늘안엔 하자", Toast.LENGTH_SHORT).show()
                        //잠금해제
                        finish()
                    }

                    // 완료했으면 짧은 진동에
                    // 할일 사라지고? 리스트에서 색 변하고?
                    progress < 5 -> {



                        // 잠금해제
                        finish()
                    }

                    else -> seekBar?.progress = 50
                }
            }
        })
    }


}