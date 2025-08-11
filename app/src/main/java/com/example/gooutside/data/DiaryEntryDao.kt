package com.example.gooutside.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM diary_entries")
    fun getAll(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries ORDER BY creationDate DESC LIMIT 5")
    fun getRecentEntries(): Flow<List<DiaryEntry>>

}