package com.example.kuntan.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val year: String,
    val month: String,
    val date: String,
    val time: String,
    val goods: String,
    val amount: String,
    val description: String,
    val category: String,
    val method: String,
    val summary: String
)