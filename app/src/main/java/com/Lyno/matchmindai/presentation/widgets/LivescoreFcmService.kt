package com.Lyno.matchmindai.presentation.widgets

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.TimeUnit

/**
 * Firebase Cloud Messaging service voor real-time widget updates.
 * Ontvangt push notifications voor live wedstrijd updates.
 */
class LivescoreFcmService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "LivescoreFcmService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "FCM message received: ${remoteMessage.data}")

        // Check of dit een wedstrijd update is
        if (remoteMessage.data.containsKey("match_id")) {
            handleMatchUpdate(remoteMessage.data)
        } else if (remoteMessage.data.containsKey("action")) {
            handleAction(remoteMessage.data["action"])
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        // Hier zou je de token naar je server kunnen sturen
    }

    private fun handleMatchUpdate(data: Map<String, String>) {
        Log.d(TAG, "Handling match update: $data")
        
        // Forceer een widget update
        WidgetSyncWorker.forceUpdate(applicationContext)
    }

    private fun handleAction(action: String?) {
        when (action) {
            "update_widget" -> {
                WidgetSyncWorker.forceUpdate(applicationContext)
            }
            "schedule_aggressive_update" -> {
                scheduleAggressiveUpdate()
            }
            else -> {
                Log.d(TAG, "Unknown action: $action")
            }
        }
    }

    private fun scheduleAggressiveUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<WidgetSyncWorker>(
            5, TimeUnit.MINUTES
        )
            .addTag("AGGRESSIVE_UPDATE")
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "aggressive_update_${System.currentTimeMillis()}",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }
}
