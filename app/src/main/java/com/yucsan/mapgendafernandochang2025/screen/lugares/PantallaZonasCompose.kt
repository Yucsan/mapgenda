package com.yucsan.mapgendafernandochang2025.screen.lugares


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel

@Composable
fun PantallaZonasCompose(
    lugarViewModel: LugarViewModel = viewModel()
) {
    val zonasPorLugares by lugarViewModel.lugaresPorZona.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Explora por Zonas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (zonasPorLugares.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay lugares para mostrar.")
            }
        } else {
            zonasPorLugares.forEach { (zonaNombre, lugares) ->
                Text(
                    text = zonaNombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(lugares) { lugar ->
                        LugarItem(lugar)
                    }
                }
            }
        }
    }
}

@Composable
fun LugarItem(lugar: LugarLocal) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Text(text = lugar.nombre, style = MaterialTheme.typography.bodyLarge)
        Text(text = lugar.direccion, style = MaterialTheme.typography.bodySmall)
    }
}
