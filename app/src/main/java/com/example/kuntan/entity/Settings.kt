package com.example.kuntan.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_table")
data class Settings(@PrimaryKey(autoGenerate = true) val id: Int, val language: String, val backgroundAnimation: String, val dashboardBackground: String, val backgroundMusicState: String)