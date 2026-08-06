package com.phytotec.recepcion.ui.clasificacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phytotec.recepcion.ui.components.ZebraScanInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClasificacionScreen(
    viewModel: ClasificacionViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    Scaffold(topBar = { TopAppBar(title = { Text("Clasificación") }) }) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Escanea primero el carnet del empleado, luego cada caja que le entregues. " +
                    "Escanear otro carnet cambia de empleado sin pasos extra.",
            )

            ZebraScanInput(
                onCodigoLeido = viewModel::onCodigoLeido,
                enabled = !state.procesando,
                label = "Carnet o caja",
            )

            EmpleadoActivoCard(empleado = state.empleadoActivo, cajasEnLaSesion = state.cajasDeLaSesion.size)

            if (state.procesando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            state.mensaje?.let { mensaje ->
                Text(
                    mensaje,
                    color = if (state.esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }

            if (state.cajasDeLaSesion.isNotEmpty()) {
                Text("Cajas asignadas en esta sesión:", style = MaterialTheme.typography.labelLarge)
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.cajasDeLaSesion.reversed()) { recepcionId ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Caja #$recepcionId", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmpleadoActivoCard(empleado: EmpleadoActivo?, cajasEnLaSesion: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (empleado != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AssignmentInd, contentDescription = null)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("Empleado activo", style = MaterialTheme.typography.labelMedium)
                Text(
                    empleado?.nombre ?: empleado?.let { "#${it.id}" } ?: "Ninguno — escanea un carnet",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (empleado != null) {
                    Text("$cajasEnLaSesion caja(s) asignada(s) en esta sesión", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
