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
import android.widget.ListView
import android.widget.Toast
import kotlin.collections.ArrayList
import org.json.JSONArray
import org.json.JSONException



class MainActivity : AppCompatActivity() {

    // 빈 데이터 리스트 생성.
    val items = ArrayList<String>()
    //  하나의? 여려개 선택도 가능한게 나은데 // 이 가능한  adapter 설정
    val adapter by lazy {  ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items) }

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
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        //할일 추가버튼
        addListButton.setOnClickListener {
            val count = adapter.count
            Log.d("갯수 : ", count.toString())

            addList()
        }

        // 선택된 리스트만 삭제
        deleteListButton.setOnClickListener {
            //선택된 아이템들
            val checkedItems = listView.checkedItemPositions

            for (i in adapter.count - 1 downTo 0) {
                if (checkedItems.get(i)) {
                    Log.d("선택된 아이템 삭제", i.toString())
                    items.removeAt(i)
                }
            }
            adapter.notifyDataSetChanged()

            // 선택 초기화
            listView.clearChoices()
            // 배열로 저장
            setStringArrayPref("listData", items)

        }

        // 리스트 다 삭제
        clearListButton.setOnClickListener {
            items.clear()
            adapter.notifyDataSetChanged()

            // 배열로 저장
            setStringArrayPref("listData", items)
        }

        // 당겨서 새로고침
        pullToRefresh.setOnRefreshListener {
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
            // 선택 초기화
            listView.clearChoices()


            pullToRefresh.isRefreshing = false
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
        }
    }

    // 할일 추가버튼 함수
    private fun addList(){

        if(editText.text.isEmpty()){
            Toast.makeText(applicationContext, R.string.empty_input_message, Toast.LENGTH_SHORT).show()
        }
        // 빈 입력 아니면 추가
        else{
            // 텍스트 추가
            items.add(editText.text.toString())
            // 배열로 저장
            setStringArrayPref("listData", items)

            editText.setText("")
            adapter.notifyDataSetChanged()
        }

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