package com.yucsan.mapgendafernandochang2025.screens.mapa.alertas

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.Polyline
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal

import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.NavegacionViewModel


@Composable
fun DetalleLugarDialog(
    lugar: LugarLocal,
    apiKey: String,
    viewModelLugar: LugarViewModel,
    navegacionViewModel: NavegacionViewModel,
    onDismiss: () -> Unit,
    ubicacionActual: Pair<Double, Double>?,
    hayConexion: Boolean
) {
    val context = LocalContext.current
    var modoEdicion by remember { mutableStateOf(false) }
    var nombreEditado by remember { mutableStateOf("") }
    var descripcionEditada by remember { mutableStateOf("") }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    fun construirUrlFoto(photoReference: String, apiKey: String, maxWidth: Int = 600): String {
        return if (photoReference.startsWith("http")) {
            photoReference
        } else {
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=$apiKey"
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {},
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ✅ Imagen solo si hay conexión
                if (hayConexion && lugar.photoReference != null) {
                    val url = construirUrlFoto(lugar.photoReference, apiKey)
                    AsyncImage(
                        model = url,
                        contentDescription = "Imagen del lugar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin conexión para cargar imagen", color = Color.Gray)
                    }
                }

                if (modoEdicion) {
                    OutlinedTextField(
                        value = nombreEditado,
                        onValueChange = { nombreEditado = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descripcionEditada,
                        onValueChange = { descripcionEditada = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val lugarActualizado = lugar.copy(
                                nombre = nombreEditado,
                                direccion = descripcionEditada
                            )
                            viewModelLugar.actualizarLugarManual(lugarActualizado)
                            Toast.makeText(context, "Lugar actualizado", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Guardar")
                    }
                } else {
                    Text("📍 Dirección: ${lugar.direccion}")
                    lugar.rating?.let { Text("⭐️ Rating: $it") }
                    lugar.userRatingsTotal?.let { Text("🗣️ Opiniones: $it") }
                    lugar.precio?.let { Text("💰 Precio: ${"$".repeat(it)}") }
                    lugar.abiertoAhora?.let {
                        Text("⏰ Abierto ahora: ${if (it) "Sí" else "No"}")
                    }
                    lugar.businessStatus?.let { Text("📌 Estado: $it") }
                    lugar.tipos?.let { Text("🏷️ Tipos: ${it.joinToString()}") }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = {
                            nombreEditado = lugar.nombre
                            descripcionEditada = lugar.direccion
                            modoEdicion = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }

                        // ✅ Botón de navegación deshabilitado si no hay conexión
                        IconButton(
                            onClick = {
                                ubicacionActual?.let { origen ->
                                    val destino = lugar.latitud to lugar.longitud
                                    val uri = Uri.parse("http://maps.google.com/maps?saddr=${origen.first},${origen.second}&daddr=${destino.first},${destino.second}&mode=w")
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Google Maps no está instalado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = hayConexion // 👈 importante
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = "Abrir en Google Maps")
                        }

                        IconButton(onClick = {
                            mostrarConfirmacionEliminar = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    )


    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModelLugar.eliminarLugar(lugar.id)
                    mostrarConfirmacionEliminar = false
                    onDismiss()
                    Toast.makeText(context, "Lugar eliminado", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("¿Eliminar lugar?") },
            text = { Text("¿Seguro que deseas eliminar este lugar? Esta acción no se puede deshacer.") }
        )
    }
}
