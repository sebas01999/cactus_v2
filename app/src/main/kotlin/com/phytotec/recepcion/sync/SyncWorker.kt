package com.phytotec.recepcion.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.phytotec.recepcion.data.repository.AsignacionRepository
import com.phytotec.recepcion.data.repository.ConfirmacionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Sincroniza las dos colas offline-first de la app en una sola corrida:
 * confirmaciones de recepción (Escanear) y asignaciones a clasificador
 * (Clasificación). Un solo Worker en vez de uno por cola — ambas son
 * "manda lo pendiente al servidor, reintenta si no hay red" con la misma
 * forma, no hace falta duplicar la clase ni el registro periódico.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val confirmacionRepository: ConfirmacionRepository,
    private val asignacionRepository: AsignacionRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val confirmaciones = confirmacionRepository.syncPending()
        val asignaciones = asignacionRepository.syncPending()

        val huboFalloTotal = confirmaciones.failed > 0 && confirmaciones.synced == 0 && confirmaciones.remaining == 0 &&
            asignaciones.failed > 0 && asignaciones.synced == 0 && asignaciones.remaining == 0

        return if (huboFalloTotal) Result.failure() else Result.success()
    }
}
