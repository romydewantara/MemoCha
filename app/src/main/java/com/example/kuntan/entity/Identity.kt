package com.example.kuntan.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "identity_table")
class Identity(@PrimaryKey(autoGenerate = true) val id: Int, val idNumber: String, val name: String, val address: String)