package com.example.kuntan

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.Times.*
import com.example.kuntan.adapter.ScheduleAdapter
import com.example.kuntan.entity.Schedule
import com.example.kuntan.lib.ScheduleEditorBottomSheet
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Times {
    Subuh, Dzuhur, Ashar, Maghrib, Isya
}

class ScheduleActivity : AppCompatActivity(), ScheduleAdapter.ScheduleAdapterListener {

    private val database by lazy { KuntanRoomDatabase(this) }
    private lateinit var scheduleAdapter: ScheduleAdapter
    var currentTab = Subuh.name

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
        }
        trash.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.scheduleDao().deleteAll()
                refreshSchedule(currentTab)
            }
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
        startHour: String,
        startMinute: String,
        endHour: String,
        endMinute: String
    ) {
        val scheduleEditorBottomSheet = ScheduleEditorBottomSheet(currentTab, startHour, startMinute, endHour, endMinute)
        scheduleEditorBottomSheet.isCancelable = false
        scheduleEditorBottomSheet.show(supportFragmentManager, scheduleEditorBottomSheet.tag)
    }
}