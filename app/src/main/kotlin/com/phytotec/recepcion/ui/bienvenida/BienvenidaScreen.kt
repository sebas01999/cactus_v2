package com.phytotec.recepcion.ui.bienvenida

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Primera pantalla al iniciar sesión. Puramente de bienvenida — las
 * acciones (escanear, historial) viven en el menú lateral (drawer), no
 * aquí, para no duplicar la navegación.
 */
@Composable
fun BienvenidaScreen(
    userName: String?,
    viewModel: BienvenidaViewModel = hiltViewModel(),
) {
    val pendingCount by viewModel.pendingCount.collectAsState()
    val hoy = remember {
        SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
                    ),
                )
                .padding(24.dp),
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    hoy,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Hola" + (userName?.let { ", $it" } ?: ""),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Bienvenido de nuevo a Phytotec Poscosecha",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Usa el menú (arriba a la izquierda) para escanear una recepción o revisar tu historial.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (pendingCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (pendingCount == 1) {
                                "1 recepción pendiente de sincronizar."
                            } else {
                                "$pendingCount recepciones pendientes de sincronizar."
                            },
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
