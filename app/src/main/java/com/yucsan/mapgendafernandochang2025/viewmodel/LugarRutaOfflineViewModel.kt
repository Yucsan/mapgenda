package com.yucsan.mapgendafernandochang2025.viewmodel


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.yucsan.mapgendafernandochang2025.repository.LugarRepository
import kotlinx.coroutines.launch
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.yucsan.aventurafernandochang2025.room.DatabaseProvider
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow




class LugarRutaOfflineViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).lugarDao()
    private val repository = LugarRepository(dao)

    // Todos los lugares visibles en la pantalla de rutas offline
    private val _lugaresOffline = MutableStateFlow<List<LugarLocal>>(emptyList())
    val lugaresOffline: StateFlow<List<LugarLocal>> = _lugaresOffline

    // Categorías seleccionadas como filtros
    private val _categoriasSeleccionadasOffline = MutableStateFlow<Set<String>>(emptySet())
    val categoriasSeleccionadasOffline: StateFlow<Set<String>> = _categoriasSeleccionadasOffline

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _filtrosActivos = MutableStateFlow<List<String>>(emptyList())
    val filtrosActivos: StateFlow<List<String>> = _filtrosActivos

    private val _conteoPorSubcategoria = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteoPorSubcategoria: StateFlow<Map<String, Int>> = _conteoPorSubcategoria

    private val _ubicacion = MutableStateFlow<Pair<Double, Double>?>(null)
    val ubicacion: StateFlow<Pair<Double, Double>?> get() = _ubicacion

    private val _categoriasSeleccionadas = MutableStateFlow<List<String>>(emptyList())
    val categoriasSeleccionadas: StateFlow<List<String>> = _categoriasSeleccionadas.asStateFlow()

    private val _radio = MutableStateFlow(8000f)
    val distanciaSeleccionada: StateFlow<Float> get() = _radio.asStateFlow()

    private val _todosLosLugares = MutableStateFlow<List<LugarLocal>>(emptyList())
    val todosLosLugares: StateFlow<List<LugarLocal>> = _todosLosLugares.asStateFlow()

    val latitud: Double? get() = ubicacion.value?.first
    val longitud: Double? get() = ubicacion.value?.second

    private val _debeAplicarFiltro = MutableStateFlow(false)

    private val _conteoPorSubcategoriaFiltrado = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteoPorSubcategoriaFiltrado: StateFlow<Map<String, Int>> = _conteoPorSubcategoriaFiltrado

    // Subcategorías seleccionadas temporalmente (chips marcados)
    private val _filtrosTemporales = MutableStateFlow<List<String>>(emptyList())
    val filtrosTemporales: StateFlow<List<String>> = _filtrosTemporales.asStateFlow()

    // Categorías padre desplegadas temporalmente
    private val _categoriasExpandibles = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val categoriasExpandibles: StateFlow<Map<String, Boolean>> = _categoriasExpandibles.asStateFlow()



    // menu Pantalla offline estados chips
    fun establecerFiltrosTemporales(subcategorias: List<String>) {
        Log.d("DBG", ">> setTemporales: $subcategorias")
        _filtrosTemporales.value = subcategorias
    }

    fun establecerCategoriasExpandibles(nuevas: Map<String, Boolean>) {
        _categoriasExpandibles.value = nuevas
    }



    fun dispararFiltro() {
        _debeAplicarFiltro.value = true
    }

    fun limpiarUbicacion() {
        _ubicacion.value = null
    }


    init {
        viewModelScope.launch {
            _debeAplicarFiltro
                .filter { it } // solo si es true
                .flatMapLatest {
                    val categorias = _categoriasSeleccionadas.value
                    val ubicacion = _ubicacion.value
                    val radio = _radio.value

                    Log.d("DEBUG_COMBINE", "🧪 DISPARADOR activado")
                    Log.d("DEBUG_COMBINE", "📍 Ubicación usada: $ubicacion")
                    Log.d("DEBUG_COMBINE", "📏 Radio: $radio")
                    Log.d("DEBUG_COMBINE", "📂 Subcategorías: $categorias")

                    if (categorias.isEmpty() || ubicacion == null) {
                        flowOf(emptyList())
                    } else {
                        repository.obtenerPorSubcategoriasCercanosHaversine(
                            subcategorias = categorias,
                            latitud = ubicacion.first,
                            longitud = ubicacion.second,
                            radio = radio
                        )
                    }
                }
                .collect { lugares: List<LugarLocal> ->
                    Log.d("DEBUG_COMBINE", "📍 ${lugares.size} lugares encontrados")
                    _lugaresOffline.value = lugares
                    _debeAplicarFiltro.value = false
                }

        }

        viewModelScope.launch {
            combine(_ubicacion.filterNotNull(), _radio) { ubicacion, radio ->
                ubicacion to radio
            }.flatMapLatest { (ubicacion, radio) ->
                flow {
                    val conteo = repository.contarLugaresPorSubcategoriaFiltrando(
                        latitud = ubicacion.first,
                        longitud = ubicacion.second,
                        radio = radio
                    )
                    emit(conteo)
                }
            }.collect { conteosFiltrados ->
                _conteoPorSubcategoriaFiltrado.value = conteosFiltrados
            }
        }


    }



    // Agregar o quitar una categoría del filtro
    fun toggleCategoriaOffline(categoria: String) {
        _categoriasSeleccionadasOffline.update { set ->
            if (set.contains(categoria)) set - categoria else set + categoria
        }
    }

    // Cargar lugares (por ejemplo, desde repositorio o mock local)
    fun setLugaresOffline(nuevos: List<LugarLocal>) {
        _lugaresOffline.value = nuevos
    }

    // Agregar un nuevo lugar a la lista
    fun agregarLugarOffline(lugar: LugarLocal) {
        _lugaresOffline.update { it + lugar }
    }

    // Retorna los lugares filtrados según las categorías seleccionadas
    fun getLugaresFiltradosOffline(): List<LugarLocal> {
        val filtros = _categoriasSeleccionadasOffline.value
        return if (filtros.isEmpty()) {
            _lugaresOffline.value
        } else {
            _lugaresOffline.value.filter { filtros.contains(it.subcategoria) }
        }
    }

    fun actualizarFiltrosActivos(subcategorias: Set<String>) {
        _filtrosActivos.value = subcategorias.toList()
    }

    //------------------------------------------------------------*** REVIZAR 1
    fun actualizarCategorias(nuevas: Set<String>) {
        _categoriasSeleccionadas.value = nuevas.toList()
    }

    fun actualizarRadio(nuevoRadio: Float) {
        _radio.value = nuevoRadio
    }

    fun cargarConteoSubcategorias() {
        viewModelScope.launch {
            _conteoPorSubcategoria.value = repository.contarLugaresPorSubcategoria()
        }
    }

    fun observarTodosLosLugares() {
        viewModelScope.launch {
            repository.obtenerTodos().collect {
                _todosLosLugares.value = it
                Log.d("LugarViewModel", "📦 Todos los lugares: ${it.size}")
                it.forEach { lugar -> Log.d("LugarViewModel", "👉 ${lugar.nombre} - ${lugar.subcategoria}") }
            }
        }
    }

    // AUTO UBICACION
    fun iniciarActualizacionUbicacion(context: Context) {
        if (_ubicacion.value != null) return // Ya tenemos ubicación
        val fused = LocationServices.getFusedLocationProviderClient(context)
        viewModelScope.launch {
            try {
                val tienePermiso = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!tienePermiso) {
                    Log.w("LugarViewModel", "⛔ Sin permiso de ubicación")
                    return@launch
                }

                fused.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            Log.d("LugarViewModel", "📍 Ubicación obtenida automáticamente: ${location.latitude}, ${location.longitude}")
                            _ubicacion.value = location.latitude to location.longitude
                        } else {
                            Log.w("LugarViewModel", "⚠️ No se pudo obtener la ubicación (es null)")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("LugarViewModel", "❌ Error al obtener la ubicación", exception)
                    }

            } catch (e: SecurityException) {
                Log.e("LugarViewModel", "❌ Seguridad: sin permiso", e)
            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error al obtener ubicación", e)
            }
        }
    }

    fun actualizarUbicacionManual(latLng: LatLng) {
        _ubicacion.value = null
        _ubicacion.value = latLng.latitude to latLng.longitude
    }


    fun aplicarFiltroSubcategoriasCercanas() {
        val subcategorias = _filtrosActivos.value
        val ubic = _ubicacion.value
        val radio = _radio.value

        if (subcategorias.isNotEmpty() && ubic != null) {
            viewModelScope.launch {
                repository
                    .obtenerPorSubcategoriasCercanosHaversine(
                        subcategorias,
                        ubic.first,
                        ubic.second,
                        radio

                ).collect { lugaresFiltrados: List<LugarLocal> ->
                _lugaresOffline.value = lugaresFiltrados
                Log.d("ViewModel", "🧭 Lugares filtrados cargados: ${lugaresFiltrados.size}")
                 Log.d("ViewModel", "📏 Radio aplicado: $radio m")

                    }
            }
        } else {
            Log.w("ViewModel", "⚠️ Falta ubicación o subcategorías para filtrar")
        }
    }

    fun seleccionarUbicacionYAplicarFiltros(nuevaUbicacion: LatLng) {
        viewModelScope.launch {
            // 1️⃣ Actualizar ubicación
            _ubicacion.value = nuevaUbicacion.latitude to nuevaUbicacion.longitude

            // 2️⃣ Esperar a que el conteoPorSubcategoriaFiltrado se actualice
            delay(300) // suficiente para que Room y Flow actualicen (ajusta si es necesario)

            val conteo = _conteoPorSubcategoriaFiltrado.value
            val subcatsDisponibles = conteo
                .filter { it.key.isNotBlank() && it.value > 0 }
                .keys.toList()

            // 3️⃣ Actualizar filtros activos
            _filtrosActivos.value = subcatsDisponibles
            _categoriasSeleccionadas.value = subcatsDisponibles

            // 4️⃣ Aplicar filtro manual con ubicación y subcategorías
            aplicarFiltroManualConParametros(
                subcategorias = subcatsDisponibles,
                centro = nuevaUbicacion,
                radio = _radio.value
            )
        }
    }

    fun aplicarFiltroManualConParametros(
        subcategorias: List<String>,
        centro: LatLng,
        radio: Float
    ) {
        viewModelScope.launch {
            Log.d("FiltroManual", "🧭 Iniciando filtro manual con:")
            Log.d("FiltroManual", "📍 Centro: (${centro.latitude}, ${centro.longitude})")
            Log.d("FiltroManual", "📏 Radio: $radio metros")
            Log.d("FiltroManual", "🗂️ Subcategorías: $subcategorias")

            try {
                repository.obtenerPorSubcategoriasCercanosHaversine(
                    subcategorias = subcategorias,
                    latitud = centro.latitude,
                    longitud = centro.longitude,
                    radio = radio
                ).collect { lugaresFiltrados: List<LugarLocal> ->
                    Log.d("FiltroManual", "✅ Filtro completado: ${lugaresFiltrados.size} lugares")
                    _lugaresOffline.value = lugaresFiltrados

                    // 🔄 Actualiza la ubicación usada para centrar el mapa, si es necesario
                    actualizarUbicacionManual(centro)
                }
            } catch (e: Exception) {
                Log.e("FiltroManual", "❌ Error al aplicar filtro manual", e)
            }
        }
    }


    //para pruebas
    /*
    fun probarFiltroManual() {
        viewModelScope.launch {
            repository.obtenerPorSubcategoriasCercanosHaversine(
                subcategorias = listOf("restaurant", "park", "cafe"), // usa subcategorías reales
                latitud = 42.8782,  // Latitud de Santiago de Compostela
                longitud = -8.5448, // Longitud de Santiago de Compostela
                radio = 2000f // 2 km de radio
            ).collect { lugares: List<LugarLocal> ->
                Log.d("PruebaManual", "🔍 Lugares en Santiago: ${lugares.size}")
                lugares.forEach {
                    Log.d("PruebaManual", "📍 ${it.nombre} - (${it.latitud}, ${it.longitud})")
                }
            }
        }
    }*/


    fun probarFiltroManual2() {
        viewModelScope.launch {
            repository.obtenerPorSubcategoriasCercanosHaversine(
                subcategorias = listOf("restaurant", "park", "cafe"),
                latitud = 42.8782,
                longitud = -8.5448,
                radio = 2000f
            ).collect { lugares: List<LugarLocal> ->
                _lugaresOffline.value = lugares // <-- Asegúrate de actualizar el StateFlow
                Log.d("PruebaManual", "🔍 Lugares en Santiago: ${lugares.size}")
                lugares.forEach {
                    Log.d("PruebaManual", "📍 ${it.nombre} - (${it.latitud}, ${it.longitud})")
                }
            }
        }
    }



}
