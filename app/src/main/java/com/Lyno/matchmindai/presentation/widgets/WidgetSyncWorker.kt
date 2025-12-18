package com.Lyno.matchmindai.presentation.widgets

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker die periodiek widget data synchroniseert.
 * Deze worker haalt nieuwe wedstrijd data op en update de widget.
 */
class WidgetSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                // Voor nu doen we niets, gewoon een placeholder
                // In de toekomst zou hier de widget update logica komen
                
                Result.success()
            }
        } catch (e: Exception) {
            // Log de error maar return success om te voorkomen dat WorkManager stopt
            e.printStackTrace()
            Result.success() // We willen niet dat de worker stopt bij errors
        }
    }

    companion object {
        private const val WORK_NAME = "widget_sync_work"

        /**
         * Start de periodieke widget synchronisatie.
         * Standaard interval: 15 minuten (minimum voor WorkManager)
         */
        fun schedulePeriodicSync(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .addTag("WIDGET_SYNC")
                .setInitialDelay(5, TimeUnit.MINUTES) // Wacht 5 minuten bij eerste start
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }

        /**
         * Stop alle widget synchronisatie workers.
         */
        fun cancelAllSync(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag("WIDGET_SYNC")
        }

        /**
         * Forceer een directe widget update.
         */
        fun forceUpdate(context: Context) {
            val oneTimeWork = PeriodicWorkRequestBuilder<WidgetSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .addTag("FORCE_UPDATE")
                .setInitialDelay(0, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "force_update_${System.currentTimeMillis()}",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    oneTimeWork
                )
        }
    }
}
