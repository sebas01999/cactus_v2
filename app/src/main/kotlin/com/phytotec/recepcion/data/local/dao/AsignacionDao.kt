package com.phytotec.recepcion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phytotec.recepcion.data.local.entities.AsignacionLocalEntity
import com.phytotec.recepcion.data.local.entities.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignacionDao {

    /** REPLACE: reasignar la misma caja antes de sincronizar solo refresca la fila. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(asignacion: AsignacionLocalEntity)

    /** Solo PENDING — un FAILED es terminal (ver ConfirmacionDao, mismo criterio). */
    @Query("SELECT * FROM asignaciones_local WHERE syncStatus = 'PENDING' ORDER BY scannedAtEpochMillis ASC")
    suspend fun findPending(): List<AsignacionLocalEntity>

    @Query("SELECT COUNT(*) FROM asignaciones_local WHERE syncStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM asignaciones_local ORDER BY scannedAtEpochMillis DESC")
    fun observeHistorial(): Flow<List<AsignacionLocalEntity>>

    @Query("UPDATE asignaciones_local SET syncStatus = :status, serverAsignadoEn = :serverAsignadoEn, lastError = :error WHERE recepcionId = :recepcionId")
    suspend fun updateSyncResult(recepcionId: Int, status: SyncStatus, serverAsignadoEn: String?, error: String?)
}
