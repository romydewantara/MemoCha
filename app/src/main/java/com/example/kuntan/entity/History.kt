package com.example.kuntan.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val year: String,
    val month: String,
    val date: String,
    var time: String,
    var goods: String,
    var amount: String,
    var description: String,
    var category: String,
    var method: String
)