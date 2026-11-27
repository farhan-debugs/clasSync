package com.classsync.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An assignment / to-do task.
 */
@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val description: String = "",
    val priority: String = "MEDIUM",    // LOW | MEDIUM | HIGH
    val dueDateTime: Long,              // epoch millis
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
