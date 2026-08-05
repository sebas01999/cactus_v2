package com.phytotec.recepcion.data.repository

import com.phytotec.recepcion.data.local.dao.ConfirmacionDao
import com.phytotec.recepcion.data.local.entities.ConfirmacionLocalEntity
import com.phytotec.recepcion.data.local.entities.SyncStatus
import com.phytotec.recepcion.data.remote.ApiService
import com.phytotec.recepcion.data.remote.RecepcionDetalleDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfirmacionRepository @Inject constructor(
    private val apiService: ApiService,
    private val confirmacionDao: ConfirmacionDao,
) {
    fun observeHistorial(): Flow<List<ConfirmacionLocalEntity>> = confirmacionDao.observeHistorial()

    fun observePendingCount(): Flow<Int> = confirmacionDao.observePendingCount()

    /**
     * Best-effort: intenta traer los datos legibles de la recepción para
     * mostrarlos antes de confirmar (y así el operario verifica que
     * escaneó la caja correcta). Si no hay conexión, devuelve null — el
     * escaneo se guarda igual, solo que sin esta vista previa.
     */
    suspend fun lookupDetalle(recepcionId: Int): RecepcionDetalleDto? =
        try {
            apiService.getRecepcion(recepcionId)
        } catch (e: Exception) {
            null
        }

    /**
     * Guarda el escaneo localmente y devuelve de inmediato — nunca depende
     * de la red. La sincronización real la dispara quien llama esto
     * (normalmente justo después, vía SyncScheduler.syncNow()).
     */
    suspend fun registrarEscaneo(recepcionId: Int, detalle: RecepcionDetalleDto?) {
        confirmacionDao.upsert(
            ConfirmacionLocalEntity(
                recepcionId = recepcionId,
                scannedAtEpochMillis = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING,
                serverConfirmedAt = null,
                lastError = null,
                productoNombre = detalle?.productoNombre,
                variedadNombre = detalle?.variedadNombre,
                colorHex = detalle?.colorHex,
                fincaNombre = detalle?.fincaNombre,
                bloqueNombre = detalle?.bloqueNombre,
                tallos = detalle?.tallos,
            ),
        )
    }

    /**
     * Envía cada confirmación pendiente/fallida al servidor. Un request por
     * recepción para que una sola falle sin bloquear las demás. Idempotente
     * por recepcionId — reintentos (periodicos, por conectividad, o manual
     * "sincronizar ahora") nunca duplican el evento "confirmado" en el
     * historial del servidor.
     */
    suspend fun syncPending(): SyncOutcome {
        val pending = confirmacionDao.findPending()
        var synced = 0
        var failed = 0

        for (item in pending) {
            try {
                val result = apiService.confirmarRecepcion(item.recepcionId)
                confirmacionDao.updateSyncResult(item.recepcionId, SyncStatus.SYNCED, result.confirmedAt, null)
                synced++
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    // Código de barras inválido / recepción borrada: no tiene
                    // sentido reintentar, se marca como error visible.
                    confirmacionDao.updateSyncResult(item.recepcionId, SyncStatus.FAILED, null, "Recepción no encontrada en el servidor.")
                    failed++
                } else {
                    confirmacionDao.updateSyncResult(item.recepcionId, SyncStatus.PENDING, null, e.message())
                }
            } catch (e: Exception) {
                // Red/servidor no disponible: se deja PENDING (no FAILED)
                // para que el próximo intento (periódico o por conectividad)
                // lo reintente solo.
                confirmacionDao.updateSyncResult(item.recepcionId, SyncStatus.PENDING, null, e.message)
            }
        }

        return SyncOutcome(synced = synced, failed = failed, remaining = pending.size - synced - failed)
    }
}

data class SyncOutcome(val synced: Int, val failed: Int, val remaining: Int)
