package com.phytotec.recepcion.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private const val ZEBRA_SCAN_ACTION = "com.phytotec.recepcion.SCAN"
private const val ZEBRA_SCAN_DATA_EXTRA = "com.symbol.datawedge.data_string"

/**
 * Campo de captura para el lector Zebra, compartido entre Escanear y
 * Clasificación. Funciona de dos formas a la vez:
 * - **Modo teclado**: el lector "escribe" el código en el campo enfocado y
 *   manda Enter — se procesa apenas para de teclear (debounce corto) o al
 *   ver el Enter.
 * - **DataWedge (broadcast)**: si el lector está configurado para mandar un
 *   Intent en vez de simular teclado, se escucha la acción
 *   `com.phytotec.recepcion.SCAN`.
 *
 * No usa cámara — ver README del proyecto.
 */
@Composable
fun ZebraScanInput(
    onCodigoLeido: (String) -> Unit,
    enabled: Boolean = true,
    label: String = "Código leído por Zebra",
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var codigoEntrada by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    fun procesarCodigo(codigo: String) {
        val limpio = codigo.trim()
        if (limpio.isNotEmpty()) {
            codigoEntrada = ""
            onCodigoLeido(limpio)
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

    OutlinedTextField(
        value = codigoEntrada,
        onValueChange = { nuevoValor ->
            codigoEntrada = nuevoValor.replace("\n", "").replace("\r", "")
        },
        label = { androidx.compose.material3.Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                    procesarCodigo(codigoEntrada)
                    true
                } else {
                    false
                }
            },
    )
}
