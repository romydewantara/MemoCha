package com.example.memocha.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "needs_table")
class Needs(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val item: String,
    val time: String,
    val dayOfMonth: String,
    val month: String,
    val year: String,
    var isDateShown: Boolean,
    val checked: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val image: ByteArray?
)