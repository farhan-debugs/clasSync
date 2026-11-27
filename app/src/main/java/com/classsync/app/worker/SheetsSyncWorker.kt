package com.classsync.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.classsync.app.data.local.AppPreferences
import com.classsync.app.data.local.dao.StudentDao
import com.classsync.app.data.remote.SheetsApiService
import com.classsync.app.data.repository.AttendanceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that syncs unsynced attendance records to Google Sheets.
 * Runs with NETWORK_CONNECTED constraint — guaranteed delivery even after app restart.
 */
@HiltWorker
class SheetsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val attendanceRepository: AttendanceRepository,
    private val sheetsApiService: SheetsApiService,
    private val prefs: AppPreferences,
    private val studentDao: StudentDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsynced = attendanceRepository.getUnsyncedRecords()
            if (unsynced.isEmpty()) return Result.success()

            val spreadsheetId = prefs.linkedSheetId.first()
                ?: return Result.failure()   // No sheet linked — can't sync
            val userEmail = prefs.userEmail.first()
            if (userEmail.isBlank()) return Result.failure()

            val classId     = prefs.classId.first()
            val allStudents = studentDao.getStudentsByClass(classId).first()
            val nameMap     = allStudents.associate { it.id to it.name }

            sheetsApiService.syncAttendance(
                spreadsheetId = spreadsheetId,
                accountEmail  = userEmail,
                records       = unsynced,
                studentNames  = nameMap
            ).onSuccess {
                val ids = unsynced.map { it.id }
                attendanceRepository.markSynced(ids)
            }.onFailure { e ->
                // Retry on network failure
                if (runAttemptCount < 3) return Result.retry()
                return Result.failure(
                    workDataOf("error" to (e.message ?: "Unknown error"))
                )
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_TAG = "sheets_sync"

        fun buildRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SheetsSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .addTag(WORK_TAG)
                .build()
    }
}
