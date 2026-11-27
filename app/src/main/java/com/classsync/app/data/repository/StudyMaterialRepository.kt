package com.classsync.app.data.repository

import com.classsync.app.data.local.dao.StudyMaterialDao
import com.classsync.app.data.local.entity.StudyMaterialEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyMaterialRepository @Inject constructor(
    private val dao: StudyMaterialDao
) {
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>> = dao.getAllMaterials()
    fun getMaterialsBySubject(subject: String): Flow<List<StudyMaterialEntity>> =
        dao.getMaterialsBySubject(subject)

    suspend fun addMaterial(material: StudyMaterialEntity): Long = dao.insertMaterial(material)
    suspend fun markDownloaded(id: Long, path: String) = dao.markDownloaded(id, path)
    suspend fun deleteMaterial(material: StudyMaterialEntity) = dao.deleteMaterial(material)
}
