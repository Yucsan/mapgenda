package com.yucsan.mapgendafernandochang2025.screen.offLine


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel


@Composable
fun PantallaTabsOffline(
    lugarViewModel: LugarViewModel,
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel,
    mapViewModel: MapViewModel,
    navController: NavController
) {
    val tabs = listOf("Lugares", "RutaOffline", "ListaRutas")
    var selectedTabIndex by remember { mutableStateOf(1) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> GestionOfflineLugares(lugarViewModel)
            //0-> PantallaFavoritos(lugarViewModel)
            1 -> MenuRutaOffline(navController)
            //2 -> PantallaRutaOffline(lugarRutaOfflineViewModel, lugarViewModel, mapViewModel, navController)
            2-> RespuestaFiltroOffline(lugarRutaOfflineViewModel)
        }
    }
}
