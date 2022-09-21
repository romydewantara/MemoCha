package com.example.kuntan.utility

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kuntan.dao.*
import com.example.kuntan.entity.*

@Database(entities = [Settings::class, Schedule::class, Identity::class, Needs::class, History::class], version = 1)
abstract class KuntanRoomDatabase : RoomDatabase() {

    abstract fun settingsDao(): SettingsDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun identityDao(): IdentityDao
    abstract fun needsDao(): NeedsDao
    abstract fun historyDao(): HistoryDao

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