package com.example.memocha.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memocha.entity.Schedule

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: Schedule)

    @Query("SELECT * FROM schedule_table")
    suspend fun getSchedule() : List<Schedule>

    @Query("SELECT * FROM schedule_table WHERE time = :time ORDER BY id DESC")
    suspend fun getSchedule(time: String) : List<Schedule>

    @Query("DELETE FROM schedule_table")
    suspend fun deleteAll()

    @Query("DELETE FROM schedule_table WHERE id = :id")
    suspend fun deleteSchedule(id: Int)

    @Query("UPDATE schedule_table SET startTime = :startTime, endTime = :endTime, `action` = :actions WHERE id = :id")
    suspend fun updateSchedule(id: Int, startTime: String, endTime: String, actions: String)
}