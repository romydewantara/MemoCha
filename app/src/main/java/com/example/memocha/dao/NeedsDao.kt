package com.example.memocha.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.memocha.entity.Needs

@Dao
interface NeedsDao {

    @Insert
    suspend fun insert(needs: Needs)

    @Query("SELECT * FROM needs_table WHERE month = :month AND year = :year ORDER BY dayOfMonth ASC")
    suspend fun getNeeds(month: String, year: String) : List<Needs>

    @Query("UPDATE needs_table SET item = :item, dayOfMonth = :dayOfMonth, time = :time, checked = :checked WHERE id = :id")
    suspend fun updateNeeds(id: Int, item: String, dayOfMonth: String, time: String, checked: Boolean)

    @Query("UPDATE needs_table SET checked = :checked WHERE id = :id")
    suspend fun updateChecked(id: Int, checked: Boolean)

    @Query("DELETE FROM needs_table WHERE id = :id")
    suspend fun deleteNeeds(id: Int)

}