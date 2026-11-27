package com.classsync.app.data.local.dao

import androidx.room.*
import com.classsync.app.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    @Query("SELECT * FROM assignments ORDER BY dueDateTime ASC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE isCompleted = 0 ORDER BY dueDateTime ASC")
    fun getPendingAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE subject = :subject ORDER BY dueDateTime ASC")
    fun getAssignmentsBySubject(subject: String): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Query("UPDATE assignments SET isCompleted = :done WHERE id = :id")
    suspend fun setCompleted(id: Long, done: Boolean)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)
}
