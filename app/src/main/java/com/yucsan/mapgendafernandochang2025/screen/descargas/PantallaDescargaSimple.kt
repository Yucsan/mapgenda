package com.yucsan.mapgendafernandochang2025.screen.descargas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.componentes.navegacion.DialogoConfirmacionBorrado
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun PantallaDescargaSimple(viewModel: LugarViewModel, ubicacionViewModel: UbicacionViewModel, rutaViewModel: RutaViewModel) {
    val context = LocalContext.current
    val cargando by viewModel.cargando.collectAsState()
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarConteoPorCategoria()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ARCHIVOS EN LA NUBE",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))


        BotonAccion(texto = "Sube Lugares") {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.sincronizarLugaresConApi()
            }
        }

        BotonAccion(texto = "Sube Ubicaciones") {
            CoroutineScope(Dispatchers.IO).launch {
                ubicacionViewModel.sincronizarConApi(context)
            }
        }

        BotonAccion(texto = "SUBE RUTAS") {
            CoroutineScope(Dispatchers.IO).launch {
                rutaViewModel.subirRutasLocalesAlBackend(context)
            }
        }


        BotonAccion(texto = "Descargar mis Lugares") {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.descargarLugaresDesdeBackend(context)

            }
        }

        BotonAccion(texto = "Descargar mis ubicaciones") {
            CoroutineScope(Dispatchers.IO).launch {
                ubicacionViewModel.descargarUbicaciones(context)
            }
        }


        BotonAccion(texto = "Descargar RUTAS") {
            CoroutineScope(Dispatchers.IO).launch {
                rutaViewModel.descargarRutasDesdeBackend(context)
            }
        }

        if (cargando) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
            Text("Cargando lugares...", modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { mostrarDialogoBorrar = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = MaterialTheme.shapes.medium, // borde un poco cuadrado
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("🗑️ Borrar base de datos", color = MaterialTheme.colorScheme.onError)
        }

        if (mostrarDialogoBorrar) {
            DialogoConfirmacionBorrado(
                onConfirmar = {
                    mostrarDialogoBorrar = false
                    viewModel.limpiarLugares()
                    ubicacionViewModel.eliminarTodasUbicaciones()
                    Toast.makeText(context, "Lugares eliminados correctamente", Toast.LENGTH_SHORT)
                        .show()
                },
                onCancelar = { mostrarDialogoBorrar = false }
            )
        }
    }
}

@Composable
fun BotonAccion(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium, // borde un poco cuadrado
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}


