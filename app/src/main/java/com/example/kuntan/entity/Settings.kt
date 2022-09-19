package com.example.kuntan.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_table")
data class Settings(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val username: String,
    val theme: String,
    val language: String,
    val analogClockTheme: String,
    val backgroundAnimation: String,
    val dashboardBackground: String,
    val backgroundMusicState: String
)