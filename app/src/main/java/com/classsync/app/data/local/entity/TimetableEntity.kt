package com.classsync.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single class slot in the weekly timetable.
 */
@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,         // 1 = Monday … 6 = Saturday
    val startTime: String,      // "09:30"
    val endTime: String,        // "10:30"
    val subject: String,
    val room: String,
    val professor: String,
    val classId: String = "default"
)
