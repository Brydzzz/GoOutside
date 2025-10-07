package com.example.gooutside

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gooutside.data.AppDatabase
import com.example.gooutside.data.DiaryEntry
import com.example.gooutside.data.DiaryEntryDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DiaryEntriesDaoTest {
    private lateinit var entryDao: DiaryEntryDao
    private lateinit var entriesDatabase: AppDatabase

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        entriesDatabase =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
                .build()
        entryDao = entriesDatabase.diaryEntryDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        entriesDatabase.close()
    }

    private var entry1 = DiaryEntry(
        1,
        LocalDate.of(2025, 5, 21),
        "content://media/external/images/media/12345",
        "Marszałkowska",
        "14",
        "Warsaw",
        "Poland",
        123.456,
        789.012
    )
    private var entry2 = DiaryEntry(
        2,
        LocalDate.of(2024, 8, 21),
        "content://media/external/images/media/12345",
        "Rogers Rd",
        "101",
        "Toronto",
        "Canada",
        654.321,
        987.012
    )

    private var entry3 = entry1.copy(id = 3, creationDate = LocalDate.of(2024, 8, 25))
    private var entry4 = entry2.copy(id = 4, creationDate = LocalDate.of(2022, 8, 21))
    private var entry5 = entry1.copy(id = 5, creationDate = LocalDate.of(2021, 5, 21))
    private var entry6 = entry2.copy(id = 6, creationDate = LocalDate.of(2020, 8, 21))


    private suspend fun addOneEntryToDb() {
        entryDao.insert(entry1)
    }

    private suspend fun addTwoEntriesToDb() {
        entryDao.insert(entry1)
        entryDao.insert(entry2)
    }

    private suspend fun addSixEntriesToDb() {
        entryDao.insert(entry1)
        entryDao.insert(entry2)
        entryDao.insert(entry3)
        entryDao.insert(entry4)
        entryDao.insert(entry5)
        entryDao.insert(entry6)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsEntryIntoDB() = runBlocking {
        addOneEntryToDb()
        val allEntries = entryDao.getAll().first()
        assertEquals(allEntries[0], entry1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetEntryById_returnsEntryFromDb() = runBlocking {
        addTwoEntriesToDb()
        val entry = entryDao.getById(1).first()
        assertEquals(entry, entry1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllEntries_returnsAllEntriesFromDb() = runBlocking {
        addTwoEntriesToDb()
        val allEntries = entryDao.getAll().first()
        assertEquals(allEntries[0], entry1)
        assertEquals(allEntries[1], entry2)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetRecentEntries_returnsFiveMostRecentEntriesFromDb() = runBlocking {
        addSixEntriesToDb()
        val allEntries = entryDao.getRecentEntries().first()
        assertEquals(allEntries[0], entry1)
        assertEquals(allEntries[1], entry3)
        assertEquals(allEntries[2], entry2)
        assertEquals(allEntries[3], entry4)
        assertEquals(allEntries[4], entry5)
        assertEquals(allEntries.size, 5)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdateEntry_updatesEntryInDb() = runBlocking {
        addTwoEntriesToDb()
        val updatedEntry = DiaryEntry(
            1,
            LocalDate.of(2025, 5, 21),
            "content://media/external/images/media/12345",
            "Marszałkowska",
            "18",
            "Warsaw",
            "Poland",
            123.456,
            789.0
        )
        entryDao.update(updatedEntry)
        val allEntries = entryDao.getAll().first()
        assertEquals(allEntries[0], updatedEntry)
    }

    @Test
    @Throws(Exception::class)
    fun daoDeleteEntries_deletesAllEntriesFromDb() = runBlocking {
        addTwoEntriesToDb()
        entryDao.delete(entry1)
        entryDao.delete(entry2)
        val allEntries = entryDao.getAll().first()
        assertTrue(allEntries.isEmpty())
    }
}