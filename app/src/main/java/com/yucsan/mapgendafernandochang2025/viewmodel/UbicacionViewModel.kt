package com.yucsan.mapgendafernandochang2025.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.repository.UbicacionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UbicacionViewModel(
    application: Application,
    private val repository: UbicacionRepository
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

    fun guardarUbicacion(nombre: String, lat: Double, lng: Double, tipo: String) {
        val nueva = UbicacionLocal(
            nombre  = nombre,
            latitud = lat,
            longitud= lng,
            tipo    = tipo
        )
        viewModelScope.launch {
            repository.insertarUbicacion(nueva)
        }
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
}
