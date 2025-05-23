package com.yucsan.mapgendafernandochang2025.componentes.navegacion


import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yucsan.mapgendafernandochang2025.util.state.AuthState
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState



@Composable
fun BottomNavigationBar(navController: NavController, authState: StateFlow<AuthState>) {
    val items = listOf(
        BottomBarScreen.Mapa,
        BottomBarScreen.MenuOffline,
        BottomBarScreen.Descargas,
        BottomBarScreen.PerfilTabs
    )

    val estado by authState.collectAsState()
    val sesionActiva = estado is AuthState.Autenticado

    NavigationBar(
        modifier = Modifier.navigationBarsPadding()
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.height(30.dp)
                    )
                },
                label = { Text(text = screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (sesionActiva && currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                enabled = sesionActiva
            )
        }
    }
}

