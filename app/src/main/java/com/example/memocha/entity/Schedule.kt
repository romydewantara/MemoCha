package com.example.memocha.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_table")
data class Schedule(@PrimaryKey(autoGenerate = true) val id: Int, val time: String, val startTime: String, val endTime: String, val action: String)