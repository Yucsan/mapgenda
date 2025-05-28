package com.yucsan.mapgendafernandochang2025.screen.lugares

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel

@Composable
fun PantallaExplorarTabsCompose(
    navController: NavController,
    lugarViewModel: LugarViewModel,
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel,
    ubicacionViewModel: UbicacionViewModel
) {
    var tabSeleccionado by remember { mutableStateOf(0) }
    val tabs = listOf("Zonas", "Ubicaciones")

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior con título y volver
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Explora lugares",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Tabs
        TabRow(selectedTabIndex = tabSeleccionado) {
            tabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick = { tabSeleccionado = index },
                    text = { Text(titulo) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Contenido de cada tab
        when (tabSeleccionado) {
            0 -> PantallaZonasCompose(
                lugarViewModel = lugarViewModel,
                ubicacionViewModel = ubicacionViewModel,
                navController = navController
            )
            1 -> PantallaUbicacionesListadoCompose(
                lugarRutaOfflineViewModel = lugarRutaOfflineViewModel,
                ubicacionViewModel = ubicacionViewModel
            )
        }
    }
}
