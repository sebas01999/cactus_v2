package com.phytotec.recepcion.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String?,
    onEscanearClick: () -> Unit,
    onHistorialClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) },
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Hola${userName?.let { ", $it" } ?: ""}",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Usa el menú lateral para entrar al flujo de recepción o revisar el historial.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Recepción rápida", style = MaterialTheme.typography.titleMedium)
                    Text("Escanea el código de barras de la caja para registrar la llegada.")
                    Button(onClick = onEscanearClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Ir a escanear")
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Historial", style = MaterialTheme.typography.titleMedium)
                    Text("Revisa lo que ya se escaneó y su estado de sincronización.")
                    Button(onClick = onHistorialClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Abrir historial")
                    }
                }
            }
        }
    }
}
