package com.example.todolocker


import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.collections.ArrayList
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import android.provider.AlarmClock


class MainActivity : AppCompatActivity() {

    lateinit var context: Context
    lateinit var alarmManager: AlarmManager

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


    // 마감시간 설정
    fun getDeadlineTime() : Array<Int>{
        var returnTime =  Array(2, {-1})

        // 입력한 시간, 분
        var inputHour = ""
        var inputMinute = ""

        // : 기준으로 문자열 나눔
        val array = deadlineTimeEditText.text.split(":")

        //시간만 입력한경우
        // 분은 0으로 설정
        if(array.size == 1){
            inputHour = array[0]
            inputMinute = "0"
        }
        // 너무 길게 입력한 경우
        // 다시 입력
        else if(array.size >2){
            Toast.makeText(applicationContext, R.string.invalid_time_message, Toast.LENGTH_SHORT).show()
            deadlineTimeEditText.setText("")
        }
        else{
            inputHour = array[0]
            inputMinute = array[1]

            Log.d("시간, : 기준 앞", inputHour)
            Log.d("분, : 기준 뒤", inputMinute)
        }

        // 입력한 시간, 분 숫자로
        val hh = inputHour.toInt()
        val mm = inputMinute.toInt()

        // 24이상이거나 60이상이면 다시 입력받음
        if(hh >= 0 && hh <=23 && mm >=0 && mm <=59 ){
            Log.d("입력한 시간", hh.toString())
            Log.d("입력한 분", mm.toString())

            returnTime[0] = hh
            returnTime[1] = mm
        }

        return  returnTime // 올바른 시간 , 분을 반환해야 한다
    }

    @SuppressLint("SimpleDateFormat")
    override fun onStart() {
        super.onStart()

        // 마감시간 설정
        setDeadlineButton.setOnClickListener {

            Log.d("선택한 시간 ", deadlineTimeEditText.text.toString())

            /// 알림 세팅
            context = this
            alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, Receiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            // 오늘 날짜 가져와서
            val todayDate = Date().date.toString()
            val todayMonth = (Date().month +1).toString()
            val todayYear = (Date().year +1900).toString()
            val fin = "$todayDate/$todayMonth/$todayYear"
            Log.d("오늘 날짜 ", fin)

            val formatter =  SimpleDateFormat("dd/MM/yyyy");
            val date = formatter.parse(fin)

            Log.d("오늘 날짜를 밀리세컨으로 ", date?.time.toString())

            // 설정한 시간만큼 더해줘야한다.
            val getDeadlineTime = getDeadlineTime()
            var hour = getDeadlineTime[0]  // 시간
            var minute = getDeadlineTime[1]  // 분

            if(getDeadlineTime[0] ==-1 || getDeadlineTime[1] ==-1){
                Toast.makeText(applicationContext, R.string.invalid_time_message, Toast.LENGTH_SHORT).show()
                deadlineTimeEditText.setText("")
                return@setOnClickListener
            }

            // 마감시간이 00시면 23시로
            if(getDeadlineTime[0] == 0)
                hour = 23
            // 마감시간 숫자가 현재시간 숫자보다 작으면 다음날로 넘겨야됨
            if(getDeadlineTime[0] < Date().hours)
                hour = 24 + getDeadlineTime[0]

            hour *= 1000 *3600 // 시간
            minute *= 1000 * 60 // 분

            val deadlineTime = date.time + hour + minute
            Log.d("설정한 시간을 밀리세컨으로 ", deadlineTime.toString())

            val listPref =  getStringArrayPref("listData")
            // 할일이 없으면
            if(listPref.size == 0){
                Toast.makeText(applicationContext, R.string.no_item_message, Toast.LENGTH_SHORT).show()
                deadlineTimeEditText.setText("")
            }
            // 못한게 있으면 한시간 전에 알림
            else if(listPref.size > 0) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, (deadlineTime - (1000 * 3600)), pendingIntent)

                Toast.makeText(applicationContext, R.string.deadline_set_message, Toast.LENGTH_SHORT).show()
            }

        }
    }

    class Receiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            Log.d("알림 나타나는 시간 ", "Receiver "+ Date().toString())
            val intent = Intent(context, alarmNotification::class.java)
            context?.startService(intent)
        }
    }

    class alarmNotification : Service(){
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

            // 꺼진 화면 켜고
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            var wLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "todolocker:TAG")
            wLock.acquire(3000)
            wLock.release()

            Log.d("222", "알림")
            val builder = NotificationCompat.Builder(this)

            builder.setSmallIcon(R.drawable.okayimage)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.okayimage)
                //.setVibrate(longArrayOf(1L, 2L))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.one_hour_left_message))

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val NOTIFICAITON_ID = "NOTIFICATION_CHAN"
                val chan = NotificationChannel(NOTIFICAITON_ID, "ttt", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(chan)
                builder.setChannelId(NOTIFICAITON_ID)

            }

            notificationManager.notify(11, builder.build()) // 11 == channel id

            return START_NOT_STICKY
        }




        override fun onBind(intent: Intent?): IBinder? {
            return null
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