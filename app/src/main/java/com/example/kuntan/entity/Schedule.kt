package com.example.kuntan.entity

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type

@Entity(tableName = "schedule_table")
data class Schedule(@PrimaryKey(autoGenerate = true) val id: Int, val time: String, val startTime: String, val endTime: String, val action: String)