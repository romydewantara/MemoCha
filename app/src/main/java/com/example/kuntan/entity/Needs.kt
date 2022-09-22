package com.example.kuntan.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "needs_table")
class Needs(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val item: String,
    val date: String,
    val time: String,
    var isDateShown: Boolean,
    val checked: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val image: ByteArray?
)