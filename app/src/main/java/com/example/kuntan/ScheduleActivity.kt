package com.example.kuntan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.Times.*
import com.example.kuntan.adapter.ScheduleAdapter
import com.example.kuntan.entity.Schedule
import com.example.kuntan.lib.KuntanPopupDialog
import com.example.kuntan.lib.ScheduleEditorBottomSheet
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.Constant
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

enum class Times {
    Subuh, Dzuhur, Ashar, Maghrib, Isya
}

class ScheduleActivity : AppCompatActivity(), ScheduleAdapter.ScheduleAdapterListener, ScheduleEditorBottomSheet.ScheduleEditorListener {

    companion object {
        const val TAG = "ScheduleActivity"
    }

    private val database by lazy { KuntanRoomDatabase(this) }
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var currentTab = Subuh.name
    private var isDelete = false

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
            val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent("Import default \"$currentTab Schedules\"?",
                "This action can make your list contain the default schedule",
                "Import", "Cancel", object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {
                        //AppUtil.writeFileToStorage(this@ScheduleActivity, Constant.FOLDER_NAME_SCHEDULES, currentTab, Gson().toJson(schedules))
                        val defSchedule = AppUtil.readFileFromStorage(this@ScheduleActivity, Constant.FOLDER_NAME_SCHEDULES, currentTab)
                        val array = JSONArray(defSchedule)
                        CoroutineScope(Dispatchers.IO).launch {
                            for (i in 0 until array.length()) {
                                val jsonObject = array.getJSONObject(i)
                                database.scheduleDao().insert(Gson().fromJson(jsonObject.toString(), Schedule::class.java))
                            }
                            val list = database.scheduleDao().getSchedule(currentTab)
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
            kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
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
            val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent("Remove \"All Schedule\"?",
                "If you delete the entire schedule it will be deleted permanently",
                "Delete All", "Cancel", object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.scheduleDao().deleteAll()
                            refreshSchedule(currentTab)
                        }
                    }
                    override fun onPositiveButton() {

                    }

                })
            kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
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
                if (schedules.isEmpty()) {
                    imageImport.isClickable = true
                    imageImport.isFocusable = true
                    imageImport.setImageResource(R.drawable.ic_import_teal_dark)
                    textViewEmpty.visibility = View.VISIBLE
                }  else {
                    imageImport.isClickable = false
                    imageImport.isFocusable = false
                    imageImport.setImageResource(R.drawable.ic_import_gray)
                    textViewEmpty.visibility = View.GONE
                    textViewEmpty.startAnimation(AnimationUtils.loadAnimation(this@ScheduleActivity, R.anim.fade_out))
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
            .setAction("DISMISS") {}.show()
    }

    override fun onAddNewSchedule(id: Int, startTime: String, endTime: String, actions: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().insert(Schedule(id, currentTab, startTime, endTime, actions))
            refreshSchedule(currentTab)
        }
        Snackbar.make(rootLayoutSchedule, getString(R.string.snackbar_schedule_added), Snackbar.LENGTH_SHORT)
            .setAction("DISMISS") {}.show()
    }

    override fun onDeleteSchedule(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().deleteSchedule(id)
            refreshSchedule(currentTab)
        }
    }
}