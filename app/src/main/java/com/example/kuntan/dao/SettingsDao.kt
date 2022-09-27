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
    suspend fun getSettings(): Settings?

    @Query("UPDATE settings_table SET surname = :surname, applicationTheme = :applicationTheme, applicationLanguage = :applicationLanguage, clockTheme = :clockTheme, backgroundAnimation = :backgroundAnimation, surnameState = :surnameState, backgroundAnimationState = :backgroundAnimationState, backgroundMusicState = :backgroundMusicState, notificationState = :notificationState")
    suspend fun updateSetting(
        surname: String,
        applicationTheme: String,
        applicationLanguage: String,
        clockTheme: String,
        backgroundAnimation: String,
        surnameState: Boolean,
        backgroundAnimationState: Boolean,
        backgroundMusicState: Boolean,
        notificationState: Boolean
    )

}