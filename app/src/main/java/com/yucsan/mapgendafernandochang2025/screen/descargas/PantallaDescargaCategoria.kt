package com.yucsan.mapgendafernandochang2025.screen.descargas

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.util.Secrets


import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.yucsan.mapgendafernandochang2025.util.categoriasPorGrupo
import com.yucsan.mapgendafernandochang2025.util.coloresPorCategoriaPadre


import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import com.google.android.gms.maps.model.LatLng
import com.yucsan.mapgendafernandochang2025.ThemeViewModel

import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFiltroDescarga(
    viewModelLugar: LugarViewModel = viewModel(),
    ubicacionViewModel: UbicacionViewModel,
    navController: NavController,
    themeViewModel: ThemeViewModel
) {

    val ubicacionSeleccionada = viewModelLugar.ubicacion.collectAsState()
    val context = LocalContext.current
    val cargando by viewModelLugar.cargando.collectAsState()
    val permisoConcedido = remember { mutableStateOf(false) }
    var iniciarCarga by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val seleccionadas = remember { mutableStateListOf<String>() }
    val categoriasActivas = remember { mutableStateListOf<String>() }
    val conteoPorSubcategoria by viewModelLugar.conteoPorSubcategoria.collectAsState()

    // Sección de categorías personalizadas
    val eventoDescargaPersonalizada by viewModelLugar.eventoDescargaPersonalizada.collectAsState()
    var mostrarBotonRefrescar by remember { mutableStateOf(false) }

    var triggerRecomposicion by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val apiKey = Secrets.GOOGLE_MAPS_API_KEY

    val categoriasPorGrupo = categoriasPorGrupo

    val enterAnimation = slideInVertically { it }
    val exitAnimation = slideOutVertically { it } + fadeOut()

    val ubicacionesGuardadas by ubicacionViewModel.ubicaciones.collectAsState()

    LaunchedEffect(triggerRecomposicion) {
        viewModelLugar.cargarConteoSubcategorias()
        visible = true
    }

    LaunchedEffect(eventoDescargaPersonalizada) {
        if (eventoDescargaPersonalizada) {
            mostrarBotonRefrescar = true
            // Opcional: resetear el evento para que no se dispare varias veces ----------------------------
            viewModelLugar.resetearEventoDescargaPersonalizada()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = enterAnimation,
        exit = exitAnimation
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE1F4ED), Color(0xFFC9DFF4)
                        )
                    )
                )
        ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                        contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 0.dp // <- este es el ajuste importante
                    )
                ) {
                    item {
                        var expandirMenu by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Ubicaciones Almacenadas:")

                            Box {
                                OutlinedButton(onClick = { expandirMenu = true }) {
                                        val ubicacionActual = ubicacionSeleccionada.value

                                        val ubicacionSeleccionadaTexto = remember(ubicacionesGuardadas, ubicacionActual) {
                                            val match = ubicacionesGuardadas.find {
                                                it.latitud == ubicacionActual?.first && it.longitud == ubicacionActual?.second
                                            }

                                            match?.let { "📍 ${it.nombre} (${it.tipo})" }
                                                ?: ubicacionActual?.let { "📍 %.4f, %.4f".format(it.first, it.second) }
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
                                                        viewModelLugar.actualizarUbicacionManual(
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

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    navController.navigate("mapaubi?modoSeleccionUbicacion=true&modoCrearRuta=false")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📍 Elegir zona de descarga en el mapa")
                            }

                            ubicacionSeleccionada.value?.let { (lat, lng) ->
                                Text(
                                    text = "Zona seleccionada: %.4f, %.4f".format(lat, lng),
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        item {

                            Spacer(Modifier.height(10.dp))

                            Text("Descargas restantes: ${18 - seleccionadas.size}")

                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categoriasPorGrupo.keys.forEach { categoria ->
                                    val color = coloresPorCategoriaPadre[categoria]
                                    FilterChip(
                                        selected = categoriasActivas.contains(categoria),
                                        onClick = {
                                            if (categoriasActivas.contains(categoria)) {
                                                categoriasActivas.remove(categoria)
                                                // Eliminar subcategorías si se desactiva grupo
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
                                            selectedLabelColor = Color.White,
                                            labelColor = MaterialTheme.colorScheme.primary , // color del texto cuando no está seleccionado
                                            selectedTrailingIconColor = Color.White,
                                            disabledContainerColor = Color.Transparent,
                                            disabledLabelColor = color?.copy(alpha = 0.4f)
                                                ?: Color.LightGray
                                        ),
                                        border = BorderStroke(1.dp, color ?: Color.Gray)
                                    )
                                }
                            }
                            if (mostrarBotonRefrescar) {
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        triggerRecomposicion++
                                        mostrarBotonRefrescar = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF00D43B
                                        )
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🔄 Refrescar Conteo", color = Color.Black)
                                }
                            }
                        }

                        //  Sección de subcategorías ---------------------------------------------------
                        item {
                            Spacer(Modifier.height(16.dp))
                            categoriasActivas.forEach { categoria ->
                                Text(
                                    text = categoria,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    categoriasPorGrupo[categoria]?.forEach { subcategoria ->
                                        val color = coloresPorCategoriaPadre[categoria]
                                        FilterChip(
                                            selected = seleccionadas.contains(subcategoria),
                                            onClick = {
                                                if (seleccionadas.contains(subcategoria)) {
                                                    seleccionadas.remove(subcategoria)
                                                } else {
                                                    if (seleccionadas.size >= 18) {
                                                        Toast.makeText(
                                                            context,
                                                            "Máximo 18 subcategorías seleccionadas",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        seleccionadas.add(subcategoria)
                                                    }
                                                }
                                            },
                                            //--------------------------------------------------------------------------------*
                                            label = {
                                                Text(
                                                    text = "$subcategoria (${conteoPorSubcategoria[subcategoria] ?: 0})",
                                                    color = Color.White
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
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (seleccionadas.isNotEmpty()) {
                                        scope.launch {
                                            iniciarCarga = true
                                            viewModelLugar.descargarLugaresPorSubcategoriasPersonalizadas(
                                                context = context,
                                                subcategorias = seleccionadas.toSet(),
                                                apiKey = apiKey
                                            )
                                            triggerRecomposicion++ // Fuerza actualización del conteo

                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Selecciona al menos una subcategoría",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                enabled = seleccionadas.isNotEmpty() && !cargando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Descargar Categorías Seleccionadas")
                            }
                        }

                    }
                    if (cargando) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Descargando lugares desde Google Places...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                }
            }


    LaunchedEffect(viewModelLugar.ubicacion.value, iniciarCarga) {
        if (iniciarCarga && viewModelLugar.ubicacion.value != null && permisoConcedido.value && !cargando) {
            visible = false
            snapshotFlow { visible }.first { !it }
            viewModelLugar.actualizarCategorias(seleccionadas.toSet())
            navController.navigate("mapa") {
                popUpTo(0)
            }
            iniciarCarga = false
        }
    }

}


