package com.classsync.app.data.repository

import com.classsync.app.data.local.dao.AttendanceDao
import com.classsync.app.data.local.dao.StudentDao
import com.classsync.app.data.local.entity.AttendanceEntity
import com.classsync.app.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {

    fun getStudents(classId: String): Flow<List<StudentEntity>> =
        studentDao.getStudentsByClass(classId)

    suspend fun importStudents(students: List<StudentEntity>) =
        studentDao.insertStudents(students)

    suspend fun markAttendance(
        studentId: Long,
        subject: String,
        date: String,
        status: String
    ): Long {
        val record = AttendanceEntity(
            studentId = studentId,
            subject   = subject,
            date      = date,
            status    = status,
            synced    = false
        )
        return attendanceDao.insertRecord(record)
    }

    fun getStudentAttendance(studentId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStudent(studentId)

    fun getSessionAttendance(subject: String, date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForSession(subject, date)

    suspend fun getUnsyncedRecords(): List<AttendanceEntity> =
        attendanceDao.getUnsyncedRecords()

    suspend fun markSynced(ids: List<Long>) =
        attendanceDao.markSynced(ids)

    /** Compute attendance % for a student in a subject */
    suspend fun getAttendancePercent(studentId: Long, subject: String): Float {
        val summary = attendanceDao.getAttendanceSummary(studentId, subject)
        val total   = summary.sumOf { it.count }
        if (total == 0) return 0f
        val present = summary.firstOrNull { it.status == "P" }?.count ?: 0
        return (present.toFloat() / total) * 100f
    }
}
