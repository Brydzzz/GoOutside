package com.example.gooutside.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate


@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creationDate: LocalDate,
    val imagePath: String,
    val street: String?,
    val streetNumber: String?,
    val city: String?,
    val country: String?,
    val longitude: Double?,
    val latitude: Double?,
)
