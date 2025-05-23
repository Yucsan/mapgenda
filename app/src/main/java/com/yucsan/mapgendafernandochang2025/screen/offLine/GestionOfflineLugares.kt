package com.yucsan.mapgendafernandochang2025.screen.offLine


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel

import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionOfflineLugares(viewModel: LugarViewModel) {
    val todosLosLugares by viewModel.todosLosLugares.collectAsState()

    // Estados locales para búsqueda y filtros
    var textoBusqueda by remember { mutableStateOf(TextFieldValue("")) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    val categoriasDisponibles = todosLosLugares.mapNotNull { it.categoriaGeneral }.distinct().sorted()

    // Estados de plegado por categoría
    val estadosPlegado = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📋 Listado completo de lugares") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por nombre, subcategoría o dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown de categorías
            var expanded by remember { mutableStateOf(false) }

            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(categoriaSeleccionada ?: "Todas las categorías")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas") },
                        onClick = {
                            categoriaSeleccionada = null
                            expanded = false
                        }
                    )
                    categoriasDisponibles.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria) },
                            onClick = {
                                categoriaSeleccionada = categoria
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aplicar filtros
            val lugaresFiltrados = todosLosLugares.filter { lugar ->
                val coincideTexto = textoBusqueda.text.trim().lowercase(Locale.getDefault()).let { query ->
                    lugar.nombre.lowercase(Locale.getDefault()).contains(query) ||
                            lugar.subcategoria.lowercase(Locale.getDefault()).contains(query) ||
                            lugar.direccion.lowercase(Locale.getDefault()).contains(query)
                }
                val coincideCategoria = categoriaSeleccionada == null || lugar.categoriaGeneral == categoriaSeleccionada
                coincideTexto && coincideCategoria
            }.groupBy { it.categoriaGeneral ?: "Sin categoría" }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                lugaresFiltrados.forEach { (categoria, lugares) ->
                    val estaPlegado = estadosPlegado[categoria] ?: false

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    estadosPlegado[categoria] = !estaPlegado
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🔹 $categoria (${lugares.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = if (estaPlegado) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = null
                            )
                        }
                    }

                    if (!estaPlegado) {
                        items(lugares) { lugar ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(text = "📍 ${lugar.nombre}", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "🏷️ ${lugar.subcategoria} | 🌍 ${lugar.direccion}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
