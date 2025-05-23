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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalConfiguration
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.componentes.Ruta
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel


@androidx.annotation.OptIn(UnstableApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFiltro(
    viewModelLugar: LugarViewModel = viewModel(), // ViewModel donde se guarda el estado global
    navController: NavController
) {
    val context = LocalContext.current
    val cargando by viewModelLugar.cargando.collectAsState() // 🔁 Estado de carga de lugares
    val seleccionadasViewModel by viewModelLugar.filtrosActivos.collectAsState() //SUBCATEGORÍAS seleccionadas globalmente (guardadas en el ViewModel)
    val seleccionadas = remember { mutableStateListOf<String>() } // SUBCATEGORÍAS seleccionadas localmente (editable)
    val permisoConcedido = remember { mutableStateOf(false) } // Estado del permiso de ubicación
    var distanciaFiltro by remember { mutableStateOf(10000f) } // Distancia máxima para filtrar lugares
    var iniciarCarga by remember { mutableStateOf(false) } // Controla si se debe iniciar la carga y navegar
    val scope = rememberCoroutineScope()
    val conteoPorSubcategoria by viewModelLugar.conteoPorSubcategoria.collectAsState() // Cantidad de lugares por subcategoría
    var visible by remember { mutableStateOf(false) } //  Controla la visibilidad animada de la pantalla
    val categoriasActivas = remember { mutableStateListOf<String>() } // CATEGORÍAS PADRE activas localmente
    val categoriasActivasOrdenadas = categoriasPorGrupo.keys.filter { categoriasActivas.contains(it) }


    // Al cambiar los filtros globales, actualizamos el estado local
    LaunchedEffect(seleccionadasViewModel) {
        seleccionadas.clear()
        seleccionadas.addAll(seleccionadasViewModel)

        categoriasActivas.clear()
        // Inferimos las CATEGORÍAS PADRE activas en base a las subcategorías
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

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("Menú Categorías") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxSize()
                    ) {

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
                                        label = { Text(categoria, color = MaterialTheme.colorScheme.onSurface) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = color ?: Color.Gray, // fondo cuando está seleccionado
                                            containerColor = Color.Transparent, // fondo cuando NO está seleccionado
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedTrailingIconColor = Color.White,
                                            disabledContainerColor = Color.Transparent,
                                            disabledLabelColor = color?.copy(alpha = 0.4f) ?: Color.LightGray
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
                                                    containerColor = color?.copy(alpha = 0.2f) ?: Color.LightGray
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

                                        viewModelLugar.actualizarCategorias(seleccionadas.toSet()) // ← ["park", ...]
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
        // Navegación al mapa si se concede el permiso y se seleccionan categorías
        LaunchedEffect(viewModelLugar.ubicacion.value, iniciarCarga) {
            Log.d(
                "DEBUG_NAV",
                "→ iniciarCarga=$iniciarCarga, ubicacion=${viewModelLugar.ubicacion.value}, permisoConcedido=${permisoConcedido.value}, cargando=$cargando"
            )

            if (iniciarCarga &&
                viewModelLugar.ubicacion.value != null &&
                permisoConcedido.value &&
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


