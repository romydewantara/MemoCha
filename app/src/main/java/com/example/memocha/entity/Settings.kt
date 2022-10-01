package com.example.memocha.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_table")
data class Settings(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val surname: String,
    val applicationTheme: String,
    val applicationLanguage: String,
    val clockTheme: String,
    val backgroundAnimation: String,
    val surnameState: Boolean,
    val backgroundAnimationState: Boolean,
    val backgroundMusicState: Boolean,
    val notificationState: Boolean,
    val badgeState: Boolean
)