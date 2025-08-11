package com.example.gooutside.di

import android.content.Context
import androidx.room.Room
import com.example.gooutside.data.AppDatabase
import com.example.gooutside.data.DiaryEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_database").build()
    }

    @Provides
    fun provideDiaryEntryDao(appDatabase: AppDatabase): DiaryEntryDao {
        return appDatabase.diaryEntryDao()
    }
}