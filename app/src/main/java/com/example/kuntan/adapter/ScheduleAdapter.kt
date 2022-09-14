package com.example.kuntan.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import com.example.kuntan.entity.Schedule
import kotlinx.android.synthetic.main.recyclerview_item_schedule.view.*

class ScheduleAdapter(private val schedules: ArrayList<Schedule>, private val scheduleAdapterListener: ScheduleAdapterListener)
    : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_schedule, parent, false))
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.itemView.textStartTime.text = schedule.startTime
        holder.itemView.textEndTime.text = schedule.endTime
        holder.itemView.textAction.text = schedule.action
        holder.itemView.edit.setOnClickListener {
            scheduleAdapterListener.onEditItemClicked(
                schedule.id,
                schedule.startTime.split(":")[0],
                schedule.startTime.split(":")[1],
                schedule.endTime.split(":")[0],
                schedule.endTime.split(":")[1],
                schedule.action
            )
        }
    }

    override fun getItemCount(): Int = schedules.size

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Schedule>) {
        schedules.clear()
        schedules.addAll(list)
        notifyDataSetChanged()
    }

    interface ScheduleAdapterListener {
        fun onEditItemClicked(id: Int, startHour: String, startMinute: String, endHour: String, endMinute: String, action: String)
    }
}