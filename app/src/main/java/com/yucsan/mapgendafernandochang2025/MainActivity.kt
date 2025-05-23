package com.yucsan.mapgendafernandochang2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yucsan.aventurafernandochang2025.room.DatabaseProvider
import com.yucsan.aventurafernandochang2025.viewmodel.RutaViewModelFactory
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository
import com.yucsan.mapgendafernandochang2025.repository.RutaRepository
import com.yucsan.mapgendafernandochang2025.repository.UbicacionRepository
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel

import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModelFactory

import com.yucsan.mapgendafernandochang2025.ui.theme.MapGendaFernandoChang2025Theme
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor
import com.yucsan.mapgendafernandochang2025.viewmodel.*

class MainActivity : ComponentActivity() {

    private lateinit var geoPosHandler: GeoPosHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        geoPosHandler = GeoPosHandler(this)

        setContent {
            val context = LocalContext.current
            val networkMonitor = remember { NetworkMonitor(context) }
            val database = DatabaseProvider.getDatabase(context)

            val gpsViewModel: GPSViewModel by viewModels {
                GPSViewModelFactory(geoPosHandler, this)
            }

            val lugarOfflineViewModel: LugarRutaOfflineViewModel = viewModel()
            val rutaRepository = RutaRepository(database.rutaDao())
            val rutaViewModel: RutaViewModel = viewModel(factory = RutaViewModelFactory(rutaRepository))
            val lugarViewModel: LugarViewModel = viewModel()
            val mapViewModel: MapViewModel = viewModel()
            val navegacionViewModel: NavegacionViewModel = viewModel()
            val themeViewModel: ThemeViewModel by viewModels()
            val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()

            val ubicacionRepository = UbicacionRepository(database.ubicacionDao())
            val ubicacionViewModel: UbicacionViewModel = viewModel(
                factory = UbicacionViewModelFactory(ubicacionRepository)
            )

            val usuarioRepository = UsuarioRepository(database.UsuarioDao())
            val usuarioViewModel: UsuarioViewModel = viewModel(
                factory = UsuarioViewModelFactory(usuarioRepository)
            )

            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(usuarioViewModel)
            )

            MapGendaFernandoChang2025Theme(darkTheme = isDarkMode) {
                MainScreen(
                    lugarViewModel = lugarViewModel,
                    mapViewModel = mapViewModel,
                    navegacionViewModel = navegacionViewModel,
                    gpsViewModel = gpsViewModel,
                    themeViewModel = themeViewModel,
                    ubicacionViewModel = ubicacionViewModel,
                    lugareOfflineViewModel = lugarOfflineViewModel,
                    rutaViewModel = rutaViewModel,
                    usuarioViewModel = usuarioViewModel,
                    authViewModel = authViewModel,
                    networkMonitor = networkMonitor
                )
            }
        }
    }
}
