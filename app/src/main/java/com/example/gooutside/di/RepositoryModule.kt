package com.example.gooutside.di

import com.example.gooutside.data.DiaryEntriesRepository
import com.example.gooutside.data.OfflineDiaryEntriesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindDiaryEntriesRepository(impl: OfflineDiaryEntriesRepository): DiaryEntriesRepository

}