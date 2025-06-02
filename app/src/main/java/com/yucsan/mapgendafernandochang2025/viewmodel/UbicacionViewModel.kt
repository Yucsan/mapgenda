package com.yucsan.mapgendafernandochang2025.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.repository.UbicacionRepository
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlin.math.pow


class UbicacionViewModel(
    application: Application,
    private val repository: UbicacionRepository,
    private val usuarioRepository: UsuarioRepository
) : AndroidViewModel(application) {

    // ———————————————————————————————————————————————————————————————
    // 1️⃣ Lista de ubicaciones persistidas (igual que antes)
    private val _ubicaciones = MutableStateFlow<List<UbicacionLocal>>(emptyList())
    val ubicaciones: StateFlow<List<UbicacionLocal>> = _ubicaciones.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // 2️⃣ Flujo con la lat/lng actual del dispositivo
    private val _ubicacionActual = MutableStateFlow<Pair<Double, Double>?>(null)
    val ubicacionActual: StateFlow<Pair<Double, Double>?> = _ubicacionActual.asStateFlow()

    // Cliente de ubicaciones de Google Play
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    init {
        // Empieza a observar tu BD/local
        viewModelScope.launch {
            query
                .flatMapLatest { texto ->
                    if (texto.isBlank()) repository.obtenerTodas()
                    else                   repository.buscar(texto)
                }
                .collect { lista ->
                    _ubicaciones.value = lista
                }
        }
    }

    fun actualizarQuery(nuevoTexto: String) {
        _query.value = nuevoTexto
    }

    @OptIn(UnstableApi::class)
    suspend fun guardarUbicacion(nombre: String, lat: Double, lng: Double, tipo: String): Boolean {
        val nueva = UbicacionLocal(
            nombre = nombre,
            latitud = lat,
            longitud = lng,
            tipo = tipo
        )

        val existentes = repository.obtenerTodas().first() // lista actual desde base de datos

        val umbral = when (tipo.trim().lowercase()) {
            "provincia" -> 8000.0   // 8 km
            "país" -> 100000.0      // 100 km
            else -> 100.0           // fallback
        }

        val existeCercana = existentes.any {
            it.tipo.trim().lowercase() == tipo.trim().lowercase() &&
                    calcularDistanciaEnMetros(it.latitud, it.longitud, lat, lng) <= umbral
        }

        return if (!existeCercana) {
            repository.insertarUbicacion(nueva)
            true
        } else {
            Log.w("UbicacionViewModel", "❌ Ya existe una ubicación '$tipo' cercana, no se guarda.")
            false
        }
    }



    private fun calcularDistanciaEnMetros(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Radio de la Tierra en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }


    fun eliminarUbicacion(ubicacion: UbicacionLocal) {
        viewModelScope.launch {
            repository.eliminarUbicacion(ubicacion)
        }
    }

    fun actualizarTipo(id: Int, nuevoTipo: String) {
        viewModelScope.launch {
            repository.actualizarTipo(id, nuevoTipo)
        }
    }

    fun actualizarUbicacionCompleta(id: Int, nombre: String, tipo: String) {
        viewModelScope.launch {
            repository.actualizarUbicacionCompleta(id, nombre, tipo)
        }
    }

    suspend fun guardarYRetornarId(ubicacion: UbicacionLocal): Long =
        repository.insertarUbicacionYRetornarId(ubicacion)
    // ———————————————————————————————————————————————————————————————

    /**
     * 3️⃣ Llama a este método (sin pasarle nada) desde tu Composable
     *    para que intente obtener la última ubicación conocida.
     */
    fun iniciarActualizacionUbicacion() {
        // Si ya tenemos un valor, no lo pedimos de nuevo
        if (_ubicacionActual.value != null) return

        // Verificamos permiso FINE_LOCATION
        val ctx = getApplication<Application>()
        val permisoOk = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permisoOk) {
            // Aquí podrías exponer otro StateFlow<Boolean> para que tu UI solicite el permiso
            return
        }

        viewModelScope.launch {
            try {
                val loc = fusedLocationClient.lastLocation.await()
                loc?.let {
                    _ubicacionActual.value = it.latitude to it.longitude
                }
            } catch (e: Exception) {
                // Maneja/loguea el error si quieres
            }
        }
    }


    fun eliminarTodasUbicaciones() {
        viewModelScope.launch {
            repository.eliminarTodas()
        }
    }

///----------------------------- funciones BACKEND -----------------------------


    fun sincronizarConApi(context: Context) {
        viewModelScope.launch {
            val usuarioId = usuarioRepository.obtenerUsuario()?.id
            val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("jwt_token", null)

            if (usuarioId != null && token != null) {
                try {
                    repository.sincronizarUbicacionesConBackend(usuarioId, token)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "📡 Ubicaciones sincronizadas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ Falló sincronización: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun descargarUbicaciones(context: Context) {
        viewModelScope.launch {
            val usuarioId = usuarioRepository.obtenerUsuario()?.id
            val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("jwt_token", null)

            if (usuarioId != null && token != null) {
                try {
                    repository.descargarUbicacionesDesdeBackend(usuarioId, token)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "⬇️ Ubicaciones descargadas", Toast.LENGTH_SHORT).show()
                        Log.d("UbicacionViewModel", "⬇️ Ubicaciones descargadas correctamente")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ Falló descarga: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.d("UbicacionViewModel", "❌ Error al descargar ubicaciones: ${e.message}", e)
                    }
                }
            }
        }
    }




}
