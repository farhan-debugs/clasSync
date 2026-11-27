package com.classsync.app.di

import android.content.Context
import androidx.room.Room
import com.classsync.app.data.local.AppPreferences
import com.classsync.app.data.local.ClassSyncDatabase
import com.classsync.app.data.local.dao.*
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
    fun provideDatabase(@ApplicationContext context: Context): ClassSyncDatabase =
        Room.databaseBuilder(context, ClassSyncDatabase::class.java, "classsync.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideStudentDao(db: ClassSyncDatabase): StudentDao = db.studentDao()
    @Provides fun provideAttendanceDao(db: ClassSyncDatabase): AttendanceDao = db.attendanceDao()
    @Provides fun provideTimetableDao(db: ClassSyncDatabase): TimetableDao = db.timetableDao()
    @Provides fun provideAssignmentDao(db: ClassSyncDatabase): AssignmentDao = db.assignmentDao()
    @Provides fun provideStudyMaterialDao(db: ClassSyncDatabase): StudyMaterialDao = db.studyMaterialDao()

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences =
        AppPreferences(context)
}
