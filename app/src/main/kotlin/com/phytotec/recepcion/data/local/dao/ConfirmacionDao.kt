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

    @Query("SELECT * FROM confirmaciones_local WHERE syncStatus != 'SYNCED' ORDER BY scannedAtEpochMillis ASC")
    suspend fun findPending(): List<ConfirmacionLocalEntity>

    @Query("SELECT COUNT(*) FROM confirmaciones_local WHERE syncStatus != 'SYNCED'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM confirmaciones_local ORDER BY scannedAtEpochMillis DESC")
    fun observeHistorial(): Flow<List<ConfirmacionLocalEntity>>

    @Query("SELECT * FROM confirmaciones_local WHERE recepcionId = :recepcionId")
    suspend fun find(recepcionId: Int): ConfirmacionLocalEntity?

    @Query("UPDATE confirmaciones_local SET syncStatus = :status, serverConfirmedAt = :serverConfirmedAt, lastError = :error WHERE recepcionId = :recepcionId")
    suspend fun updateSyncResult(recepcionId: Int, status: SyncStatus, serverConfirmedAt: String?, error: String?)
}
