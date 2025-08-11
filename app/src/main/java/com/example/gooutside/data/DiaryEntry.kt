package com.example.gooutside.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creationDate: Date,
    val imagePath: String,
    val streetName: String?,
    val city: String?,
    val country: String?,
    val longitude: Double?,
    val latitude: Double?,
)
