package com.example.kuntan.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kuntan.entity.Needs

@Dao
interface NeedsDao {

    @Insert
    suspend fun insert(needs: Needs)

    @Query("SELECT * FROM needs_table")
    suspend fun getNeeds() : List<Needs>

    @Query("UPDATE needs_table SET item = :item, date = :date, time = :time, isChecked = :isChecked WHERE id = :id")
    suspend fun updateNeeds(id: Int, item: String, date: String, time: String, isChecked: Boolean)

    @Query("DELETE FROM needs_table WHERE id = :id")
    suspend fun deleteNeeds(id: Int)

}