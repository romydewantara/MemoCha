package com.example.memocha.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.memocha.R
import com.example.memocha.entity.Schedule
import kotlinx.android.synthetic.main.recyclerview_dashboard_schedule.view.*
import kotlin.collections.ArrayList

@SuppressLint("NotifyDataSetChanged", "UseCompatLoadingForDrawables")
class DashboardScheduleAdapter(
    private val context: Context,
    private val schedules: ArrayList<Schedule>
) : RecyclerView.Adapter<DashboardScheduleAdapter.ScheduleViewHolder>() {

    private var currentHour = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_dashboard_schedule, parent, false))
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.itemView.textStartTime.text = schedule.startTime
        holder.itemView.textEndTime.text = schedule.endTime
        holder.itemView.textAction.text = schedule.action
        holder.itemView.layoutFieldSchedule.background = context.getDrawable(R.drawable.background_schedule_fields_mesty)
        if (currentHour.toInt() >= schedule.startTime.split(":")[0].toInt()
            && currentHour.toInt() <= schedule.endTime.split(":")[0].toInt()) {
            holder.itemView.layoutFieldSchedule.background = context.getDrawable(R.drawable.background_schedule_fields_yellow)
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textAction,
            1, 16, 1, TypedValue.COMPLEX_UNIT_SP)
    }

    override fun getItemCount(): Int = schedules.size

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(currentHour: String, list: List<Schedule>) {
        this.currentHour = currentHour
        schedules.clear()
        schedules.addAll(list)
        notifyDataSetChanged()
    }
}