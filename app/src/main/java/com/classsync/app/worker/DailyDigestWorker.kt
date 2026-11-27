package com.classsync.app.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.classsync.app.data.local.AppPreferences
import com.classsync.app.data.local.dao.AssignmentDao
import com.classsync.app.data.local.dao.TimetableDao
import com.classsync.app.util.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Sends a daily morning digest notification: today's classes + pending assignments.
 * Scheduled daily via WorkManager periodic request.
 */
@HiltWorker
class DailyDigestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val timetableDao: TimetableDao,
    private val assignmentDao: AssignmentDao,
    private val prefs: AppPreferences
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val classId     = prefs.classId.first()
        val todayDow    = LocalDate.now().dayOfWeek.value.let { if (it == 7) 1 else it }

        val todaySlots  = timetableDao.getScheduleForDay(classId, todayDow).first()
        val pendingTasks = assignmentDao.getPendingAssignments().first()
            .filter { it.dueDateTime > System.currentTimeMillis() }
            .take(3)

        val classesText = if (todaySlots.isEmpty()) "No classes today."
            else todaySlots.joinToString(" · ") { "${it.startTime} ${it.subject.take(6)}" }

        val tasksText = if (pendingTasks.isEmpty()) "No pending tasks."
            else "⚠ ${pendingTasks.size} task(s) pending — ${pendingTasks.firstOrNull()?.title}"

        val notif = NotificationCompat.Builder(applicationContext, NotificationUtils.CHANNEL_DIGEST)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Good Morning! Here's your day 📚")
            .setContentText(classesText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$classesText\n\n$tasksText")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(1001, notif)
        } catch (_: SecurityException) {}

        return Result.success()
    }

    companion object {
        const val WORK_TAG = "daily_digest"

        /**
         * Enqueue a daily periodic digest at ~8 AM.
         * Uses [WorkManager.enqueueUniquePeriodicWork] to avoid duplicates.
         */
        fun buildRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<DailyDigestWorker>(1, TimeUnit.DAYS)
                .addTag(WORK_TAG)
                .build()
    }
}
