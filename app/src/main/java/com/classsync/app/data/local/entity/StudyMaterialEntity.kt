package com.classsync.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A study material file / link card.
 */
@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val title: String,
    val fileType: String,           // "PDF" | "SLIDE" | "LINK" | "NOTE"
    val driveFileId: String? = null,
    val downloadUrl: String? = null,
    val localPath: String? = null,
    val fileSize: Long = 0,         // bytes
    val isDownloaded: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
