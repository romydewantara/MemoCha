package com.example.memocha

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

class ScheduleActivity : AppCompatActivity(), ScheduleAdapter.ScheduleAdapterListener, ScheduleEditorBottomSheet.ScheduleEditorListener {

    companion object {
        const val TAG = "ScheduleActivity"
        const val SCHEDULES = "schedules"

        const val subuh = "Subuh"
        const val dzuhur = "Dzuhur"
        const val ashar = "Ashar"
        const val maghrib = "Maghrib"
        const val isya = "Isya"
    }

    private val database by lazy { MemoChaRoomDatabase(this) }
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var currentTab = subuh
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
            subuh -> navigate(subuh)
            dzuhur -> navigate(dzuhur)
            ashar -> navigate(ashar)
            maghrib -> navigate(maghrib)
            isya -> navigate(isya)
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
                                refreshSchedule(currentTab) //after import
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
        buttonSubuh.setOnClickListener { navigate(subuh) }
        buttonDzuhur.setOnClickListener { navigate(dzuhur) }
        buttonAshar.setOnClickListener { navigate(ashar) }
        buttonMaghrib.setOnClickListener { navigate(maghrib) }
        buttonIsya.setOnClickListener { navigate(isya) }
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
            layoutManager = LinearLayoutManager(this@ScheduleActivity)
            adapter = scheduleAdapter
        }
    }

    private fun navigate(time: String) {
        when (time) {
            subuh -> {
                buttonSubuh.isChecked = true
                currentTab = subuh
            }
            dzuhur -> {
                buttonDzuhur.isChecked = true
                currentTab = dzuhur
            }
            ashar -> {
                buttonAshar.isChecked = true
                currentTab = ashar
            }
            maghrib -> {
                buttonMaghrib.isChecked = true
                currentTab = maghrib
            }
            isya -> {
                buttonIsya.isChecked = true
                currentTab = isya
            }
        }
        refreshSchedule(currentTab) //navigate
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
            refreshSchedule(currentTab) //after edit schedule
        }
        Snackbar.make(rootLayoutSchedule, getString(R.string.snackbar_schedule_updated), Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.snackbar_button_dismiss)) {}.show()
    }

    override fun onAddNewSchedule(id: Int, startTime: String, endTime: String, actions: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().insert(Schedule(id, currentTab, startTime, endTime, actions))
            refreshSchedule(currentTab) //after add new schedule
        }
        Snackbar.make(rootLayoutSchedule, getString(R.string.snackbar_schedule_added), Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.snackbar_button_dismiss)) {}.show()
    }

    override fun onDeleteSchedule(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().deleteSchedule(id)
            refreshSchedule(currentTab) //after delete schedule
        }
    }
}