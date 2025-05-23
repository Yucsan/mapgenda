package com.yucsan.mapgendafernandochang2025.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import kotlinx.coroutines.flow.first
import com.yucsan.mapgendafernandochang2025.util.categoriasPorGrupo
import com.yucsan.mapgendafernandochang2025.util.coloresPorCategoriaPadre
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.yucsan.mapgendafernandochang2025.util.categoriasPorGrupo
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarRutaOfflineViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFiltroOffline(
    lugarOfflineViewModel: LugarRutaOfflineViewModel,
    lugarViewModel: LugarViewModel,
    ubicacionViewModel: UbicacionViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val cargando by lugarOfflineViewModel.cargando.collectAsState()
    val seleccionadasViewModel by lugarOfflineViewModel.filtrosActivos.collectAsState()
    val seleccionadas = remember { mutableStateListOf<String>() }
    val permisoConcedido = remember { mutableStateOf(false) }
    var distanciaFiltro by remember { mutableStateOf(10000f) }
    var iniciarCarga by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val conteoPorSubcategoria by lugarOfflineViewModel.conteoPorSubcategoria.collectAsState()
    var visible by remember { mutableStateOf(false) }
    val categoriasActivas = remember { mutableStateListOf<String>() }
    val categoriasActivasOrdenadas =
        categoriasPorGrupo.keys.filter { categoriasActivas.contains(it) }
    val switchesActivos = remember { mutableStateMapOf<String, Boolean>() }
    val ubicacion by lugarOfflineViewModel.ubicacion.collectAsState()
    val ubicacionesGuardadas by ubicacionViewModel.ubicaciones.collectAsState()

    var expandirMenu by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        lugarOfflineViewModel.probarFiltroManual()
    }

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

    LaunchedEffect(ubicacion) {
        Log.d("DEBUG_UI", "🔁 UI recomposición: ubicación actualizada a: $ubicacion")
    }

    //Acciones iniciales al entrar a la pantalla
    LaunchedEffect(Unit) {
        lugarOfflineViewModel.cargarConteoSubcategorias()
        lugarOfflineViewModel.observarTodosLosLugares()
        verificarPermisoYIniciar(context, permisoConcedido, lugarOfflineViewModel)
        visible = true
    }

    // Animaciones de entrada/salida
    val enterAnimation = remember {
        slideInVertically(initialOffsetY = { fullHeight -> fullHeight })
    }
    val exitAnimation = remember {
        slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
    }

    // UI principal animada
    AnimatedVisibility(visible = visible, enter = enterAnimation, exit = exitAnimation) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Filtro de lugares Rutas Offline") },
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
            AnimatedVisibility(visible = visible, enter = enterAnimation, exit = exitAnimation) {
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
                        item {


                            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Text("Selecciona una ubicación guardada:")

                                Box {
                                    OutlinedButton(onClick = { expandirMenu = true }) {
                                        val ubicacionActual = ubicacion // Extraemos primero el valor para evitar smart cast error

                                        val ubicacionSeleccionadaTexto = remember(ubicacionesGuardadas, ubicacionActual) {
                                            val match = ubicacionesGuardadas.find {
                                                it.latitud == ubicacionActual?.first && it.longitud == ubicacionActual?.second
                                            }

                                            match?.let { "📍 ${it.nombre} (${it.tipo})" }
                                                ?: ubicacionActual?.let { "📍 ${it.first.format(4)}, ${it.second.format(4)}" }
                                                ?: "Elegir ubicación"
                                        }

                                        Text(ubicacionSeleccionadaTexto)

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
                                                        lugarOfflineViewModel.actualizarUbicacionManual(
                                                            LatLng(ubi.latitud, ubi.longitud)
                                                        )
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
                            Text(text = "Ubicación usada: ${ubicacion?.first}, ${ubicacion?.second}")

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
                                        label = { Text(categoria) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = color
                                                ?: Color.Gray, // fondo cuando está seleccionado
                                            containerColor = Color.Transparent, // fondo cuando NO está seleccionado
                                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            selectedTrailingIconColor = Color.White,
                                            disabledContainerColor = Color.Transparent,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                ?: Color.LightGray
                                        ),
                                        border = BorderStroke(1.dp, color ?: Color.Gray)
                                    )
                                }
                            }
                        }

                        item{

                            //------------------------------------------ funcion de prueba que YA FUNCIONA
                         /*
                            val scope = rememberCoroutineScope()
                            Button(
                                onClick = {
                                    scope.launch {
                                        lugarOfflineViewModel.probarFiltroManual2()
                                        delay(500) // Espera corta para asegurar que _lugaresOffline se actualice
                                        val lugares = lugarOfflineViewModel.lugaresOffline.value
                                        Log.d("PRUEBA_MANUAL_UI", "🟢 Lugares después de prueba: ${lugares.size}")
                                        if (lugares.isNotEmpty()) {
                                            // Opcionalmente setear ubicación fija para centrar el mapa (coincide con el filtro)
                                            lugarOfflineViewModel.actualizarUbicacionManual(
                                                LatLng(42.8782, -8.5448)
                                            )

                                            // Navegar al mapa
                                            navController.navigate("mapaubi") {
                                                popUpTo(0)
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "❗ No se encontraron lugares con el filtro de prueba",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            ) {
                                Text("🔍 Probar filtro manual")
                            }*/
                        }

                        item {
                            Spacer(Modifier.height(1.dp))
                            // Sección de subcategorías
                            categoriasActivasOrdenadas.forEach { categoria ->
                                val color = coloresPorCategoriaPadre[categoria]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = categoria,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "minimenú",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Gray
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    categoriasPorGrupo[categoria]?.forEach { subcategoria ->
                                        if ((conteoPorSubcategoria[subcategoria] ?: 0) > 0) {

                                            val switchActivo =
                                                switchesActivos[subcategoria] ?: false

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 1.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                // CHIP para selección de subcategoría
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
                                                        Text("$subcategoria (${conteoPorSubcategoria[subcategoria] ?: 0})")
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = color
                                                            ?: Color.Gray,
                                                        selectedLabelColor = Color.White,
                                                        containerColor = color?.copy(alpha = 0.2f)
                                                            ?: Color.LightGray
                                                    ),
                                                    border = BorderStroke(1.dp, color ?: Color.Gray)
                                                )

                                                // SWITCH independiente
                                                Switch(
                                                    checked = switchActivo,
                                                    onCheckedChange = { isChecked ->
                                                        switchesActivos[subcategoria] = isChecked
                                                        // Aquí puedes activar una acción especial si lo necesitas
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(1.dp))
                            }
                        }
                        // Botón para aplicar filtro


                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            val scope = rememberCoroutineScope()
                            Button(
                                onClick = {
                                    if (seleccionadas.isNotEmpty()) {
                                        scope.launch {
                                            val ubicacionManual = lugarOfflineViewModel.ubicacion.value

                                            if (ubicacionManual != null) {
                                                val latLng = LatLng(ubicacionManual.first, ubicacionManual.second)

                                                // ✅ Actualizamos ubicación manualmente en el ViewModel, si aún no lo está
                                                lugarOfflineViewModel.actualizarUbicacionManual(latLng)

                                                // ✅ Guardamos filtros y radio
                                                lugarOfflineViewModel.actualizarRadio(distanciaFiltro)
                                                lugarOfflineViewModel.actualizarFiltrosActivos(seleccionadas.toSet())
                                                lugarOfflineViewModel.actualizarCategorias(seleccionadas.toSet())

                                                // ✅ Aplicamos el filtro dinámico real
                                                lugarOfflineViewModel.aplicarFiltroManualConParametros(
                                                    subcategorias = seleccionadas.toList(),
                                                    centro = latLng,
                                                    radio = distanciaFiltro
                                                )

                                                delay(600) // ⏱️ Pequeña espera para que lugaresOffline se actualice
                                                val lugares = lugarOfflineViewModel.lugaresOffline.value
                                                Log.d("FILTRO_DINAMICO", "✅ Lugares filtrados: ${lugares.size}")

                                                if (lugares.isNotEmpty()) {
                                                    navController.navigate("mapaubi") {
                                                        popUpTo(0)
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "❗ No se encontraron lugares con el filtro actual",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "❗ No hay ubicación disponible para aplicar el filtro",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
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
                                                lugarOfflineViewModel
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
            }
        }
        // Navegación al mapa si se concede el permiso y se seleccionan categorías
        LaunchedEffect(ubicacion, iniciarCarga) {
            // Fallback: si no hay ubicación en el ViewModel offline, pero sí en el global
            if (ubicacion == null && lugarViewModel.ubicacion.value != null) {
                val (lat, lng) = lugarViewModel.ubicacion.value!!
                Log.d("DEBUG_UBICACION", "📦 Fallback a ubicación desde LugarViewModel: $lat, $lng")
                lugarOfflineViewModel.actualizarUbicacionManual(LatLng(lat, lng))
            }
            if (iniciarCarga && ubicacion != null && permisoConcedido.value && !cargando) {
                visible = false
                snapshotFlow { visible }.first { !it }
                lugarOfflineViewModel.actualizarCategorias(seleccionadas.toSet())
                lugarOfflineViewModel.actualizarRadio(distanciaFiltro)

                Log.d("DEBUG_UBICACION", "🌍 Ubicación usada para filtrar: $ubicacion")
                Toast.makeText(context, "Usando ubicación: $ubicacion", Toast.LENGTH_SHORT).show()

                Log.d("DEBUG_NAV", "🌍 Ubicación actual en ViewModel: ${ubicacion}")
                Log.d("DEBUG_NAV", "🗂️ Categorías seleccionadas: ${seleccionadas}")
                Log.d("DEBUG_NAV", "📏 Radio: $distanciaFiltro")


                navController.navigate("mapaubi") {
                    popUpTo(0)
                }

                iniciarCarga = false
            }
        }
    }
}

// Función auxiliar para manejar el permiso de ubicación
private suspend fun verificarPermisoYIniciar(
    context: Context,
    permisoConcedido: MutableState<Boolean>,
    lugarOfflineViewModel: LugarRutaOfflineViewModel
) {
    val ok = solicitarPermisoUbicacion(context)
    permisoConcedido.value = ok
    if (ok) {
        // SOLO INICIA actualización si no hay ubicación ya establecida
        if (lugarOfflineViewModel.ubicacion.value == null) {
            lugarOfflineViewModel.iniciarActualizacionUbicacion(context)
        } else {
            Log.d("DEBUG_UBI", "✅ Ubicación manual ya establecida, no se sobrescribirá.")
        }
    } else {
        Toast.makeText(context, "Se necesita el permiso de ubicación", Toast.LENGTH_SHORT).show()
    }
}

fun Double.format(decimales: Int) = "%.${decimales}f".format(this)
