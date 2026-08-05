package com.phytotec.recepcion.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val PERIODIC_WORK_NAME = "confirmacion-sync-periodic"
private const val MANUAL_WORK_NAME = "confirmacion-sync-manual"

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val connectedConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Runs roughly every 15 minutes whenever there's connectivity — WorkManager's floor for periodic work. */
    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<ConfirmacionSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(connectedConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** Triggered right after scanning a barcode, and by the "Sincronizar ahora" button. */
    fun syncNow() {
        val request = OneTimeWorkRequestBuilder<ConfirmacionSyncWorker>()
            .setConstraints(connectedConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(MANUAL_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
