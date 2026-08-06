package com.phytotec.recepcion.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phytotec.recepcion.data.remote.MobileNavItemDto
import com.phytotec.recepcion.data.repository.AuthRepository
import com.phytotec.recepcion.ui.ajustes.AjustesScreen
import com.phytotec.recepcion.ui.bienvenida.BienvenidaScreen
import com.phytotec.recepcion.ui.escanear.EscanearScreen
import com.phytotec.recepcion.ui.historial.HistorialScreen
import com.phytotec.recepcion.ui.login.LoginScreen
import kotlinx.coroutines.launch

private object Routes {
    const val LOGIN = "login"
    const val BIENVENIDA = "bienvenida"
    const val ESCANEAR = "escanear"
    const val HISTORIAL = "historial"
    const val AJUSTES = "ajustes"
}

@Composable
fun RecepcionNavHost(authRepository: AuthRepository) {
    val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = false)

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = {})
    } else {
        MainShell(authRepository = authRepository)
    }
}

@Composable
private fun MainShell(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navViewModel: MobileNavigationViewModel = hiltViewModel()
    val navState = navViewModel.uiState
    val userName by authRepository.userName.collectAsState(initial = null)
    val userRoles by authRepository.userRoles.collectAsState(initial = emptyList())
    val menuItems = when {
        navState.items.isNotEmpty() -> navState.items
        navState.error != null -> fallbackItems(userRoles)
        else -> emptyList()
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.BIENVENIDA

    fun open(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun logout() {
        coroutineScope.launch {
            drawerState.close()
            authRepository.logout()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .padding(vertical = 8.dp),
            ) {
                DrawerHeader(userName = userName, roles = userRoles)

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (navState.loading && menuItems.isEmpty()) {
                        Text(
                            text = "Cargando menú...",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }

                    menuItems.forEach { item ->
                        DrawerItem(
                            item = item,
                            currentRoute = currentRoute,
                            onNavigate = { route -> open(route) },
                        )
                    }

                    if (navState.error != null) {
                        Text(
                            text = navState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }

                    Divider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), color = Color(0xFFE5E7EB))

                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        icon = { Icon(Icons.Filled.ExitToApp, contentDescription = null) },
                        selected = false,
                        onClick = { logout() },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    )
                }
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.BIENVENIDA,
            ) {
                composable(Routes.BIENVENIDA) {
                    BienvenidaScreen(userName = userName)
                }
                composable(Routes.ESCANEAR) {
                    EscanearScreen()
                }
                composable(Routes.HISTORIAL) {
                    HistorialScreen()
                }
                composable(Routes.AJUSTES) {
                    AjustesScreen()
                }
            }

            FilledTonalIconButton(
                onClick = { coroutineScope.launch { drawerState.open() } },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
            }
        }
    }
}

@Composable
private fun DrawerHeader(userName: String?, roles: List<String>) {
    val primaryRole = when {
        roles.contains("ROLE_ADMIN") -> "Administrador"
        roles.contains("ROLE_MOBILE_APP") -> "Operario"
        roles.isNotEmpty() -> roles.first().removePrefix("ROLE_").replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        else -> "Usuario"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
                ),
            )
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = userName ?: "Phytotec SAS",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = primaryRole,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Administrador",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    item: MobileNavItemDto,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    indent: Int = 0,
) {
    val hasChildren = item.children.isNotEmpty()
    val route = item.path?.trim('/')
    val selected = route != null && currentRoute == route
    val icon = iconFor(item.icon)
    val background = if (selected) Color(0xFFEAF4EA) else Color.Transparent
    val textColor = if (selected) Color(0xFF2E7D32) else Color(0xFF374151)

    Column {
        if (route != null) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = background,
                modifier = Modifier
                    .padding(start = (indent * 12).dp, end = 4.dp)
                    .fillMaxWidth(),
            ) {
                NavigationDrawerItem(
                    label = { Text(item.name, color = textColor) },
                    icon = { Icon(icon, contentDescription = null, tint = textColor) },
                    selected = selected,
                    onClick = { onNavigate(route) },
                    shape = MaterialTheme.shapes.medium,
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent,
                        selectedIconColor = Color(0xFF2E7D32),
                        unselectedIconColor = Color(0xFF6B7280),
                        selectedTextColor = Color(0xFF2E7D32),
                        unselectedTextColor = Color(0xFF374151),
                    ),
                )
            }
        } else {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = background,
                modifier = Modifier
                    .padding(start = (indent * 12).dp, end = 4.dp)
                    .fillMaxWidth(),
            ) {
                NavigationDrawerItem(
                    label = { Text(item.name, color = textColor) },
                    icon = { Icon(icon, contentDescription = null, tint = textColor) },
                    selected = false,
                    onClick = {},
                    shape = MaterialTheme.shapes.medium,
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.Transparent,
                        unselectedContainerColor = Color.Transparent,
                        selectedIconColor = Color(0xFF2E7D32),
                        unselectedIconColor = Color(0xFF6B7280),
                        selectedTextColor = Color(0xFF2E7D32),
                        unselectedTextColor = Color(0xFF374151),
                    ),
                )
            }
        }

        if (hasChildren) {
            item.children.forEach { child ->
                DrawerItem(
                    item = child,
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    indent = indent + 1,
                )
            }
        }
    }
}

private fun iconFor(code: String?): ImageVector = when (code?.lowercase()) {
    "home" -> Icons.Filled.Home
    "scan", "qrcode", "qrcode_scanner" -> Icons.Filled.QrCodeScanner
    "history", "list" -> Icons.Filled.History
    "settings", "cog" -> Icons.Filled.Settings
    "folder" -> Icons.Filled.Folder
    "user", "person" -> Icons.Filled.Person
    else -> Icons.Filled.Folder
}

private fun fallbackItems(roles: List<String>): List<MobileNavItemDto> {
    val canUseApp = roles.contains("ROLE_MOBILE_APP") || roles.contains("ROLE_ADMIN")
    val items = mutableListOf<MobileNavItemDto>()

    items.add(MobileNavItemDto("Inicio", "home", Routes.BIENVENIDA, emptyList()))

    if (canUseApp) {
        items.add(MobileNavItemDto("Escanear", "scan", Routes.ESCANEAR, emptyList()))
        items.add(MobileNavItemDto("Historial", "history", Routes.HISTORIAL, emptyList()))
    }

    if (roles.contains("ROLE_ADMIN")) {
        items.add(MobileNavItemDto("Ajustes", "settings", Routes.AJUSTES, emptyList()))
    }

    return items
}
