package com.classsync.app.data.repository

import com.classsync.app.data.local.dao.AssignmentDao
import com.classsync.app.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val dao: AssignmentDao
) {
    fun getAllAssignments(): Flow<List<AssignmentEntity>> = dao.getAllAssignments()
    fun getPendingAssignments(): Flow<List<AssignmentEntity>> = dao.getPendingAssignments()
    fun getAssignmentsBySubject(subject: String): Flow<List<AssignmentEntity>> =
        dao.getAssignmentsBySubject(subject)

    suspend fun addAssignment(assignment: AssignmentEntity): Long = dao.insertAssignment(assignment)
    suspend fun toggleComplete(id: Long, done: Boolean) = dao.setCompleted(id, done)
    suspend fun updateAssignment(assignment: AssignmentEntity) = dao.updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: AssignmentEntity) = dao.deleteAssignment(assignment)
}
