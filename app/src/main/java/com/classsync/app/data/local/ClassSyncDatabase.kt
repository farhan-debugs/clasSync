package com.classsync.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.classsync.app.data.local.dao.*
import com.classsync.app.data.local.entity.*

@Database(
    entities = [
        StudentEntity::class,
        AttendanceEntity::class,
        TimetableEntity::class,
        AssignmentEntity::class,
        StudyMaterialEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ClassSyncDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun timetableDao(): TimetableDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun studyMaterialDao(): StudyMaterialDao
}
