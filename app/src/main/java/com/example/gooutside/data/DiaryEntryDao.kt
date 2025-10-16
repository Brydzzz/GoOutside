package com.example.gooutside.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DiaryEntryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(diaryEntry: DiaryEntry)

    @Update
    suspend fun update(diaryEntry: DiaryEntry)

    @Delete
    suspend fun delete(diaryEntry: DiaryEntry)

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    fun getById(id: Int): Flow<DiaryEntry>

    @Query("SELECT * FROM diary_entries ORDER BY creationDate DESC")
    fun getAll(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries ORDER BY creationDate DESC LIMIT 5")
    fun getRecentEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE creationDate BETWEEN :startDate AND :endDate order by creationDate desc")
    fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DiaryEntry>>


}