package com.phytotec.recepcion.ui.escanear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phytotec.recepcion.ui.components.ZebraScanInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanearScreen(
    viewModel: EscanearViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState
    var ultimoCodigo by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Escanear recepción") }) }) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Usa el escáner Zebra para leer el código de barras de la etiqueta. La app procesa la recepción al instante, sin cámara.",
            )

            ZebraScanInput(
                onCodigoLeido = { codigo ->
                    ultimoCodigo = codigo
                    viewModel.procesarCodigoLeido(codigo)
                },
                enabled = !state.procesando,
            )

            Button(
                onClick = { if (ultimoCodigo.isNotBlank()) viewModel.procesarCodigoLeido(ultimoCodigo) },
                enabled = ultimoCodigo.isNotBlank() && !state.procesando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Procesar de nuevo")
            }

            if (state.procesando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            state.mensaje?.let { mensaje ->
                Text(
                    mensaje,
                    color = if (state.esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
