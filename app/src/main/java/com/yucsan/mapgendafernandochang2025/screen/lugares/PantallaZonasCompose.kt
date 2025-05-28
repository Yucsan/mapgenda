package com.yucsan.mapgendafernandochang2025.screen.lugares

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.yucsan.mapgendafernandochang2025.util.haversineDistance


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(UnstableApi::class)
@Composable
fun PantallaZonasCompose(
    lugarViewModel: LugarViewModel = viewModel(),
    ubicacionViewModel: UbicacionViewModel = viewModel(),
    navController: NavController
) {
    val zonasPorLugares by lugarViewModel.lugaresPorZona.collectAsState()
    val ubicaciones by ubicacionViewModel.ubicaciones.collectAsState()

    var zonaExpandida by remember { mutableStateOf<String?>(null) }
    var filtroTexto by remember { mutableStateOf(TextFieldValue("")) }


    // Arrancamos la obtención de la ubicación una sola vez:
    LaunchedEffect(Unit) {
        ubicacionViewModel.iniciarActualizacionUbicacion()
        lugarViewModel.observarTodosLosLugares()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar con título y volver
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Explora por Zonas", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }

        // Filtro minimalista
        OutlinedTextField(
            value = filtroTexto,
            onValueChange = { filtroTexto = it },
            label = { Text("Buscar lugar") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp)
        )

        if (zonasPorLugares.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay lugares para mostrar.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                zonasPorLugares.forEach { (zonaNombre, lugaresOriginales) ->
                    val lugaresFiltrados = lugaresOriginales.filter {
                        it.nombre.contains(filtroTexto.text, ignoreCase = true) ||
                                it.direccion.contains(filtroTexto.text, ignoreCase = true)
                    }

                    if (lugaresFiltrados.isNotEmpty()) {
                        item {
                            val isExpanded = zonaExpandida == zonaNombre

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            zonaExpandida = if (isExpanded) null else zonaNombre
                                        }
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = zonaNombre,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null
                                    )
                                }

                                if (isExpanded) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        lugaresFiltrados.forEach { lugar ->
                                            LugarItemMinimal(lugar)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LugarItemMinimal(lugar: LugarLocal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = lugar.nombre, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = lugar.direccion,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
