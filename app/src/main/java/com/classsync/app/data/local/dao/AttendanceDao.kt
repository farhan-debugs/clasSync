package com.classsync.app.data.local.dao

import androidx.room.*
import com.classsync.app.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("""
        SELECT * FROM attendance_records
        WHERE studentId = :studentId
        ORDER BY date DESC
    """)
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT * FROM attendance_records
        WHERE subject = :subject AND date = :date
        ORDER BY studentId ASC
    """)
    fun getAttendanceForSession(subject: String, date: String): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT status, COUNT(*) as count
        FROM attendance_records
        WHERE studentId = :studentId AND subject = :subject
        GROUP BY status
    """)
    suspend fun getAttendanceSummary(studentId: Long, subject: String): List<StatusCount>

    @Query("SELECT * FROM attendance_records WHERE synced = 0")
    suspend fun getUnsyncedRecords(): List<AttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceEntity>)

    @Query("UPDATE attendance_records SET synced = 1, syncedAt = :time WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>, time: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteRecord(record: AttendanceEntity)
}

data class StatusCount(val status: String, val count: Int)
