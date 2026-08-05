package com.phytotec.recepcion.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus { PENDING, SYNCED, FAILED }

/**
 * Un escaneo de código de barras hecho en el celular. recepcionId es el id
 * numérico que trae el código (el mismo id de la Recepcion en el servidor,
 * generado al imprimir la etiqueta en la web) — es a la vez la llave local
 * y la garantía de idempotencia: escanear la misma caja varias veces
 * (a propósito o por un reintento offline) nunca duplica nada, porque tanto
 * aquí como en el servidor "confirmar" es una operación de una sola vez por
 * recepcionId.
 */
@Entity(tableName = "confirmaciones_local")
data class ConfirmacionLocalEntity(
    @PrimaryKey val recepcionId: Int,
    val scannedAtEpochMillis: Long,
    val syncStatus: SyncStatus,
    /** Fecha ISO que confirmó el servidor, una vez sincronizado. */
    val serverConfirmedAt: String?,
    /** Set cuando syncStatus == FAILED, para no fallar en silencio. */
    val lastError: String?,
    // Datos legibles cacheados de la vista previa al escanear (si hubo
    // conexión en ese momento) — puramente para mostrar en el historial;
    // null si se escaneó sin conexión, la confirmación igual funciona.
    val productoNombre: String?,
    val variedadNombre: String?,
    val colorHex: String?,
    val fincaNombre: String?,
    val bloqueNombre: String?,
    val tallos: Int?,
)
