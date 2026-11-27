package com.classsync.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.classsync.app.data.repository.AssignmentRepository
import com.classsync.app.util.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Sends a push notification when an assignment deadline is approaching.
 */
@HiltWorker
class AssignmentReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val assignmentRepository: AssignmentRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_ASSIGNMENT_ID    = "assignment_id"
        const val KEY_ASSIGNMENT_TITLE = "assignment_title"
        const val KEY_SUBJECT          = "subject"
    }

    override suspend fun doWork(): Result {
        val title   = inputData.getString(KEY_ASSIGNMENT_TITLE) ?: return Result.failure()
        val subject = inputData.getString(KEY_SUBJECT) ?: ""

        val notif = NotificationCompat.Builder(applicationContext, NotificationUtils.CHANNEL_ASSIGNMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Assignment Due Soon — $subject")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(System.currentTimeMillis().toInt(), notif)
        } catch (_: SecurityException) {}

        return Result.success()
    }
}
