package com.phytotec.recepcion.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phytotec.recepcion.data.repository.ConfirmacionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Envía las confirmaciones de escaneo pendientes a
 * POST /api/recepciones/{id}/confirmar. Seguro de correr repetidas veces
 * (programado cada 15 min + al recuperar conectividad + "sincronizar
 * ahora" manual) porque confirmar es idempotente por recepcionId — nunca
 * duplica el evento en el servidor.
 */
@HiltWorker
class ConfirmacionSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val confirmacionRepository: ConfirmacionRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val outcome = confirmacionRepository.syncPending()

        return if (outcome.failed > 0 && outcome.synced == 0 && outcome.remaining == 0) {
            Result.failure()
        } else {
            Result.success()
        }
    }
}
