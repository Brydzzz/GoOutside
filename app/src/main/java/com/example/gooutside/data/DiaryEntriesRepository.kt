package com.example.gooutside.data

import kotlinx.coroutines.flow.Flow

interface DiaryEntriesRepository {
    fun getDiaryEntryStream(id: Int): Flow<DiaryEntry?>

    fun getAllDiaryEntriesStream(): Flow<List<DiaryEntry>>

    fun getRecentEntries(): Flow<List<DiaryEntry>>

    suspend fun insertDiaryEntry(diaryEntry: DiaryEntry)

    suspend fun updateDiaryEntry(diaryEntry: DiaryEntry)

    suspend fun deleteDiaryEntry(diaryEntry: DiaryEntry)
}