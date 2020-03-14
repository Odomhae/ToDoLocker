package com.example.todolocker

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import android.view.View
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.preference.PreferenceManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_to_do_locker.*
import java.util.Arrays.asList
import java.util.*
import kotlin.collections.ArrayList
import android.R.id.edit
import android.content.SharedPreferences



class MainActivity : AppCompatActivity() {

    val fragment = MyPreferenceFragment()

    // 정의하고
    // 할일 리스트
    val listDataPref by lazy { getSharedPreferences("listData", Context.MODE_PRIVATE) }
    // 전체 리스트 갯수
    val listCountPref by lazy { getSharedPreferences("listCount" , Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // frameLayout 부분에 frament 삽입
        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()

        // 빈 데이터 리스트 생성.
        val items = ArrayList<String>()
        items.add("오오도")
        items.add("오오d도")

        // ArrayAdapter 생성. 아이템 View를 선택(single choice)가능하도록 만듦.
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.setAdapter(adapter)


        //할일 추가버튼
        addListButton.setOnClickListener {
            val count = adapter.count
            Log.d("갯수 : ", count.toString())

            // 텍스트로 저장
            items.add(editText.text.toString())
            // 키 + string
            listDataPref.edit().putString(count.toString(), editText.text.toString()).apply()

            // 리스트 갯수
            // 가져와서
            var listCount = listCountPref.getInt("listNumber", 0)
            //++
            listCount++
            listCountPref.edit().putInt("listNumber", listCount).apply()

            editText.setText("")
            adapter.notifyDataSetChanged()
        }

        // 할일 제거
        // 일단은 다 제거하는걸로
        deleteListButton.setOnClickListener {
            val checked = listView.checkedItemPosition
            items.clear()
            adapter.notifyDataSetChanged()
            // 숫자도 초기화
            listCountPref.edit().clear().apply()

            // 데이터도 초기화
            listDataPref.edit().clear().apply()
        }

        // 리스트 보내기
        completeButton.setOnClickListener {
            //val intent = Intent(this, ToDoLockerActivity::class.java)
            val intent = Intent(this, ScreenOffReceiver::class.java)
            intent.putStringArrayListExtra("list", items)
            Log.d("111", "zzzzz")
            startService(intent)

        }

    }

    class MyPreferenceFragment : PreferenceFragment(){
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // 환경설정 리소스 파일
            // xml 폴더의 pref 파일
            addPreferencesFromResource(R.xml.pref)

            // 잠금화면 사용 스위치 객체 사용
            // useLockScreen키로 찾음
            val useLockScreenPref = findPreference("useLockScreen") as SwitchPreference

            useLockScreenPref.setOnPreferenceClickListener {
                when{
                    // 퀴즈 잠금화면 사용이 체크된 경우 lockScreenService 실행
                    useLockScreenPref.isChecked ->{
                        Log.d("앱 사용여부", "체크됨")
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            activity.startForegroundService(Intent(activity, LockScreenService::class.java))
                        }else{
                            activity.startService(Intent(activity, LockScreenService::class.java))
                        }
                    }
                    // 사용 체크 안됬으면 서비스 중단
                    else -> activity.stopService(Intent(activity, LockScreenService::class.java))
                }
                true
            }

            // 앱이 시작됬을대 이미 퀴즈잠금화면 사용이 체크되어있으면 서비스 실행
            if(useLockScreenPref.isChecked){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    activity.startForegroundService(Intent(activity, LockScreenService::class.java))
                }else{
                    activity.startService(Intent(activity, LockScreenService::class.java))
                }

            }
        }

    }


}