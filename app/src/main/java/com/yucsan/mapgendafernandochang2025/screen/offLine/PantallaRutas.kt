package com.yucsan.mapgendafernandochang2025.screen.offLine

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugares
import android.net.Uri





@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRutas(viewModel: RutaViewModel, navController: NavController) {
    val rutas by viewModel.rutas.collectAsState()
    val scope = rememberCoroutineScope()

    var rutaSeleccionada by remember { mutableStateOf<RutaConLugares?>(null) }
    var lugaresRutaSeleccionada by remember { mutableStateOf(listOf<LugarLocal>()) }


    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    val categoriasDisponibles = listOf("personalizada", "cultural", "gastronómica", "histórica", "otros")

    var nuevoNombreRuta by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContainerColor = Color.Transparent,
        containerColor = MaterialTheme.colorScheme.background,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            if (rutaSeleccionada != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.DarkGray.copy(alpha = 0.9f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        OutlinedTextField(
                            value = nuevoNombreRuta,
                            onValueChange = { nuevoNombreRuta = it },
                            label = { Text("Nombre de la ruta") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown,
                            onExpandedChange = { expandedDropdown = !expandedDropdown }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = categoriaSeleccionada ?: "Selecciona categoría",
                                onValueChange = {},
                                label = { Text("Categoría") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                categoriasDisponibles.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria) },
                                        onClick = {
                                            categoriaSeleccionada = categoria
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                rutaSeleccionada?.let {
                                    val rutaActualizada = it.ruta.copy(
                                        nombre = nuevoNombreRuta,
                                        categoria = categoriaSeleccionada
                                    )

                                    viewModel.actualizarRuta(rutaActualizada)

                                    viewModel.actualizarOrdenLugares(rutaActualizada.id, lugaresRutaSeleccionada)

                                    // ✅ Refrescar datos desde Room después de guardar
                                    viewModel.recargarRutaSeleccionada(rutaActualizada.id) { rutaRecargada ->
                                        rutaSeleccionada = RutaConLugares(
                                            ruta = rutaRecargada.ruta,
                                            lugares = rutaRecargada.lugares
                                        )
                                        lugaresRutaSeleccionada = rutaRecargada.lugares
                                    }




                                    scope.launch {
                                        scaffoldState.bottomSheetState.partialExpand()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Text("Guardar cambios", color = Color.White)
                        }


                        Spacer(modifier = Modifier.height(12.dp)) // Separación visual



                        Text(
                            text = "Puntos de la ruta (${lugaresRutaSeleccionada.size}):",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn {
                            itemsIndexed(lugaresRutaSeleccionada) { index, lugar ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF424242))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${lugar.nombre}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        lugaresRutaSeleccionada = lugaresRutaSeleccionada.toMutableList().also {
                                                            val moved = it.removeAt(index)
                                                            it.add(index - 1, moved)
                                                        }
                                                        viewModel.actualizarOrdenLugares(rutaSeleccionada!!.ruta.id, lugaresRutaSeleccionada)
                                                    }
                                                },
                                                enabled = index > 0
                                            ) {
                                                Icon(Icons.Default.ExpandLess, contentDescription = "Subir", tint = Color.White)
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (index < lugaresRutaSeleccionada.lastIndex) {
                                                        lugaresRutaSeleccionada = lugaresRutaSeleccionada.toMutableList().also {
                                                            val moved = it.removeAt(index)
                                                            it.add(index + 1, moved)
                                                        }
                                                        viewModel.actualizarOrdenLugares(rutaSeleccionada!!.ruta.id, lugaresRutaSeleccionada)
                                                    }
                                                },
                                                enabled = index < lugaresRutaSeleccionada.lastIndex
                                            ) {
                                                Icon(Icons.Default.ExpandMore, contentDescription = "Bajar", tint = Color.White)
                                            }

                                            IconButton(onClick = {
                                                lugaresRutaSeleccionada = lugaresRutaSeleccionada.filterIndexed { i, _ -> i != index }
                                                viewModel.actualizarOrdenLugares(rutaSeleccionada!!.ruta.id, lugaresRutaSeleccionada)
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.LightGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        content = {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Rutas Guardadas",
                            modifier = Modifier.padding(vertical = 0.dp) // 👈 reduce espacio arriba y abajo
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )


                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(rutas) { rutaConLugares ->
                        val ruta = rutaConLugares.ruta
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🛣️ ${ruta.nombre}", style = MaterialTheme.typography.titleMedium)
                                    Text("Lugares: ${rutaConLugares.lugares.size}", style = MaterialTheme.typography.bodySmall)
                                    Text("Categoría: ${ruta.categoria ?: "Sin categoría"}", style = MaterialTheme.typography.bodySmall)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                                    IconButton(onClick = {
                                        viewModel.recargarRutaSeleccionada(ruta.id) { rutaRecargada ->
                                            rutaSeleccionada = RutaConLugares(
                                                ruta = rutaRecargada.ruta,
                                                lugares = rutaRecargada.lugares
                                            )
                                            lugaresRutaSeleccionada = rutaRecargada.lugares
                                            nuevoNombreRuta = rutaRecargada.ruta.nombre
                                            categoriaSeleccionada = rutaRecargada.ruta.categoria

                                            scope.launch {
                                                scaffoldState.bottomSheetState.expand()
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Visibility, contentDescription = "Ver lugares")
                                    }

                                    IconButton(
                                        onClick = { navController.navigate("verRutaOffline/${ruta.id}") },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color(0xFF1976D2), // Azul tipo Google Maps
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = "Ver en mapa")
                                    }



                                    IconButton(onClick = {
                                        val lugares = rutaConLugares.lugares
                                        if (lugares.size >= 2) {
                                            val uri = buildString {
                                                append("https://www.google.com/maps/dir/?api=1")
                                                append("&travelmode=driving")
                                                append("&origin=${lugares.first().latitud},${lugares.first().longitud}")
                                                append("&destination=${lugares.last().latitud},${lugares.last().longitud}")
                                                if (lugares.size > 2) {
                                                    val waypoints = lugares.subList(1, lugares.lastIndex)
                                                        .joinToString("|") { "${it.latitud},${it.longitud}" }
                                                    append("&waypoints=$waypoints")
                                                }
                                            }

                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                                                setPackage("com.google.android.apps.maps")
                                            }

                                            try {
                                                navController.context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Log.e("RUTA_LAUNCH", "Error al abrir Google Maps: ${e.message}")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Directions, contentDescription = "Ir a ruta")
                                    }

                                    IconButton(onClick = {
                                        viewModel.eliminarRuta(ruta.id)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }


                            }
                        }
                    }
                }
            }
        }
    )
}