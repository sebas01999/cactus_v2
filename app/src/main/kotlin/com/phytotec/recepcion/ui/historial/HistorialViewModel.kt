package com.phytotec.recepcion.ui.historial

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phytotec.recepcion.data.local.entities.ConfirmacionLocalEntity
import com.phytotec.recepcion.data.repository.ConfirmacionRepository
import com.phytotec.recepcion.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistorialUiState(val mensaje: String? = null)

@HiltViewModel
class HistorialViewModel @Inject constructor(
    confirmacionRepository: ConfirmacionRepository,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    val historial: StateFlow<List<ConfirmacionLocalEntity>> =
        confirmacionRepository.observeHistorial().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> =
        confirmacionRepository.observePendingCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    var uiState by mutableStateOf(HistorialUiState())
        private set

    fun syncNow() {
        syncScheduler.syncNow()
        uiState = uiState.copy(mensaje = "Sincronizando en segundo plano...")
    }

    fun dismissMensaje() {
        uiState = uiState.copy(mensaje = null)
    }
}
