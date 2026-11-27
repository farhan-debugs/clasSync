package com.classsync.app.data.local.dao

import androidx.room.*
import com.classsync.app.data.local.entity.StudyMaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyMaterialDao {

    @Query("SELECT * FROM study_materials ORDER BY addedAt DESC")
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE subject = :subject ORDER BY addedAt DESC")
    fun getMaterialsBySubject(subject: String): Flow<List<StudyMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterialEntity): Long

    @Update
    suspend fun updateMaterial(material: StudyMaterialEntity)

    @Query("UPDATE study_materials SET isDownloaded = 1, localPath = :path WHERE id = :id")
    suspend fun markDownloaded(id: Long, path: String)

    @Delete
    suspend fun deleteMaterial(material: StudyMaterialEntity)
}
