package com.example.kuntan.utility

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kuntan.dao.HistoryDao
import com.example.kuntan.dao.ScheduleDao
import com.example.kuntan.dao.SettingsDao
import com.example.kuntan.entity.History
import com.example.kuntan.entity.Schedule
import com.example.kuntan.entity.Settings

@Database(entities = [Schedule::class, History::class, Settings::class], version = 1, exportSchema = false)
abstract class KuntanRoomDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun historyDao(): HistoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var instance: KuntanRoomDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            KuntanRoomDatabase::class.java,
            "kuntan.db"
        ).build()
    }

}