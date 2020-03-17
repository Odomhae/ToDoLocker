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
import kotlin.collections.ArrayList
import org.json.JSONArray
import org.json.JSONException


class MainActivity : AppCompatActivity() {

    // 빈 데이터 리스트 생성.
    val items = ArrayList<String>()

    val adapter by lazy {  ArrayAdapter(this, android.R.layout.simple_list_item_1, items) }

    val fragment = MyPreferenceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // frameLayout 부분에 frament 삽입
        fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()

        // 새로운 스레드로 리스트 관리
        val thread = ThreadClass()
        thread.start()

        // ArrayAdapter 생성. 아이템 View를 선택(single choice)가능하도록 만듦.
        listView.setAdapter(adapter)
        adapter.notifyDataSetChanged()

        //할일 추가버튼
        addListButton.setOnClickListener {
            val count = adapter.count
            Log.d("갯수 : ", count.toString())

            addList()
        }

        // 할일 제거버튼
        // 일단은 다 제거하는걸로
        deleteListButton.setOnClickListener {
            items.clear()
            adapter.notifyDataSetChanged()

            // 배열로 저장
            setStringArrayPref("listData", items)
        }

        //새로고침 버튼
        reloadButton.setOnClickListener {
            //삭제된게 반영된 배열을 불러와야하니까
            // 기존에 있던 거 없애고
            items.clear()
            adapter.notifyDataSetChanged()
            //  다시 채움
            val listPref2 =  getStringArrayPref("listData")
            if(listPref2.size > 0){
                for (value in listPref2)
                    items.add(value)
            }
            adapter.notifyDataSetChanged()

        }

    }

    // 리스트 관리 스레드
    inner class ThreadClass: Thread (){
        override fun run() {

            // 기존 데이터있으면 추가
            val listPref =  getStringArrayPref("listData")
            if(listPref.size > 0){
                for (value in listPref)
                    items.add(value)
            }

            items.add("오오도")
            items.add("오동도")

        }
    }

    // 할일 추가버튼 함수
    private fun addList(){

        // 텍스트 추가
        items.add(editText.text.toString())
        // 배열로 저장
        setStringArrayPref("listData", items)

        editText.setText("")
        adapter.notifyDataSetChanged()
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