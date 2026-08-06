package com.phytotec.recepcion.ui.clasificacion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phytotec.recepcion.data.repository.AsignacionRepository
import com.phytotec.recepcion.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private val CODIGO_EMPLEADO = Regex("^EMP(\\d+)$", RegexOption.IGNORE_CASE)

data class EmpleadoActivo(val id: Int, val nombre: String?)

data class ClasificacionUiState(
    val empleadoActivo: EmpleadoActivo? = null,
    /** Ids de cajas asignadas al empleado activo en esta sesión — solo feedback visual, no persiste. */
    val cajasDeLaSesion: List<Int> = emptyList(),
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false,
)

@HiltViewModel
class ClasificacionViewModel @Inject constructor(
    private val asignacionRepository: AsignacionRepository,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    var uiState by mutableStateOf(ClasificacionUiState())
        private set

    /**
     * Un código puede ser el carnet de un empleado (EMP123, cambia el
     * contexto activo sin pasos extra) o una caja (número plano, se
     * asigna al empleado activo). Cualquier otra cosa es inválida.
     */
    fun onCodigoLeido(codigo: String) {
        val matchEmpleado = CODIGO_EMPLEADO.matchEntire(codigo.trim())
        if (matchEmpleado != null) {
            activarEmpleado(matchEmpleado.groupValues[1].toInt())
            return
        }

        val recepcionId = codigo.trim().toIntOrNull()
        if (recepcionId == null) {
            uiState = uiState.copy(mensaje = "Código inválido: \"$codigo\" no es un carnet ni una caja.", esError = true)
            return
        }

        val empleado = uiState.empleadoActivo
        if (empleado == null) {
            uiState = uiState.copy(mensaje = "Primero escanea el carnet de un empleado.", esError = true)
            return
        }

        asignarCaja(recepcionId, empleado)
    }

    /**
     * A diferencia de una caja (que se guarda offline-first sin esperar
     * respuesta), activar un empleado sí espera la consulta al servidor:
     * hace falta saber su sesionEstado para decidir si se puede activar o
     * hay que rechazarlo con un error — ese es justo el control que se
     * pidió ("debe darle un error si no tiene el tiempo iniciado").
     */
    private fun activarEmpleado(empleadoId: Int) {
        uiState = uiState.copy(mensaje = null, procesando = true)

        viewModelScope.launch {
            val detalle = asignacionRepository.lookupEmpleado(empleadoId)
            uiState = uiState.copy(procesando = false)

            if (detalle == null) {
                // Sin conexión: no se puede verificar la jornada ahora. Se
                // activa igual con el id nomás — el servidor es quien
                // manda al final, y rechazará las cajas al sincronizar si
                // de verdad no tenía el tiempo iniciado.
                uiState = uiState.copy(
                    empleadoActivo = EmpleadoActivo(id = empleadoId, nombre = null),
                    cajasDeLaSesion = emptyList(),
                )
                return@launch
            }

            if (detalle.sesionEstado != "activa") {
                uiState = uiState.copy(
                    empleadoActivo = null,
                    mensaje = mensajeParaEstado(detalle.nombre, detalle.sesionEstado),
                    esError = true,
                )
                return@launch
            }

            uiState = uiState.copy(
                empleadoActivo = EmpleadoActivo(id = empleadoId, nombre = detalle.nombre),
                cajasDeLaSesion = emptyList(),
            )
        }
    }

    private fun mensajeParaEstado(nombre: String, estado: String): String = when (estado) {
        "sin_iniciar" -> "$nombre no tiene el tiempo iniciado. Pide que le inicien la jornada en Rendimientos."
        "pausada" -> "$nombre está en pausa ahora mismo."
        "finalizada" -> "La jornada de $nombre ya terminó por hoy."
        else -> "$nombre no puede recibir cajas en este momento."
    }

    private fun asignarCaja(recepcionId: Int, empleado: EmpleadoActivo) {
        viewModelScope.launch {
            uiState = uiState.copy(procesando = true, mensaje = null)

            val detalle = asignacionRepository.lookupCaja(recepcionId)
            asignacionRepository.registrarEscaneo(recepcionId, empleado.id, empleado.nombre, detalle)
            syncScheduler.syncNow()

            uiState = uiState.copy(
                procesando = false,
                cajasDeLaSesion = uiState.cajasDeLaSesion + recepcionId,
                mensaje = "Caja #$recepcionId asignada" + (empleado.nombre?.let { " a $it" } ?: "") + ".",
                esError = false,
            )
        }
    }

    fun limpiarMensaje() {
        uiState = uiState.copy(mensaje = null)
    }
}
