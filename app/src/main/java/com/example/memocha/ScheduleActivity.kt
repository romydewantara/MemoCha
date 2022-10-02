package com.example.memocha

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memocha.Times.*
import com.example.memocha.adapter.ScheduleAdapter
import com.example.memocha.entity.Schedule
import com.example.memocha.lib.MemoChaPopupDialog
import com.example.memocha.lib.ScheduleEditorBottomSheet
import com.example.memocha.utility.AppUtil
import com.example.memocha.utility.Constant
import com.example.memocha.utility.MemoChaRoomDatabase
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class Times {
    Subuh, Dzuhur, Ashar, Maghrib, Isya
}

class ScheduleActivity : AppCompatActivity(), ScheduleAdapter.ScheduleAdapterListener, ScheduleEditorBottomSheet.ScheduleEditorListener {

    companion object {
        const val TAG = "ScheduleActivity"
        const val SCHEDULES = "schedules"
    }

    private val database by lazy { MemoChaRoomDatabase(this) }
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var currentTab = Subuh.name
    private var isDelete = false
    private var isSchedulesEmpty = true

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        if (intent.extras != null) currentTab = intent.extras?.getString("time").toString()

        setupRecyclerView()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        when(currentTab) {
            Subuh.name -> navigate(Subuh)
            Dzuhur.name -> navigate(Dzuhur)
            Ashar.name -> navigate(Ashar)
            Maghrib.name -> navigate(Maghrib)
            Isya.name -> navigate(Isya)
        }
    }

    private fun initListener() {
        imageImport.setOnClickListener {
            val kPopupDialog = MemoChaPopupDialog.newInstance()
            if (isSchedulesEmpty) {
                kPopupDialog.setContent(String.format(getString(R.string.dialog_title_import_schedule), currentTab),
                    getString(R.string.dialog_message_import_schedule), getString(R.string.button_import), getString(R.string.button_cancel),
                    object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                        override fun onNegativeButton() {
                            val defSchedule = AppUtil.readTextFromAssets(this@ScheduleActivity, Constant.FOLDER_ASSETS_FILE_SCHEDULE)
                            val schedules = JSONObject(defSchedule).getJSONObject(SCHEDULES)
                            val array = schedules.getJSONArray(currentTab)
                            CoroutineScope(Dispatchers.IO).launch {
                                for (i in 0 until array.length()) {
                                    val jsonObject = array.getJSONObject(i)
                                    Log.d(TAG, "onNegativeButton - jObject: $jsonObject")
                                    database.scheduleDao().insert(Gson().fromJson(jsonObject.toString(), Schedule::class.java))
                                }
                                val list = database.scheduleDao().getSchedule(currentTab)
                                Log.d(TAG, "onNegativeButton - list: $list")
                                withContext(Dispatchers.Main) {
                                    scheduleAdapter.setData(list)
                                    runOnUiThread {
                                        refreshSchedule(currentTab)
                                    }
                                }
                            }
                        }
                        override fun onPositiveButton() {}
                    })
            } else {
                kPopupDialog.setContent("", getString(R.string.dialog_message_schedule_filled),
                    "", getString(R.string.button_ok), object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                        override fun onNegativeButton() {}
                        override fun onPositiveButton() {}
                    })
            }
            kPopupDialog.show(supportFragmentManager, kPopupDialog.tag)
        }
        add.setOnClickListener {
            showScheduleEditorBottomSheet(0, "00", "00", "00", "00", "",false)
        }
        trash.setOnClickListener {
            isDelete = false //isDelete = !isDelete //not used yet… o_0
            if (isDelete) {
                buttonSelectAll.visibility = View.VISIBLE
                buttonSelectAll.isEnabled = true
            } else {
                buttonSelectAll.visibility = View.INVISIBLE
                buttonSelectAll.isEnabled = false
            }
            val mcPopupDialog = MemoChaPopupDialog.newInstance().setContent(getString(R.string.dialog_title_delete_all_schedule),
                getString(R.string.dialog_message_delete_all_schedule), getString(R.string.dialog_button_delete_all), getString(R.string.button_cancel),
                object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                    override fun onNegativeButton() {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.scheduleDao().deleteAll()
                            refreshSchedule(currentTab)
                        }
                    }
                    override fun onPositiveButton() {

                    }

                })
            mcPopupDialog.show(supportFragmentManager, mcPopupDialog.tag)
        }
        buttonSubuh.setOnClickListener { navigate(Subuh) }
        buttonDzuhur.setOnClickListener { navigate(Dzuhur) }
        buttonAshar.setOnClickListener { navigate(Ashar) }
        buttonMaghrib.setOnClickListener { navigate(Maghrib) }
        buttonIsya.setOnClickListener { navigate(Isya) }
    }

    private fun refreshSchedule(time: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val schedules = database.scheduleDao().getSchedule(time)
            withContext(Dispatchers.Main) {
                scheduleAdapter.setData(schedules)
            }
            runOnUiThread {
                isSchedulesEmpty = schedules.isEmpty()
                if (schedules.isEmpty()) {
                    imageImport.setImageResource(R.drawable.ic_import_teal_dark)
                    textViewEmpty.visibility = View.VISIBLE
                }  else {
                    imageImport.setImageResource(R.drawable.ic_import_gray)
                    textViewEmpty.visibility = View.GONE
                }
            }
        }
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(arrayListOf(), this)
        recyclerviewSchedule.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = scheduleAdapter
        }
    }

    private fun navigate(time: Times) {
        when (time) {
            Subuh -> {
                buttonSubuh.isChecked = true
                currentTab = Subuh.name
            }
            Dzuhur -> {
                buttonDzuhur.isChecked = true
                currentTab = Dzuhur.name
            }
            Ashar -> {
                buttonAshar.isChecked = true
                currentTab = Ashar.name
            }
            Maghrib -> {
                buttonMaghrib.isChecked = true
                currentTab = Maghrib.name
            }
            Isya -> {
                buttonIsya.isChecked = true
                currentTab = Isya.name
            }
        }
        refreshSchedule(currentTab)
    }

    private fun showScheduleEditorBottomSheet(
        id: Int,
        startHour: String,
        startMinute: String,
        endHour: String,
        endMinute: String,
        actions: String,
        isEdit: Boolean
    ) {
        val scheduleEditorBottomSheet =
            ScheduleEditorBottomSheet(currentTab, id, startHour, startMinute, endHour, endMinute, actions, isEdit, this)
        scheduleEditorBottomSheet.isCancelable = false
        scheduleEditorBottomSheet.show(supportFragmentManager, scheduleEditorBottomSheet.tag)
    }

    override fun onEditItemClicked(
        id: Int,
        startHour: String,
        startMinute: String,
        endHour: String,
        endMinute: String,
        action: String
    ) {
        showScheduleEditorBottomSheet(id, startHour, startMinute, endHour, endMinute, action, true)
    }

    override fun onEditSchedule(id: Int, startTime: String, endTime: String, actions: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().updateSchedule(id, startTime, endTime, actions)
            refreshSchedule(currentTab)
        }
        Snackbar.make(rootLayoutSchedule, getString(R.string.snackbar_schedule_updated), Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.snackbar_button_dismiss)) {}.show()
    }

    override fun onAddNewSchedule(id: Int, startTime: String, endTime: String, actions: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().insert(Schedule(id, currentTab, startTime, endTime, actions))
            refreshSchedule(currentTab)
        }
        Snackbar.make(rootLayoutSchedule, getString(R.string.snackbar_schedule_added), Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.snackbar_button_dismiss)) {}.show()
    }

    override fun onDeleteSchedule(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().deleteSchedule(id)
            refreshSchedule(currentTab)
        }
    }
}