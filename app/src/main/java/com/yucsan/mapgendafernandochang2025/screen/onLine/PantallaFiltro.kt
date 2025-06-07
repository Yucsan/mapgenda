package com.yucsan.mapgendafernandochang2025.screen.onLine

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.graphics.Color
import com.yucsan.mapgendafernandochang2025.util.solicitarPermisoUbicacion
import com.yucsan.mapgendafernandochang2025.util.categoriasPorGrupo
import com.yucsan.mapgendafernandochang2025.util.coloresPorCategoriaPadre
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.componentes.Ruta
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.google.android.gms.maps.model.LatLng


@androidx.annotation.OptIn(UnstableApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFiltro(
    viewModelLugar: LugarViewModel = viewModel(),
    ubicacionViewModel: UbicacionViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val cargando by viewModelLugar.cargando.collectAsState()
    val seleccionadasViewModel by viewModelLugar.filtrosActivos.collectAsState()
    val seleccionadas = remember { mutableStateListOf<String>() }
    val permisoConcedido = remember { mutableStateOf(false) }
    var distanciaFiltro by remember { mutableStateOf(10000f) }
    var iniciarCarga by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val conteoPorSubcategoria by viewModelLugar.conteoPorSubcategoria.collectAsState()
    var visible by remember { mutableStateOf(false) }
    val categoriasActivas = remember { mutableStateListOf<String>() }
    val categoriasActivasOrdenadas = categoriasPorGrupo.keys.filter { categoriasActivas.contains(it) }
    val ubicacionesGuardadas by ubicacionViewModel.ubicaciones.collectAsState()
    var expandirMenu by remember { mutableStateOf(false) }

    // Al cambiar los filtros globales, actualizamos el estado local
    LaunchedEffect(seleccionadasViewModel) {
        seleccionadas.clear()
        seleccionadas.addAll(seleccionadasViewModel)

        categoriasActivas.clear()
        seleccionadasViewModel.forEach { subcat ->
            categoriasPorGrupo.entries.find { it.value.contains(subcat) }?.key?.let { categoria ->
                if (!categoriasActivas.contains(categoria)) {
                    categoriasActivas.add(categoria)
                }
            }
        }
    }

    //Acciones iniciales al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModelLugar.cargarConteoSubcategorias()
        viewModelLugar.observarTodosLosLugares()
        verificarPermisoYIniciar(context, permisoConcedido, viewModelLugar)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Filtro Mapa OnLine",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Selector de ubicación
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                ) {
                    Text(
                        text = "Selecciona una ubicación guardada:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 5.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandirMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Seleccionar ubicación")
                        }

                        DropdownMenu(
                            expanded = expandirMenu,
                            onDismissRequest = { expandirMenu = false }
                        ) {
                            if (ubicacionesGuardadas.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay ubicaciones guardadas") },
                                    onClick = {}
                                )
                            } else {
                                ubicacionesGuardadas.forEach { ubi ->
                                    DropdownMenuItem(
                                        text = { Text("${ubi.nombre} (${ubi.tipo})") },
                                        onClick = {
                                            viewModelLugar.actualizarUbicacion(ubi.latitud, ubi.longitud)
                                            expandirMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sección de categorías principales
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    categoriasPorGrupo.entries.forEach { (categoria, _) ->
                        val color = coloresPorCategoriaPadre[categoria]
                        FilterChip(
                            selected = categoriasActivas.contains(categoria),
                            onClick = {
                                if (categoriasActivas.contains(categoria)) {
                                    categoriasActivas.remove(categoria)
                                    categoriasPorGrupo[categoria]?.forEach {
                                        seleccionadas.remove(it)
                                    }
                                } else {
                                    categoriasActivas.add(categoria)
                                }
                            },
                            label = {
                                Text(
                                    categoria
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color
                                    ?: Color.Gray, // fondo cuando está seleccionado
                                containerColor = Color.Transparent, // fondo cuando NO está seleccionado
                                selectedLabelColor = Color.White,
                                labelColor = color ?: MaterialTheme.colorScheme.primary, // color del texto cuando no está seleccionado
                                selectedTrailingIconColor = Color.White,
                                disabledContainerColor = Color.Transparent,
                                disabledLabelColor = color?.copy(alpha = 0.4f)
                                    ?: Color.LightGray
                            ),
                            border = BorderStroke(1.dp, color ?: Color.Gray)
                        )
                    }
                }
            }

            // Sección de subcategorías (SIMPLIFICADA)
            item {
                Spacer(Modifier.height(8.dp))

                categoriasActivasOrdenadas.forEach { categoria ->
                    val color = coloresPorCategoriaPadre[categoria]

                    // Título de la categoría
                    Text(
                        text = categoria,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    // Chips de subcategorías
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        categoriasPorGrupo[categoria]?.forEach { subcategoria ->
                            if ((conteoPorSubcategoria[subcategoria] ?: 0) > 0) {
                                FilterChip(
                                    selected = seleccionadas.contains(subcategoria),
                                    onClick = {
                                        if (seleccionadas.contains(subcategoria)) {
                                            seleccionadas.remove(subcategoria)
                                        } else {
                                            seleccionadas.add(subcategoria)
                                        }
                                    },
                                    label = {
                                        Text(
                                            "$subcategoria (${conteoPorSubcategoria[subcategoria] ?: 0})",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color ?: Color.Gray,
                                        selectedLabelColor = Color.White,
                                        containerColor = color?.copy(alpha = 0.2f)
                                            ?: Color.LightGray
                                    ),
                                    border = BorderStroke(1.dp, color ?: Color.Gray)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }


            // Botón para aplicar filtro
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (seleccionadas.isNotEmpty()) {
                            Log.d("Filtro", "🔍 Subcategorías seleccionadas: $seleccionadas")

                            viewModelLugar.actualizarCategorias(seleccionadas.toSet())
                            viewModelLugar.actualizarFiltrosActivos(seleccionadas.toSet())
                            viewModelLugar.actualizarRadio(distanciaFiltro)

                            iniciarCarga = true
                        } else {
                            Toast.makeText(
                                context,
                                "Selecciona al menos una categoría",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buscar lugares")
                }
            }


            // Botón para volver a pedir permiso si no fue concedido
            if (!permisoConcedido.value) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                verificarPermisoYIniciar(
                                    context,
                                    permisoConcedido,
                                    viewModelLugar
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("\uD83D\uDD13 Volver a intentar permiso")
                    }
                }
            }

            // 🟦 Indicador de carga si está cargando
            if (cargando) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                    Text("Cargando lugares desde base local...")
                }
            }
        }
    }

    // Navegación al mapa si se concede el permiso y se seleccionan categorías
    LaunchedEffect(viewModelLugar.ubicacion.value, iniciarCarga) {
        Log.d(
            "DEBUG_NAV",
            "→ iniciarCarga=$iniciarCarga, ubicacion=${viewModelLugar.ubicacion.value}, permisoConcedido=${permisoConcedido.value}, cargando=$cargando"
        )

        if (iniciarCarga &&
            viewModelLugar.ubicacion.value != null &&
            !cargando
        ) {
            Log.d("DEBUG_NAV", "✅ Condiciones cumplidas, navegando a mapa")

            navController.navigate(Ruta.MapaCompose.ruta) {
                popUpTo(Ruta.Filtro.ruta) { inclusive = true }
            }

            iniciarCarga = false
        } else {
            Log.d("DEBUG_NAV", "⏳ Aún no se cumplen condiciones para navegar")
        }
    }
}


// Función auxiliar para manejar el permiso de ubicación
@androidx.annotation.OptIn(UnstableApi::class)
private suspend fun verificarPermisoYIniciar(
    context: Context,
    permisoConcedido: MutableState<Boolean>,
    viewModelLugar: LugarViewModel
) {
    val ok = solicitarPermisoUbicacion(context)
    permisoConcedido.value = ok
    if (ok) {
        viewModelLugar.iniciarActualizacionUbicacion(context)
        Log.d("FiltroUbicacion", "📍 Permiso concedido. Intentando obtener ubicación...")
        delay(1000)
        Log.d("FiltroUbicacion", "📍 Ubicación obtenida: ${viewModelLugar.ubicacion.value}")
    } else {
        Toast.makeText(context, "Se necesita el permiso de ubicación", Toast.LENGTH_SHORT).show()
        Log.w("FiltroUbicacion", "⛔ Permiso denegado")
    }
}


