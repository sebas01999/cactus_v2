package com.phytotec.recepcion.ui.bienvenida

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phytotec.recepcion.data.repository.ConfirmacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BienvenidaViewModel @Inject constructor(
    confirmacionRepository: ConfirmacionRepository,
) : ViewModel() {

    val pendingCount: StateFlow<Int> =
        confirmacionRepository.observePendingCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
