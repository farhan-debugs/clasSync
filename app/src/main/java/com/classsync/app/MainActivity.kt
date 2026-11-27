package com.classsync.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.classsync.app.ui.navigation.ClassSyncNavGraph
import com.classsync.app.ui.theme.ClassSyncTheme
import com.classsync.app.util.NotificationUtils
import com.classsync.app.worker.DailyDigestWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workManager: WorkManager

    // Notification permission launcher (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) scheduleDailyDigest()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channels
        NotificationUtils.createAllChannels(this)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationUtils.hasNotificationPermission(this)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleDailyDigest()
            }
        } else {
            scheduleDailyDigest()
        }

        setContent {
            ClassSyncTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ClassSyncNavGraph()
                }
            }
        }
    }

    private fun scheduleDailyDigest() {
        workManager.enqueueUniquePeriodicWork(
            DailyDigestWorker.WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            DailyDigestWorker.buildRequest()
        )
    }
}
