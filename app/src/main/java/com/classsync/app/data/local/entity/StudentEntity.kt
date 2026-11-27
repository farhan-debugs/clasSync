package com.classsync.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a student in the roster.
 */
@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rollNumber: String,
    val name: String,
    val photoUrl: String? = null,
    val classId: String = "default",    // Which class/section this student belongs to
    val email: String? = null
)
