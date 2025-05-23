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
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.componentes.navegacion.DialogoConfirmacionBorrado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDescargaSimple(viewModel: LugarViewModel) {
    val context = LocalContext.current
    val cargando by viewModel.cargando.collectAsState()
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarConteoPorCategoria()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Descarga Simple") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Contenido del Perfil con opciones avanzadas")
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
                            .edit().remove("descarga_base_ok").apply()

                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.descargarCategoriasBaseAmplias(
                                context,
                                "AIzaSyDSXCx8phqDDPCvcV4hvbCYYkG-OB4ElHk"
                            )
                        }
                    }
                ) {
                    Text("🔄 Forzar recarga desde API")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.descargarCategoriasPersonalizadas(
                                context,
                                "AIzaSyDSXCx8phqDDPCvcV4hvbCYYkG-OB4ElHk"
                            )
                        }
                    }
                ) {
                    Text("🆕 Descargar categorías personalizadas")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.sincronizarLugaresConApi()
                        }
                    }
                ) {
                    Text("☁️ Sincronizar con Backend envia android")
                }

                if (cargando) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                    Text("Cargando lugares...", modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { mostrarDialogoBorrar = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("🗑️ Borrar base de datos", color = MaterialTheme.colorScheme.onError)
                }

                if (mostrarDialogoBorrar) {
                    com.yucsan.mapgendafernandochang2025.componentes.navegacion.DialogoConfirmacionBorrado(
                        onConfirmar = {
                            mostrarDialogoBorrar = false
                            viewModel.limpiarLugares()
                            Toast.makeText(context, "Lugares eliminados correctamente", Toast.LENGTH_SHORT).show()
                        },
                        onCancelar = { mostrarDialogoBorrar = false }
                    )
                }
            }
        }
    }
}
