package com.phytotec.recepcion.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un escaneo de "esta caja se le entregó a este empleado" hecho en el
 * celular, durante el flujo de Clasificación. recepcionId es la llave
 * local: si la misma caja se reasigna (se escanea de nuevo bajo otro
 * empleado) antes de sincronizar, esta fila simplemente se sobrescribe con
 * el empleado más reciente — igual que en el servidor (ver
 * RecepcionEventLogger::asignar(), que siempre sobrescribe y registra un
 * evento nuevo).
 */
@Entity(tableName = "asignaciones_local")
data class AsignacionLocalEntity(
    @PrimaryKey val recepcionId: Int,
    val empleadoId: Int,
    val empleadoNombre: String?,
    val scannedAtEpochMillis: Long,
    val syncStatus: SyncStatus,
    val serverAsignadoEn: String?,
    val lastError: String?,
    // Datos legibles cacheados de la vista previa al escanear la caja (si
    // hubo conexión) — solo para mostrar en el historial.
    val productoNombre: String?,
    val variedadNombre: String?,
    val colorHex: String?,
    val fincaNombre: String?,
    val bloqueNombre: String?,
    val tallos: Int?,
)
