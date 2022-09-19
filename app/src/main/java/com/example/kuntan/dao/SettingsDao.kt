package com.example.kuntan.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kuntan.entity.Settings

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSetting(settings: Settings)

    @Query("SELECT * FROM settings_table")
    suspend fun getSettings(): Settings

    @Query("UPDATE settings_table SET username = :username, theme = :theme, language = :language, analogClockTheme = :analogClockTheme, backgroundAnimation = :backgroundAnimation, dashboardBackground = :dashboardBackground, backgroundMusicState = :backgroundMusicState")
    suspend fun updateSetting(
        username: String,
        theme: String,
        language: String,
        analogClockTheme: String,
        backgroundAnimation: String,
        dashboardBackground: String,
        backgroundMusicState: String
    )

}