package com.yucsan.mapgendafernandochang2025.screen.offLine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel

@Composable
fun MenuRutaOffline(
    navController: NavController,
    lugarViewModel: LugarViewModel
) {
    val buttonColor = Color(0xFFF5881F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "MENÚ OFFLINE",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        BotonOfflineAccion(
            texto = "Seleccionar Ubicación",
            colorFondo = buttonColor
        ) {
            navController.navigate("mapaSeleccionUbicacion")
        }

        BotonOfflineAccion(
            texto = "Ubicaciones",
            colorFondo = buttonColor
        ) {
            navController.navigate("ubicaciones")
        }

        BotonOfflineAccion(
            texto = "Crear Rutas Offline",
            colorFondo = buttonColor
        ) {
            navController.navigate("mapaubi?modoSeleccionUbicacion=false")
        }

        BotonOfflineAccion(
            texto = "Gestión de Rutas",
            colorFondo = buttonColor
        ) {
            navController.navigate("rutas")
        }

        BotonOfflineAccion(
            texto = "🗺️ Ver Lugares por Zonas"
        ) {
            lugarViewModel.agruparLugaresPorZona()
            navController.navigate("listalugares")
        }
    }
}

@Composable
fun BotonOfflineAccion(
    texto: String,
    colorFondo: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorFondo),
        shape = MaterialTheme.shapes.medium // mismo estilo cuadrado suave
    ) {
        Text(
            text = texto,
            color = Color.White,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

