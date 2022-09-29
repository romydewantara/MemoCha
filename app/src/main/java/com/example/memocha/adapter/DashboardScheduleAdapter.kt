package com.example.memocha.adapter

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.memocha.R
import com.example.memocha.entity.Schedule
import kotlinx.android.synthetic.main.recyclerview_dashboard_schedule.view.*

class DashboardScheduleAdapter(private val schedules: ArrayList<Schedule>) : RecyclerView.Adapter<DashboardScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_dashboard_schedule, parent, false))
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.itemView.textStartTime.text = schedule.startTime
        holder.itemView.textEndTime.text = schedule.endTime
        holder.itemView.textAction.text = schedule.action
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textAction,
            1, 16, 1, TypedValue.COMPLEX_UNIT_SP)
    }

    override fun getItemCount(): Int = schedules.size

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Schedule>) {
        schedules.clear()
        schedules.addAll(list)
        notifyDataSetChanged()
    }
}