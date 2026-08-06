package com.phytotec.recepcion.ui.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phytotec.recepcion.data.local.entities.ConfirmacionLocalEntity
import com.phytotec.recepcion.data.local.entities.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = hiltViewModel(),
) {
    val historial by viewModel.historial.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de escaneos") },
                actions = {
                    BadgedBox(
                        badge = { if (pendingCount > 0) Badge { Text("$pendingCount") } },
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        if (state.sincronizando) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Text("Sincronizando...", modifier = Modifier.padding(start = 8.dp))
                            }
                        } else {
                            TextButton(onClick = viewModel::syncNow) { Text("Sincronizar") }
                        }
                    }
                },
            )
        },
    ) { padding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            state.mensaje?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))
            }

            if (historial.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Todavía no has escaneado ninguna recepción.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(historial, key = { it.recepcionId }) { confirmacion ->
                        ConfirmacionRow(confirmacion)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmacionRow(confirmacion: ConfirmacionLocalEntity) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                confirmacion.colorHex?.let { hex ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(12.dp)
                            .background(color = parseHexColor(hex), shape = RoundedCornerShape(50)),
                    )
                }
                Text(
                    "#${confirmacion.recepcionId}" + (confirmacion.variedadNombre?.let { " · ${confirmacion.productoNombre} — $it" } ?: ""),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                SyncBadge(confirmacion.syncStatus)
            }

            if (confirmacion.fincaNombre != null && confirmacion.bloqueNombre != null) {
                Text("${confirmacion.fincaNombre} / ${confirmacion.bloqueNombre}", style = MaterialTheme.typography.bodySmall)
            }
            confirmacion.tallos?.let {
                Text("Tallos: $it", style = MaterialTheme.typography.bodySmall)
            }
            Text("Escaneado: ${formatDate(confirmacion.scannedAtEpochMillis)}", style = MaterialTheme.typography.bodySmall)

            confirmacion.lastError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SyncBadge(status: SyncStatus) {
    val (text, color) = when (status) {
        SyncStatus.SYNCED -> "Confirmado" to MaterialTheme.colorScheme.primary
        SyncStatus.PENDING -> "Pendiente de sync" to MaterialTheme.colorScheme.tertiary
        SyncStatus.FAILED -> "Error" to MaterialTheme.colorScheme.error
    }
    Text(text, color = color, style = MaterialTheme.typography.labelMedium)
}

private fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color.Gray
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(epochMillis))
