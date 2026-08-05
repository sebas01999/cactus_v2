package com.phytotec.recepcion.ui.escanear

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phytotec.recepcion.data.repository.ConfirmacionRepository
import com.phytotec.recepcion.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EscanearUiState(
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false,
)

@HiltViewModel
class EscanearViewModel @Inject constructor(
    private val confirmacionRepository: ConfirmacionRepository,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    var uiState by mutableStateOf(EscanearUiState())
        private set

    fun procesarCodigoLeido(codigo: String) {
        val recepcionId = codigo.trim().toIntOrNull()
        if (recepcionId == null) {
            uiState = uiState.copy(
                mensaje = "Código inválido: \"$codigo\" no es un número de recepción.",
                esError = true,
            )
            return
        }

        if (uiState.procesando) {
            return
        }

        uiState = uiState.copy(procesando = true, mensaje = null, esError = false)

        viewModelScope.launch {
            val detalle = confirmacionRepository.lookupDetalle(recepcionId)
            confirmacionRepository.registrarEscaneo(recepcionId, detalle)
            syncScheduler.syncNow()

            uiState = uiState.copy(
                procesando = false,
                mensaje = "Recepción #$recepcionId registrada en el dispositivo.",
                esError = false,
            )
        }
    }

    fun limpiarMensaje() {
        uiState = uiState.copy(mensaje = null, esError = false)
    }
}
