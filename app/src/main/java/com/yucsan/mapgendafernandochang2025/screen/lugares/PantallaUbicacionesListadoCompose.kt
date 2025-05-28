package com.yucsan.mapgendafernandochang2025.screen.lugares

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.maps.model.LatLng
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import android.util.Log  // Asegúrate de tener este import arriba
import kotlinx.coroutines.launch

@SuppressLint("SuspiciousIndentation")
@OptIn(UnstableApi::class)
@Composable
fun PantallaUbicacionesListadoCompose(
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel,
    ubicacionViewModel: UbicacionViewModel
) {
    // 1️⃣ Estado de ubicación seleccionada
    val ubicacion by lugarRutaOfflineViewModel.ubicacion.collectAsState(initial = null)

    // 2️⃣ Estado del conjunto de subcategorías elegidas
    //    (asumimos que aquí ya has llenado lugarRutaOfflineViewModel.filtrosActivos
    //     vía los chips, igual que en tu PantallaFiltroOffline)
    val subcatsSeleccionadas by lugarRutaOfflineViewModel.filtrosActivos.collectAsState()

    LaunchedEffect(subcatsSeleccionadas) {
        Log.d("DEBUG_UI", "🧪 Subcategorías seleccionadas: $subcatsSeleccionadas")
    }


    // 3️⃣ Este es el flujo **filtrado** de lugares que devuelve aplicarFiltroManualConParametros
    val lugaresFiltrados by lugarRutaOfflineViewModel.lugaresOffline.collectAsState(initial = emptyList())



            LaunchedEffect(Unit) {
                Log.d("DEBUG_LISTADO", "✅ Ubicación actual: $ubicacion")
            }

    LaunchedEffect(subcatsSeleccionadas) {
        Log.d("DEBUG_LISTADO", "🏷️ Subcategorías seleccionadas: $subcatsSeleccionadas")
    }

    LaunchedEffect(lugaresFiltrados) {
        Log.d("DEBUG_LISTADO", "📦 Lugares filtrados (${lugaresFiltrados.size}):")
        lugaresFiltrados.forEach {
            Log.d("DEBUG_LISTADO", "🔹 ${it.nombre} | subcat=${it.subcategoria}")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ——— Selector de ubicación ———
        Text("Selecciona una ubicación guardada:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val ubicaciones by ubicacionViewModel.ubicaciones.collectAsState(initial = emptyList())
        val conteoPorSubcategoria by lugarRutaOfflineViewModel.conteoPorSubcategoriaFiltrado.collectAsState()

        DropdownMenuUbicaciones(
            ubicaciones = ubicaciones,
            ubicacionActual = ubicacion,
            conteoPorSubcategoria = conteoPorSubcategoria,
            lugarRutaOfflineViewModel = lugarRutaOfflineViewModel
        )
        Spacer(Modifier.height(16.dp))

        // ——— Agrupar por subcategoría y mostrar ———
        val agrupados = remember(lugaresFiltrados) {
            lugaresFiltrados.groupBy { it.subcategoria ?: "Sin categoría" }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            agrupados.forEach { (subcat, lista) ->
                if (lista.isNotEmpty()) {
                    item {
                        Text(
                            text     = "$subcat (${lista.size})",
                            style    = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                    items(lista) { lugar ->
                        ListadoLugarItem(lugar)
                    }
                }
            }
        }
    }
}

@Composable
fun ListadoLugarItem(lugar: LugarLocal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = lugar.nombre, style = MaterialTheme.typography.bodyLarge)
        Text(text = lugar.direccion ?: "", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun DropdownMenuUbicaciones(
    ubicaciones: List<UbicacionLocal>,
    ubicacionActual: Pair<Double, Double>?,
    conteoPorSubcategoria: Map<String, Int>,
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope() // ✅

    val textoActual = ubicaciones.find {
        it.latitud == ubicacionActual?.first && it.longitud == ubicacionActual.second
    }?.let { "📍 ${it.nombre} (${it.tipo})" } ?: "Elegir ubicación"

    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(textoActual)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ubicaciones.forEach { ubi ->
                DropdownMenuItem(
                    text = { Text("${ubi.nombre} (${ubi.tipo})") },

                    onClick = {
                        expanded = false
                        val nuevaUbicacion = LatLng(ubi.latitud, ubi.longitud)
                        lugarRutaOfflineViewModel.seleccionarUbicacionYAplicarFiltros(nuevaUbicacion)
                    }


                )
            }
        }
    }
}



