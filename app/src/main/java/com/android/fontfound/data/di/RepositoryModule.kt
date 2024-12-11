package com.android.fontfound.data.di

import com.android.fontfound.data.repository.HistoryRepository
import com.android.fontfound.data.retrofit.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideHistoryRepository(apiService: ApiService): HistoryRepository {
        return HistoryRepository(apiService)
    }
}