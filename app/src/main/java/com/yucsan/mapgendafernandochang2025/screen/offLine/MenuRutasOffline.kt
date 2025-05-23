package com.yucsan.mapgendafernandochang2025.screen.offLine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MenuRutaOffline(
    navController: NavController
) {
    val buttonColor = Color(0xFFF5881F)
    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .height(50.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Button(
            onClick = {
                navController.navigate("mapaSeleccionUbicacion")

            },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Seleccionar Ubicación", color = Color.White)
        }

        Button(
            onClick = { navController.navigate( "ubicaciones") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Ubicaciones", color = Color.White)
        }

        Button(
            onClick = { navController.navigate("mapaubi?modoSeleccionUbicacion=false") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "crear Rutas Offline", color = Color.White)
        }

        Button(
            onClick = { navController.navigate("rutas") },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Gestion de Rutas", color = Color.White)
        }
    }
}
