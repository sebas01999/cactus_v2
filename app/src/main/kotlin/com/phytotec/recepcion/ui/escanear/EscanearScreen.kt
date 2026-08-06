package com.phytotec.recepcion.ui.escanear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

private const val ZEBRA_SCAN_ACTION = "com.phytotec.recepcion.SCAN"
private const val ZEBRA_SCAN_DATA_EXTRA = "com.symbol.datawedge.data_string"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanearScreen(
    viewModel: EscanearViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    var codigoEntrada by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    fun procesarCodigo(codigo: String) {
        val limpio = codigo.trim()
        if (limpio.isNotEmpty()) {
            codigoEntrada = ""
            viewModel.procesarCodigoLeido(limpio)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(codigoEntrada) {
        val limpio = codigoEntrada.trim()
        if (limpio.isNotEmpty()) {
            delay(180)
            if (codigoEntrada.trim() == limpio) {
                procesarCodigo(limpio)
            }
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val codigo = intent?.getStringExtra(ZEBRA_SCAN_DATA_EXTRA)
                if (!codigo.isNullOrBlank()) {
                    procesarCodigo(codigo)
                }
            }
        }

        val filter = IntentFilter(ZEBRA_SCAN_ACTION)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

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

            OutlinedTextField(
                value = codigoEntrada,
                onValueChange = { nuevoValor ->
                    codigoEntrada = nuevoValor.replace("\n", "").replace("\r", "")
                },
                label = { Text("Código leído por Zebra") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { event ->
                        if (
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP &&
                            event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER
                        ) {
                            procesarCodigo(codigoEntrada)
                            true
                        } else {
                            false
                        }
                    },
            )

            Button(
                onClick = { procesarCodigo(codigoEntrada) },
                enabled = codigoEntrada.isNotBlank() && !state.procesando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Procesar código")
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
