package com.example.kuntan.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kuntan.entity.History

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: History)

    @Query("SELECT * FROM history_table WHERE year = :year AND month = :month ORDER BY id ASC")
    suspend fun getHistory(year: String, month: String): List<History>

    @Query("UPDATE history_table SET yearEdited = :yearEdited, monthEdited = :monthEdited, dateEdited = :dateEdited, timeEdited = :timeEdited, goods = :goods, amount = :amount, description = :description, category = :category, method = :method WHERE id = :id")
    suspend fun updateHistory(id: Int, yearEdited: String, monthEdited: String, dateEdited: String, timeEdited: String, goods: String, amount: String, description: String, category: String, method: String)

    @Query("DELETE FROM history_table WHERE id = :id")
    suspend fun deleteFromHistory(id: Int)

}