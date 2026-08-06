package com.phytotec.recepcion.data.repository

import com.phytotec.recepcion.data.local.dao.AsignacionDao
import com.phytotec.recepcion.data.local.entities.AsignacionLocalEntity
import com.phytotec.recepcion.data.local.entities.SyncStatus
import com.phytotec.recepcion.data.remote.ApiService
import com.phytotec.recepcion.data.remote.AsignarRequest
import com.phytotec.recepcion.data.remote.EmpleadoDetalleDto
import com.phytotec.recepcion.data.remote.RecepcionDetalleDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsignacionRepository @Inject constructor(
    private val apiService: ApiService,
    private val asignacionDao: AsignacionDao,
) {
    fun observeHistorial(): Flow<List<AsignacionLocalEntity>> = asignacionDao.observeHistorial()

    fun observePendingCount(): Flow<Int> = asignacionDao.observePendingCount()

    /** Best-effort: nombre del empleado para mostrar como "activo" al escanear su carnet. */
    suspend fun lookupEmpleado(empleadoId: Int): EmpleadoDetalleDto? =
        try {
            apiService.getEmpleado(empleadoId)
        } catch (e: Exception) {
            null
        }

    /** Best-effort: datos legibles de la caja para la vista previa (igual que en Escanear). */
    suspend fun lookupCaja(recepcionId: Int): RecepcionDetalleDto? =
        try {
            apiService.getRecepcion(recepcionId)
        } catch (e: Exception) {
            null
        }

    /** Guarda la asignación localmente y devuelve — nunca depende de la red. */
    suspend fun registrarEscaneo(recepcionId: Int, empleadoId: Int, empleadoNombre: String?, detalle: RecepcionDetalleDto?) {
        asignacionDao.upsert(
            AsignacionLocalEntity(
                recepcionId = recepcionId,
                empleadoId = empleadoId,
                empleadoNombre = empleadoNombre,
                scannedAtEpochMillis = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING,
                serverAsignadoEn = null,
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
     * Mismo patrón que ConfirmacionRepository.syncPending(): un request por
     * asignación, 404 es error terminal (caja borrada o todavía no
     * confirmada — ver más abajo), cualquier otro fallo se deja PENDING
     * para reintentar solo.
     */
    suspend fun syncPending(): SyncOutcome {
        val pending = asignacionDao.findPending()
        var synced = 0
        var failed = 0

        for (item in pending) {
            try {
                val result = apiService.asignarRecepcion(item.recepcionId, AsignarRequest(item.empleadoId))
                asignacionDao.updateSyncResult(item.recepcionId, SyncStatus.SYNCED, result.asignadoEn, null)
                synced++
            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    404 -> {
                        asignacionDao.updateSyncResult(item.recepcionId, SyncStatus.FAILED, null, "Recepción no encontrada en el servidor.")
                        failed++
                    }
                    409 -> {
                        // La caja todavía no está confirmada en poscosecha —
                        // error terminal por ahora (reintentar no la va a
                        // confirmar sola); el operario ve el error y decide.
                        asignacionDao.updateSyncResult(item.recepcionId, SyncStatus.FAILED, null, "Esta caja todavía no ha sido confirmada en poscosecha.")
                        failed++
                    }
                    else -> asignacionDao.updateSyncResult(item.recepcionId, SyncStatus.PENDING, null, e.message())
                }
            } catch (e: Exception) {
                asignacionDao.updateSyncResult(item.recepcionId, SyncStatus.PENDING, null, e.message)
            }
        }

        return SyncOutcome(synced = synced, failed = failed, remaining = pending.size - synced - failed)
    }
}
