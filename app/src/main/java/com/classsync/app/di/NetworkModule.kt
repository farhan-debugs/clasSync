package com.classsync.app.di

import com.classsync.app.data.remote.SheetsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSheetsApiService(
        @ApplicationContext context: Context
    ): SheetsApiService = SheetsApiService(context)
}
