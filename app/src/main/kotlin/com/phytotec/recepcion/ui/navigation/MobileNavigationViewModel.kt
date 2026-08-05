package com.phytotec.recepcion.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phytotec.recepcion.data.remote.MobileNavItemDto
import com.phytotec.recepcion.data.repository.MobileNavigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MobileNavigationUiState(
    val loading: Boolean = true,
    val items: List<MobileNavItemDto> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class MobileNavigationViewModel @Inject constructor(
    private val mobileNavigationRepository: MobileNavigationRepository,
) : ViewModel() {

    var uiState by mutableStateOf(MobileNavigationUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)

            runCatching {
                mobileNavigationRepository.loadNavigation()
            }.onSuccess { items ->
                uiState = MobileNavigationUiState(loading = false, items = items)
            }.onFailure { error ->
                uiState = MobileNavigationUiState(
                    loading = false,
                    items = emptyList(),
                    error = error.message ?: "No se pudo cargar el menú.",
                )
            }
        }
    }
}
