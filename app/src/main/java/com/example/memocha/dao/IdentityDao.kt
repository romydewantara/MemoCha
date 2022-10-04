package com.example.memocha.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memocha.entity.Identity

@Dao
interface IdentityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(identity: Identity)

    @Query("SELECT * FROM identity_table")
    suspend fun getIdentity(): List<Identity>

    @Query("UPDATE identity_table SET idNumber = :idNumber, name = :name, address = :address, dob = :dob, phone = :phone WHERE idNumber = :idNumber")
    suspend fun updateIdentity(idNumber: String, name: String, address: String, dob: String, phone: String)

    @Query("DELETE FROM identity_table WHERE idNumber = :idNumber")
    suspend fun deleteIdentity(idNumber: String)
}