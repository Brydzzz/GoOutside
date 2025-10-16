package com.example.gooutside.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class OfflineDiaryEntriesRepository @Inject constructor(private val diaryEntryDao: DiaryEntryDao) :
    DiaryEntriesRepository {
    override fun getDiaryEntryStream(id: Int): Flow<DiaryEntry?> = diaryEntryDao.getById(id)

    override fun getAllDiaryEntriesStream(): Flow<List<DiaryEntry>> = diaryEntryDao.getAll()

    override fun getRecentEntries(): Flow<List<DiaryEntry>> = diaryEntryDao.getRecentEntries()

    override fun getEntriesForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DiaryEntry>> = diaryEntryDao.getEntriesForDateRange(startDate, endDate)


    override suspend fun insertDiaryEntry(diaryEntry: DiaryEntry) = diaryEntryDao.insert(diaryEntry)

    override suspend fun deleteDiaryEntry(diaryEntry: DiaryEntry) = diaryEntryDao.delete(diaryEntry)

    override suspend fun updateDiaryEntry(diaryEntry: DiaryEntry) = diaryEntryDao.update(diaryEntry)
}