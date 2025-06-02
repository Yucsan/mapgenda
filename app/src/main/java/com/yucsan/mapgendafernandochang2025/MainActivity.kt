package com.yucsan.mapgendafernandochang2025

import UbicacionViewModelFactory
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UbicacionViewModel
import com.yucsan.mapgendafernandochang2025.ui.theme.MapGendaFernandoChang2025Theme
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
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

            val usuarioRepository = UsuarioRepository(database.UsuarioDao())
            val usuarioViewModel: UsuarioViewModel = viewModel(
                factory = UsuarioViewModelFactory(usuarioRepository)
            )

            val lugarOfflineViewModel: LugarRutaOfflineViewModel = viewModel()
            val rutaRepository = RutaRepository(database.rutaDao())
            val rutaViewModel: RutaViewModel = viewModel(factory = RutaViewModelFactory(rutaRepository, usuarioRepository))

            val mapViewModel: MapViewModel = viewModel()
            val navegacionViewModel: NavegacionViewModel = viewModel()
            val themeViewModel: ThemeViewModel by viewModels()
            val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()

            val ubicacionRepository = UbicacionRepository(database.ubicacionDao())

            val ubicacionViewModel: UbicacionViewModel = viewModel(
                factory = UbicacionViewModelFactory(application, ubicacionRepository,
                    UsuarioRepository(database.UsuarioDao())))


            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(usuarioViewModel)
            )

            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(authState) {
                val token = (authState as? AuthState.Autenticado)?.token
                RetrofitInstance.setTokenProvider { token }
            }

            authViewModel.initAuth(context)

            val application = context.applicationContext as Application

            val lugarViewModel: LugarViewModel = viewModel(
                factory = LugarViewModelFactory(application, authViewModel, usuarioViewModel, ubicacionViewModel)
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
