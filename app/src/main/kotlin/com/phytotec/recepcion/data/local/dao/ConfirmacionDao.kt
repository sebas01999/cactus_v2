package com.phytotec.recepcion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phytotec.recepcion.data.local.entities.ConfirmacionLocalEntity
import com.phytotec.recepcion.data.local.entities.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfirmacionDao {

    /** REPLACE: volver a escanear la misma caja simplemente refresca la fila existente. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(confirmacion: ConfirmacionLocalEntity)

    /**
     * Solo syncStatus = PENDING, NO 'FAILED'. Un FAILED es un error terminal
     * (p. ej. 404: el id escaneado no existe en el servidor) — reintentarlo
     * para siempre no lo va a arreglar, solo hace que "Sincronizar" parezca
     * que no hace nada.
     */
    @Query("SELECT * FROM confirmaciones_local WHERE syncStatus = 'PENDING' ORDER BY scannedAtEpochMillis ASC")
    suspend fun findPending(): List<ConfirmacionLocalEntity>

    /** Mismo criterio que findPending(): solo cuenta lo que "Sincronizar" puede resolver. */
    @Query("SELECT COUNT(*) FROM confirmaciones_local WHERE syncStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM confirmaciones_local ORDER BY scannedAtEpochMillis DESC")
    fun observeHistorial(): Flow<List<ConfirmacionLocalEntity>>

    @Query("SELECT * FROM confirmaciones_local WHERE recepcionId = :recepcionId")
    suspend fun find(recepcionId: Int): ConfirmacionLocalEntity?

    @Query("UPDATE confirmaciones_local SET syncStatus = :status, serverConfirmedAt = :serverConfirmedAt, lastError = :error WHERE recepcionId = :recepcionId")
    suspend fun updateSyncResult(recepcionId: Int, status: SyncStatus, serverConfirmedAt: String?, error: String?)
}
