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
import android.util.Log
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.yucsan.mapgendafernandochang2025.screens.mapa.alertas.DetalleLugarDialog
import com.yucsan.mapgendafernandochang2025.util.Secrets
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.NavegacionViewModel
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController


@SuppressLint("SuspiciousIndentation")
@OptIn(UnstableApi::class)
@Composable
// ********************************************  LISTADO DE LUGARES POR UBICACIONES  ********************************************
fun PantallaListadoLugares(
    lugarRutaOfflineViewModel: LugarRutaOfflineViewModel,
    ubicacionViewModel: UbicacionViewModel,
    lugarViewModel: LugarViewModel,
    navegacionViewModel: NavegacionViewModel,
    networkMonitor: NetworkMonitor,
    navController: NavController
) {
    // 1️⃣ Estado de ubicación seleccionada
    val ubicacion by lugarRutaOfflineViewModel.ubicacion.collectAsState(initial = null)

    // 2️⃣ Estado del conjunto de subcategorías elegidas
    //    (asumimos que aquí ya has llenado lugarRutaOfflineViewModel.filtrosActivos
    //     vía los chips, igual que en tu PantallaFiltroOffline)
    val subcatsSeleccionadas by lugarRutaOfflineViewModel.filtrosActivos.collectAsState()
    val ubicaciones by ubicacionViewModel.ubicaciones.collectAsState(initial = emptyList())
    val conteoPorSubcategoria by lugarRutaOfflineViewModel.conteoPorSubcategoriaFiltrado.collectAsState()


    // 3️⃣ Este es el flujo **filtrado** de lugares que devuelve aplicarFiltroManualConParametros
    val lugaresFiltrados by lugarRutaOfflineViewModel.lugaresOffline.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        Log.d("DEBUG_LISTADO", "✅ Ubicación actual: $ubicacion")
    }


    // ——— Agrupar por subcategoría y mostrar ———
    val agrupados = remember(lugaresFiltrados) {
        lugaresFiltrados.groupBy { it.subcategoria ?: "Sin categoría" }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val subcategoriasDisponibles = agrupados.keys.toList()

    val buscador = remember { mutableStateOf("") }
    val categoriaSeleccionada = remember { mutableStateOf<String?>(null) }

    val indicesDeCategoria = remember(agrupados) {
        val mapa = mutableMapOf<String, Int>()
        var index = 0
        agrupados.forEach { (subcat, lista) ->
            mapa[subcat] = index
            index += 1 + lista.size // 1 por el título + lugares
        }
        mapa
    }

    LaunchedEffect(subcatsSeleccionadas) {
        Log.d("DEBUG_UI", "🧪 Subcategorías seleccionadas: $subcatsSeleccionadas")
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

        // ——— Encabezado con flecha, fecha y título ———
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {  navController.popBackStack() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }



            Text(
                text = "Listado de lugares",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // ——— Selector de ubicación ———
        Text("Selecciona una ubicación guardada:",
            style = MaterialTheme.typography.titleMedium)



        DropdownMenuUbicaciones(
            ubicaciones = ubicaciones,
            ubicacionActual = ubicacion,
            conteoPorSubcategoria = conteoPorSubcategoria,
            lugarRutaOfflineViewModel = lugarRutaOfflineViewModel
        )


        // *****************************************************  Filtros *****************************************************

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = buscador.value,
                    onValueChange = { buscador.value = it },
                    textStyle = MaterialTheme.typography.bodySmall,
                    label = { Text("Buscar lugar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                var expandedCat by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ir a categoría...")
                    }
                    DropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        subcategoriasDisponibles.forEach { subcat ->
                            DropdownMenuItem(
                                text = { Text(subcat) },
                                onClick = {
                                    expandedCat = false
                                    categoriaSeleccionada.value = subcat
                                    val index = indicesDeCategoria[subcat] ?: 0
                                    scope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }


        // *************************************************    LISTA DE LUGARES    *************************************************
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            agrupados.forEach { (subcat, lista) ->

                val queryWords = buscador.value.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }

                val lugaresFiltradosPorTexto = lista.filter { lugar ->
                    val textoCompleto = "${lugar.nombre} ${lugar.direccion}".lowercase()

                    queryWords.all { palabra ->
                        textoCompleto.contains(palabra.lowercase())
                    }
                }

                if (lugaresFiltradosPorTexto.isNotEmpty()) {
                    item {
                        Text(
                            text = "$subcat (${lugaresFiltradosPorTexto.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(lugaresFiltradosPorTexto) { lugar ->
                        ListadoLugarItem(
                            lugar = lugar,
                            lugarViewModel = lugarViewModel,
                            navegacionViewModel = navegacionViewModel,
                            networkMonitor = networkMonitor
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun ListadoLugarItem(
    lugar: LugarLocal,
    lugarViewModel: LugarViewModel,
    navegacionViewModel: NavegacionViewModel,
    networkMonitor: NetworkMonitor
) {
    val context = LocalContext.current
    var mostrarDialogo by remember { mutableStateOf(false) }
    val hayConexion by networkMonitor.isConnected.collectAsState()
    val ubicacionActual by navegacionViewModel.ubicacionActual.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🧾 Información (85%)
            Column(
                modifier = Modifier
                    .weight(0.85f)
            ) {
                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = lugar.direccion ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✏️ Botón de edición (15%)
            Box(
                modifier = Modifier
                    .weight(0.15f),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = { mostrarDialogo = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar lugar"
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        DetalleLugarDialog(
            lugar = lugar,
            apiKey = Secrets.GOOGLE_MAPS_API_KEY,
            viewModelLugar = lugarViewModel,
            navegacionViewModel = navegacionViewModel,
            onDismiss = { mostrarDialogo = false },
            ubicacionActual = ubicacionActual,
            hayConexion = hayConexion
        )
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
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        ) {
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



