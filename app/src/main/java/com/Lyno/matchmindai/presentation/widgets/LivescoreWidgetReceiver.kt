package com.Lyno.matchmindai.presentation.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * AppWidgetProvider for the Livescore widget.
 * Handles widget lifecycle events and updates.
 */
class LivescoreWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widget instances using coroutine scope
        // Note: updateAll() is a suspend function, so we need to handle it differently
        // For now, we'll just schedule a WorkManager update
        WidgetSyncWorker.forceUpdate(context)
    }

    override fun onEnabled(context: Context) {
        // Widget is added to homescreen for the first time
        // Start periodic sync
        WidgetSyncWorker.schedulePeriodicSync(context)
    }

    override fun onDisabled(context: Context) {
        // Last widget instance removed from homescreen
        // Stop periodic sync
        WidgetSyncWorker.cancelAllSync(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Widget instances deleted
        // Clean up any instance-specific data if needed
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        // Widget restored after backup/restore
        // Update widget IDs if needed
    }
}
