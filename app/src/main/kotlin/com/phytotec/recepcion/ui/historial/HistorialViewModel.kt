package com.phytotec.recepcion.ui.historial

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phytotec.recepcion.data.local.entities.ConfirmacionLocalEntity
import com.phytotec.recepcion.data.repository.ConfirmacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistorialUiState(val sincronizando: Boolean = false, val mensaje: String? = null)

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val confirmacionRepository: ConfirmacionRepository,
) : ViewModel() {

    val historial: StateFlow<List<ConfirmacionLocalEntity>> =
        confirmacionRepository.observeHistorial().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> =
        confirmacionRepository.observePendingCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    var uiState by mutableStateOf(HistorialUiState())
        private set

    /**
     * Corre la sincronización directo (no vía WorkManager) para que el
     * botón "Sincronizar" dé una respuesta inmediata en pantalla — antes
     * solo encolaba trabajo en segundo plano y nunca avisaba si terminó ni
     * qué pasó.
     */
    fun syncNow() {
        if (uiState.sincronizando) {
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(sincronizando = true, mensaje = null)
            val outcome = confirmacionRepository.syncPending()

            val mensaje = when {
                outcome.synced == 0 && outcome.failed == 0 && outcome.remaining == 0 -> "No había nada pendiente por sincronizar."
                outcome.remaining > 0 -> "Sin conexión: se reintentará solo más tarde."
                outcome.failed > 0 && outcome.synced == 0 -> "No se pudo sincronizar: revisa los errores marcados abajo."
                outcome.failed > 0 -> "${outcome.synced} sincronizada(s). ${outcome.failed} con error — revisa abajo."
                else -> "${outcome.synced} recepción(es) sincronizada(s)."
            }

            uiState = HistorialUiState(sincronizando = false, mensaje = mensaje)
        }
    }

    fun dismissMensaje() {
        uiState = uiState.copy(mensaje = null)
    }
}
