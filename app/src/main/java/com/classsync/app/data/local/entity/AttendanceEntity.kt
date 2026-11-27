package com.classsync.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single attendance record for one student in one class session.
 */
@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subject: String,
    val date: String,           // ISO-8601 date string: "2024-10-24"
    val status: String,         // "P" | "A" | "L"
    val synced: Boolean = false,
    val syncedAt: Long? = null
)
