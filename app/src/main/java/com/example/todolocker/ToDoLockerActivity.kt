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
import org.json.JSONException
import org.json.JSONArray


class ToDoLockerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_locker)

        val listPref =  getStringArrayPref("listData")
        Log.d("size : ", listPref.size.toString())
        // 아이디는 전체 리스트 갯수 중 하나로 랜덤으로 정함
        val id = Random().nextInt(listPref.size)
        Log.d("임의로 선택된 아이디 id : ", id.toString())

        Log.d("0번째  : ", listPref[0])
        Log.d("1번재 : ", listPref[1])

        if (listPref.size > 0) {
            for (value in listPref) {
                Log.d("listData 내용 : ", "할일 : $value")
            }
        }

        // 아이디에 해당하는 할일 가져옴
        Log.d("id에 해당하는 할일: ", listPref[id])
        textView.setText(listPref[id])


        //seekBar 변경시 리스너
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
                    // 못했으면
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

                    // 완료했으면 리스트에서 삭제
                    progress < 5 -> {
                        // 할일 사라지고
                        Log.d("삭제될 놈 : ${id} ",listPref[id] )
                        // 삭제하고
                        listPref.removeAt(id)

                        // 결과 찍어보기
                        Log.d("삭제된 후 size : ", listPref.size.toString())
                        if (listPref.size > 0) {
                            for (value in listPref) {
                                Log.d("삭제후 listData 내용 : ", "Get json : $value")
                            }
                        }

                        // 설정화면에서도 사라지게 배열 저장
                        setStringArrayPref("listData", listPref)

                        // 잠금해제
                        finish()
                    }

                    else -> seekBar?.progress = 50
                }
            }
        })
    }

    // JSON 배열로 저장
    fun setStringArrayPref(key: String, values: ArrayList<String>) {
        val prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val a = JSONArray()
        for (i in 0 until values.size) {
            a.put(values[i])
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, null)
        }
        editor.apply()
    }

    // 저장된 배열 받아옴
    fun getStringArrayPref(key: String): ArrayList<String> {
        val prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        val json = prefs.getString(key, null)
        val urls = ArrayList<String>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url = a.optString(i)
                    urls.add(url)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return urls
    }


}