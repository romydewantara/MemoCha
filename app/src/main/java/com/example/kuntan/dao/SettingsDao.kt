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

    @Query("UPDATE settings_table SET language = :language, backgroundAnimation = :backgroundAnimation, dashboardBackground = :dashboardBackground")
    suspend fun updateSetting(language: String, backgroundAnimation: String, dashboardBackground: String)

}