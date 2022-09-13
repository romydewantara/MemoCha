package com.example.kuntan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.Times.*
import com.example.kuntan.adapter.ScheduleAdapter
import com.example.kuntan.entity.Schedule
import com.example.kuntan.lib.KuntanPopupDialog
import com.example.kuntan.lib.ScheduleEditorBottomSheet
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Times {
    Subuh, Dzuhur, Ashar, Maghrib, Isya
}

class ScheduleActivity : AppCompatActivity(), ScheduleAdapter.ScheduleAdapterListener, ScheduleEditorBottomSheet.ScheduleEditorListener {

    private val database by lazy { KuntanRoomDatabase(this) }
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var currentTab = Subuh.name
    private var isScheduleEmpty = true
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
        add.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.scheduleDao().insert(Schedule(0, currentTab, "00:00", "00:00", "Empty"))
                refreshSchedule(currentTab)
            }
            /*val scheduleEditorBottomSheet = ScheduleEditorBottomSheet(
                currentTab, 0, "0", "0", "0", "0", this)
            scheduleEditorBottomSheet.isCancelable = false
            scheduleEditorBottomSheet.show(supportFragmentManager, scheduleEditorBottomSheet.tag)*/
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
            isScheduleEmpty = schedules.isEmpty()
            runOnUiThread {
                if (isScheduleEmpty) textViewEmpty.visibility = View.VISIBLE  else textViewEmpty.visibility = View.GONE
            }
            withContext(Dispatchers.Main) {
                scheduleAdapter.setData(schedules)
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

    override fun onEditItemClicked(
        id: Int,
        startHour: String,
        startMinute: String,
        endHour: String,
        endMinute: String
    ) {
        val scheduleEditorBottomSheet =
            ScheduleEditorBottomSheet(currentTab, id, startHour, startMinute, endHour, endMinute, this)
        scheduleEditorBottomSheet.isCancelable = false
        scheduleEditorBottomSheet.show(supportFragmentManager, scheduleEditorBottomSheet.tag)
    }

    override fun onEditSchedule(id: Int, startTime: String, endTime: String, actions: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().updateSchedule(id, startTime, endTime, actions)
            refreshSchedule(currentTab)
        }
    }

    override fun onDeleteSchedule(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.scheduleDao().deleteSchedule(id)
            refreshSchedule(currentTab)
        }
    }
}