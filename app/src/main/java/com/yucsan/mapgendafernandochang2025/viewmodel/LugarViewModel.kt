package com.yucsan.mapgendafernandochang2025.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import java.io.File
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.yucsan.aventurafernandochang2025.room.DatabaseProvider
import com.yucsan.mapgendafernandochang2025.dto.toEntity


import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.repository.LugarRepository
import com.yucsan.mapgendafernandochang2025.servicio.backend.RetrofitInstance
import com.yucsan.mapgendafernandochang2025.servicio.maps.directions.places.PlacesService
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
import com.yucsan.mapgendafernandochang2025.util.CategoriaMapper
import com.yucsan.mapgendafernandochang2025.util.categoriasPersonalizadas
import kotlinx.coroutines.withContext


class LugarViewModel(
    application: Application,
    private val authViewModel: AuthViewModel,
    private val usuarioViewModel: UsuarioViewModel
) : AndroidViewModel(application) {

    // RUTAS
    private val _lugaresSeleccionadosParaRuta = mutableStateListOf<LugarLocal>()
    val lugaresSeleccionadosParaRuta: List<LugarLocal> get() = _lugaresSeleccionadosParaRuta
//---

    private val _ultimaCategoriaAgregada = MutableStateFlow<String?>(null)
    val ultimaCategoriaAgregada: StateFlow<String?> = _ultimaCategoriaAgregada

    private val _ultimaSubcategoriaAgregada = MutableStateFlow<String?>(null)
    val ultimaSubcategoriaAgregada: StateFlow<String?> = _ultimaSubcategoriaAgregada
//---

    private val _ultimoLugarAgregadoId = MutableStateFlow<String?>(null)
    val ultimoLugarAgregadoId: StateFlow<String?> = _ultimoLugarAgregadoId

    private val _lugaresFiltrados = MutableStateFlow<List<LugarLocal>>(emptyList())
    val lugaresFiltrados: StateFlow<List<LugarLocal>> = _lugaresFiltrados.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val placesService = PlacesService()
    private val dao = DatabaseProvider.getDatabase(application).lugarDao()
    private val repository = LugarRepository(dao)

    private val _categoriasSeleccionadas = MutableStateFlow<List<String>>(emptyList())
    val categoriasSeleccionadas: StateFlow<List<String>> = _categoriasSeleccionadas.asStateFlow()
    private val _ubicacion = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _radio = MutableStateFlow(2000f)

    private val _lugares = MutableStateFlow<List<LugarLocal>>(emptyList())
    val lugares: StateFlow<List<LugarLocal>> = _lugares.asStateFlow()

    private val _todosLosLugares = MutableStateFlow<List<LugarLocal>>(emptyList())
    val todosLosLugares: StateFlow<List<LugarLocal>> = _todosLosLugares.asStateFlow()

    val distanciaSeleccionada: StateFlow<Float> get() = _radio.asStateFlow()
    val ubicacion: StateFlow<Pair<Double, Double>?> get() = _ubicacion.asStateFlow()

    // conteo de lugares por categoria
    private val _conteoPorCategoria = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteoPorCategoria: StateFlow<Map<String, Int>> = _conteoPorCategoria

    // 🔒 SharedPreferences para control de descarga
    private fun marcarDescargaBaseComoCompleta(context: Context) {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("descarga_base_ok", true).apply()
    }

    private fun fueDescargaBaseCompletada(context: Context): Boolean {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("descarga_base_ok", false)
    }

    private val _conteoPorSubcategoria = MutableStateFlow<Map<String, Int>>(emptyMap())
    val conteoPorSubcategoria: StateFlow<Map<String, Int>> = _conteoPorSubcategoria

    private val _filtrosActivos = MutableStateFlow<List<String>>(emptyList())
    val filtrosActivos: StateFlow<List<String>> = _filtrosActivos


    // desacargas personalizadas flags
    private val _eventoDescargaPersonalizada = MutableStateFlow(false)
    val eventoDescargaPersonalizada: StateFlow<Boolean> = _eventoDescargaPersonalizada



    fun actualizarFiltrosActivos(subcategorias: Set<String>) {
        _filtrosActivos.value = subcategorias.toList()
    }

    fun cargarConteoSubcategorias() {
        viewModelScope.launch {
            _conteoPorSubcategoria.value = repository.contarLugaresPorSubcategoria()
        }
    }

    fun obtenerUsuarioId(): String? {
        val estado = authViewModel.authState.value
        return if (estado is AuthState.Autenticado) {
            estado.usuario.id.toString()
        } else null
    }


    init {
        viewModelScope.launch {
            combine(
                _categoriasSeleccionadas,
                _ubicacion.filterNotNull(),
                _radio
            ) { categorias, ubicacion, radio ->

                Log.d("DEBUG_COMBINE", "🔄 Combine activado con:")
                Log.d("DEBUG_COMBINE", "📍 Ubicación: $ubicacion")
                Log.d("DEBUG_COMBINE", "🗂️ Categorías: $categorias")
                Log.d("DEBUG_COMBINE", "📏 Radio: $radio")

                Triple(categorias, ubicacion, radio)
            }.flatMapLatest { (categorias, ubicacion, radio) ->

                Log.d("DEBUG_COMBINE", "Categorias para consulta DB: $categorias")

                if (categorias.isEmpty()) return@flatMapLatest flowOf(emptyList())

                repository.obtenerPorSubcategoriasCercanos(
                    subcategorias = categorias,
                    latitud = ubicacion.first,
                    longitud = ubicacion.second,
                    radio = radio
                )
            }.collect {
                Log.d("LugarViewModel", "📥 Recibidos ${it.size} lugares desde DB")
                _lugares.value = it

                it.forEach { lugar ->
                    Log.d(
                        "lugares",
                        "📍${lugar.id} ${lugar.nombre} en ${lugar.latitud},${lugar.longitud}"
                    )
                }
            }

        }

        // Escucha cambios en filtros activos
        viewModelScope.launch {
            combine(_lugares, _filtrosActivos) { lugares, filtros ->
                Log.d("DEBUG_FILTER", "🧪 Filtros activos: $filtros")
                Log.d("DEBUG_FILTER", "📦 Lugares previos: ${lugares.size}")
                lugares.filter { it.subcategoria in filtros }
            }.collect {
                Log.d("DEBUG_FILTER", "✅ lugaresFiltrados emitido: ${it.size}")
                _lugaresFiltrados.value = it
            }
        }



    }

    private fun actualizarFiltradoInterno() {
        val filtros = _filtrosActivos.value

        Log.d("DEBUG_VIEWMODEL", "🎯 Filtros activos en ViewModel: $filtros")
        Log.d("DEBUG_VIEWMODEL", "📦 Total lugares actuales: ${_lugares.value.size}")

        _lugaresFiltrados.value = if (filtros.isEmpty()) {
            _lugares.value
        } else {
            _lugares.value.filter { lugar -> filtros.contains(lugar.subcategoria) }
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

                val location = fused.lastLocation.await()
                if (location != null) {
                    Log.d("LugarViewModel", "📍 Ubicación obtenida automáticamente")
                    _ubicacion.value = location.latitude to location.longitude
                } else {
                    Log.w("LugarViewModel", "⚠️ No se pudo obtener la ubicación")
                }
            } catch (e: SecurityException) {
                Log.e("LugarViewModel", "❌ Seguridad: sin permiso", e)
            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error al obtener ubicación", e)
            }
        }
    }

    fun observarTodosLosLugares() {
        viewModelScope.launch {
            repository.obtenerTodos().collect {
                _todosLosLugares.value = it
                Log.d("LugarViewModel", "📦 Todos los lugares: ${it.size}")
            }
        }
    }

    fun actualizarCategorias(nuevas: Set<String>) {
        Log.d("LugarViewModel", "✏️ Categorías actualizadas: $nuevas")

        // Solo actualiza si cambia el contenido (fuerza emisión manual si igual)
        if (_categoriasSeleccionadas.value.toSet() != nuevas) {
            _categoriasSeleccionadas.value = nuevas.toList()
        } else {
            // Forzar nueva emisión para reactivar el combine
            _categoriasSeleccionadas.value = emptyList()
            _categoriasSeleccionadas.value = nuevas.toList()
        }
    }


    fun actualizarUbicacion(lat: Double, lng: Double) {
        Log.d("LugarViewModel", "📍 Ubicación actualizada: $lat, $lng")
        _ubicacion.value = lat to lng
    }

    fun actualizarRadio(nuevoRadio: Float) {
        Log.d("LugarViewModel", "📏 Radio actualizado: $nuevoRadio")
        _radio.value = nuevoRadio
    }

    fun limpiarLugares() {
        viewModelScope.launch {
            Log.d("LugarViewModel", "🧹 Limpiando lugares...")
            repository.limpiarTodo()
        }
    }


    fun generarPuntosAlrededor(
        centroLat: Double,
        centroLng: Double,
        cantidadPorLado: Int = 1,
        separacionGrados: Double = 0.02 // Aproximadamente 2.2 km
    ): List<Pair<Double, Double>> {
        val puntos = mutableListOf<Pair<Double, Double>>()

        for (latOffset in -cantidadPorLado..cantidadPorLado) {
            for (lngOffset in -cantidadPorLado..cantidadPorLado) {
                val nuevaLat = centroLat + (latOffset * separacionGrados)
                val nuevaLng = centroLng + (lngOffset * separacionGrados)
                puntos.add(nuevaLat to nuevaLng)
            }
        }
        return puntos
    }

    fun cargarConteoPorCategoria() {
        viewModelScope.launch {
            _conteoPorCategoria.value = repository.contarLugaresPorCategoria()
        }
    }

    // Esta función guarda la lista de lugares en un archivo CSV en la carpeta de descargas
    private fun guardarCsv(context: Context, nombreArchivo: String, lugares: List<LugarLocal>) {
        try {
            val csvHeader =
                "id,nombre,direccion,latitud,longitud,categoriaGeneral,subcategoria,rating,userRatingsTotal"
            val csvBody = lugares.joinToString("\n") { lugar ->
                listOf(
                    lugar.id,
                    lugar.nombre.replace(",", " "),
                    lugar.direccion.replace(",", " "),
                    lugar.latitud,
                    lugar.longitud,
                    lugar.categoriaGeneral ?: "",
                    lugar.subcategoria,
                    lugar.rating ?: 0.0,
                    lugar.userRatingsTotal ?: 0
                ).joinToString(",")
            }

            val fullText = csvHeader + "\n" + csvBody

            // 📁 Ruta pública de Descargas
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val archivo = File(downloadsDir, nombreArchivo)
            archivo.writeText(fullText)

            Log.d("CSV_EXPORT", "✅ Archivo guardado en: ${archivo.absolutePath}")
            Toast.makeText(context, "Archivo guardado en Descargas", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("CSV_EXPORT", "❌ Error exportando a CSV", e)
        }
    }

// REFACTORIZAR
    fun guardarCsvCompleto(context: Context, nombreArchivo: String, lugares: List<LugarLocal>) {
        try {
            val csvHeader = listOf(
                "id", "nombre", "direccion", "latitud", "longitud", "categoriaGeneral", "subcategoria",
                "tipos", "rating", "userRatingsTotal", "precio", "abiertoAhora", "estado", "photoReference", "fuente"
            ).joinToString(",")

            val csvBody = lugares.joinToString("\n") { lugar ->
                listOf(
                    lugar.id,
                    lugar.nombre.replace(",", " "),
                    lugar.direccion.replace(",", " "),
                    lugar.latitud.toString(),
                    lugar.longitud.toString(),
                    lugar.categoriaGeneral ?: "",
                    lugar.subcategoria ?: "",
                    lugar.tipos?.joinToString(";") ?: "",
                    lugar.rating?.toString() ?: "",
                    lugar.userRatingsTotal?.toString() ?: "",
                    lugar.precio?.toString() ?: "",
                    lugar.abiertoAhora?.toString() ?: "",
                    lugar.estado ?: "",
                    lugar.photoReference ?: "",
                    lugar.fuente ?: ""
                ).joinToString(",")
            }

            val fullText = "$csvHeader\n$csvBody"

            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val archivo = File(downloadsDir, nombreArchivo)
            archivo.writeText(fullText)

            Log.d("CSV_EXPORT", "✅ Archivo guardado en: ${archivo.absolutePath}")
            Toast.makeText(context, "📁 CSV guardado en Descargas", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("CSV_EXPORT", "❌ Error exportando CSV", e)
        }
    }


// DESCARGA PRINCIPAL --------------------------------------------------- REFACTORIZAR
    fun descargarCategoriasBaseAmplias(
        context: Context,
        apiKey: String,
        cantidadPorLado: Int = 0,
        radioMetros: Float = 3000f
    ) {
        viewModelScope.launch {
            val ubicacionActual = _ubicacion.value

            if (ubicacionActual == null) {
                Log.w("LugarViewModel", "❗ Ubicación no disponible para descarga ampliada.")
                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                return@launch
            }

            _cargando.value = true

            val puntos = generarPuntosAlrededor(
                centroLat = ubicacionActual.first,
                centroLng = ubicacionActual.second,
                cantidadPorLado = cantidadPorLado
            ).toMutableList()

            val categoriasBase = listOf(
                "Comida", "Compras", "Hospedaje", "Transporte", "Ocio", "Cultura", "Aire libre"
            )

            val resumenDescarga = mutableListOf<String>()

            val todosDescargados = mutableListOf<LugarLocal>()
            val todosFiltrados = mutableListOf<LugarLocal>()
            val todosGuardados = mutableListOf<LugarLocal>()

            Log.d("LugarViewModel", "🧭 Generando cuadrantes (incluyendo ubicación actual):")
            puntos.forEachIndexed { index, (lat, lng) ->
                Log.d("LugarViewModel", "🗺 Punto $index → Lat: $lat, Lng: $lng")
            }

            try {
                for (categoria in categoriasBase) {
                    val subtiposList = CategoriaMapper.subcategoriasPorCategoria(categoria)

                    for ((lat, lng) in puntos) {
                        for (subtipo in subtiposList) {
                            try {
                                val lugares = placesService.obtenerLugaresCercanos(
                                    lat = lat,
                                    lng = lng,
                                    apiKey = apiKey,
                                    type = subtipo,
                                    maxDistancia = radioMetros
                                )

                                Log.d(
                                    "📦LugarViewModel",
                                    "🔍 Categoría: $categoria, Subtipo: $subtipo → Resultado: ${lugares.size} lugares en lat:$lat lng:$lng"
                                )

                                val mapeados = lugares.map { lugar ->
                                    lugar.copy(
                                        subcategoria = subtipo,
                                        photoReference = lugar.photoReference,
                                        businessStatus = lugar.businessStatus,
                                        userRatingsTotal = lugar.userRatingsTotal,
                                        abiertoAhora = lugar.abiertoAhora
                                    )
                                }

                                todosDescargados.addAll(mapeados)

                                val filtrados = mapeados.filter {
                                    it.photoReference != null &&
                                            (it.rating ?: 0f) >= 3.0f &&
                                            (it.userRatingsTotal ?: 0) >= 5
                                }

                                todosFiltrados.addAll(filtrados)

                                val unicos =
                                    filtrados.distinctBy { "${it.nombre}-${it.latitud}-${it.longitud}" }
                                repository.insertarLugares(unicos)

                                todosGuardados.addAll(unicos)

                                Log.d(
                                    "LugarViewModel",
                                    "✅ Insertados ${unicos.size} lugares para $subtipo en punto $lat,$lng"
                                )
                                resumenDescarga.add("• $categoria - $subtipo: ${unicos.size} lugares")

                            } catch (e: Exception) {
                                Log.e(
                                    "LugarViewModel",
                                    "❌ Error al descargar tipo $subtipo en $lat,$lng",
                                    e
                                )
                            }
                        }
                    }
                }

                // Guardar CSV
                guardarCsv(context, "lugares_descargados.csv", todosDescargados)
                guardarCsv(context, "lugares_filtrados.csv", todosFiltrados)
                guardarCsv(context, "lugares_guardados.csv", todosGuardados)

                marcarDescargaBaseComoCompleta(context)
                Toast.makeText(context, "✅ Lugares base cargados", Toast.LENGTH_SHORT).show()
                Log.d("LugarViewModel", "📊 Resumen de descarga:")
                resumenDescarga.forEach { Log.d("LugarViewModel", it) }

            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error general en descarga ampliada", e)
                Toast.makeText(context, "Error descargando lugares", Toast.LENGTH_SHORT).show()
            } finally {
                _cargando.value = false
            }
        }
    }




    // DESCARGAS  CATEGORIAS PERNSONALIZADAS -- REFACTORIZAR

    // 🔥 Añadir antes del final de LugarViewModel
    fun descargarCategoriasPersonalizadas(
        context: Context,
        apiKey: String,
        cantidadPorLado: Int = 0,
        radioMetros: Float = 3000f
    ) {
        viewModelScope.launch {
            val ubicacionActual = _ubicacion.value

            if (ubicacionActual == null) {
                Log.w("LugarViewModel", "❗ Ubicación no disponible para descarga personalizada.")
                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                return@launch
            }

            _cargando.value = true

            val puntos = generarPuntosAlrededor(
                centroLat = ubicacionActual.first,
                centroLng = ubicacionActual.second,
                cantidadPorLado = cantidadPorLado
            ).toMutableList()

            val resumenDescarga = mutableListOf<String>()
            val todosDescargados = mutableListOf<LugarLocal>()
            val todosFiltrados = mutableListOf<LugarLocal>()
            val todosGuardados = mutableListOf<LugarLocal>()

            Log.d("LugarViewModel", "🧭 Generando cuadrantes para categorías personalizadas:")
            puntos.forEachIndexed { index, (lat, lng) ->
                Log.d("LugarViewModel", "🗺 Punto $index → Lat: $lat, Lng: $lng")
            }

            try {
                for (subtipo in categoriasPersonalizadas) {
                    for ((lat, lng) in puntos) {
                        try {
                            val lugares = placesService.obtenerLugaresCercanos(
                                lat = lat,
                                lng = lng,
                                apiKey = apiKey,
                                type = subtipo,
                                maxDistancia = radioMetros
                            )

                            Log.d(
                                "📦LugarViewModel",
                                "🔍 Subtipo personalizado: $subtipo → Resultado: ${lugares.size} lugares"
                            )

                            val mapeados = lugares.map { lugar ->
                                lugar.copy(
                                    subcategoria = subtipo,
                                    categoriaGeneral = subtipo, // Asegurar consistencia
                                    photoReference = lugar.photoReference,
                                    businessStatus = lugar.businessStatus,
                                    userRatingsTotal = lugar.userRatingsTotal,
                                    abiertoAhora = lugar.abiertoAhora
                                )
                            }

                            todosDescargados.addAll(mapeados)

                            val filtrados = mapeados.filter {
                                it.photoReference != null &&
                                        (it.rating ?: 0f) >= 3.0f &&
                                        (it.userRatingsTotal ?: 0) >= 5
                            }

                            todosFiltrados.addAll(filtrados)

                            val unicos = filtrados.distinctBy { "${it.nombre}-${it.latitud}-${it.longitud}" }
                            repository.insertarLugares(unicos)

                            todosGuardados.addAll(unicos)

                            resumenDescarga.add("• Subtipo: $subtipo - ${unicos.size} lugares insertados")

                        } catch (e: Exception) {
                            Log.e("LugarViewModel", "❌ Error en subtipo $subtipo", e)
                        }
                    }
                }

                /*guardarCsv(context, "lugares_personalizados_descargados.csv", todosDescargados)
                guardarCsv(context, "lugares_personalizados_filtrados.csv", todosFiltrados)
                guardarCsv(context, "lugares_personalizados_guardados.csv", todosGuardados)*/

                Toast.makeText(context, "✅ Lugares personalizados cargados", Toast.LENGTH_SHORT).show()

                Log.d("LugarViewModel", "📊 Resumen de descarga personalizada:")
                resumenDescarga.forEach { Log.d("LugarViewModel", it) }

            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error general en descarga personalizada", e)
                Toast.makeText(context, "Error descargando lugares personalizados", Toast.LENGTH_SHORT).show()
            } finally {
                _cargando.value = false
            }
        }
    }



    // 1 Verifica y descarga automáticamente si es la primera vez
    fun verificarYDescargarCategoriasBaseSiNecesario(context: Context, apiKey: String) {
        viewModelScope.launch {
            val yaDescargado = fueDescargaBaseCompletada(context)

            if (!yaDescargado) {
                Log.d(
                    "LugarViewModel",
                    "📭 Aún no se ha hecho descarga base por sectores. Iniciando..."
                )
                descargarCategoriasBaseAmplias(context, apiKey)
            } else {
                Log.d("LugarViewModel", "📦 Descarga base ya realizada. No se repite.")
            }
        }
    }


    fun fueBusquedaInicialHecha(context: Context): Boolean {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("busqueda_inicial_hecha", false)
    }

    fun marcarBusquedaInicial(context: Context) {
        val prefs = context.getSharedPreferences("aventura_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("busqueda_inicial_hecha", true).apply()
    }





    fun descargarLugaresEspecificosPorNombre(context: Context, apiKey: String) {
        viewModelScope.launch {
            val lugaresObjetivo = listOf(
                // Río / Puentes
                "Paseo Marqués de Contadero",
                "Paseo en barco por el Guadalquivir",
                "Puente de Triana",
                "Puente del Alamillo",
                "Muelle de Nueva York",
                "Muelle de la Sal",
                // Mercados / Experiencias
                "Mercado de Triana",
                "Mercado de Feria",
                "Mercado de la Encarnación",
                "El Rinconcillo"
            )

            val categoriaPorNombre = mapOf(
                "Catedral de Sevilla" to "Cultura",
                "Real Alcázar" to "Cultura",
                "Parque de María Luisa" to "Aire libre",
                "Museo de Bellas Artes" to "Cultura",
                "Plaza de España" to "Cultura",
                "Torre del Oro" to "Cultura",
                "Mercado de Triana" to "Comida",
                "Metropol Parasol" to "Cultura",
                "El Rinconcillo" to "Comida",
                // Puedes seguir mapeando más si quieres
            )

            val todosEncontrados = mutableListOf<LugarLocal>()
            val todosGuardados = mutableListOf<LugarLocal>()

            for (nombre in lugaresObjetivo) {
                try {
                    val resultados = placesService.buscarLugaresPorTexto(nombre, apiKey)
                    if (resultados.isEmpty()) continue

                    val lugarLocal = resultados.first()  // Ya viene mapeado

                    val yaExiste = repository.existeLugarConId(lugarLocal.id)
                    if (!yaExiste) {
                        repository.insertarLugares(
                            listOf(
                                lugarLocal.copy(
                                    categoriaGeneral = categoriaPorNombre[nombre] ?: "Cultura",
                                    subcategoria = categoriaPorNombre[nombre] ?: "Cultura"
                                )
                            )
                        )
                        todosGuardados.add(lugarLocal)
                        Log.d("TextSearch", "✅ Insertado: ${lugarLocal.nombre}")
                    } else {
                        Log.d("TextSearch", "ℹ️ Ya existe: ${lugarLocal.nombre}")
                    }

                    todosEncontrados.add(lugarLocal)

                } catch (e: Exception) {
                    Log.e("TextSearch", "❌ Error buscando: $nombre", e)
                }
            }

            guardarCsv(context, "lugares_curados.csv", todosEncontrados)
            Toast.makeText(
                context,
                "🏛️ Lugares curados descargados: ${todosGuardados.size}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


// ***********  FUNCIONALIDADES CRUD  *********** //

    private val _estadoGuardado = MutableLiveData<Boolean>()
    val estadoGuardado: LiveData<Boolean> get() = _estadoGuardado

    fun agregarLugar(lugar: LugarLocal) {
        viewModelScope.launch {
            try {
                repository.insertarLugar(lugar)
                _estadoGuardado.value = true
                _ultimoLugarAgregadoId.value = lugar.id

                _ultimoLugarAgregadoId.value = lugar.id
                _ultimaCategoriaAgregada.value = lugar.categoriaGeneral
                _ultimaSubcategoriaAgregada.value = lugar.subcategoria


            } catch (e: Exception) {
                _estadoGuardado.value = false
            }
        }
    }

    fun recargarLugares() {
        viewModelScope.launch {
            _cargando.value = true
            val nuevosLugares = repository.obtenerTodos().first()
            _lugares.value = nuevosLugares
            _cargando.value = false
        }
    }


    // justo después de _ultimoLugarAgregadoId
    fun resetUltimoLugarAgregado() {
        _ultimoLugarAgregadoId.value = null
    }



    fun insertarLugares(lugares: List<LugarLocal>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertarLugares(lugares)
            Log.d("INSERTADOS NUEVOS", "✅ Lugares insertados: ${lugares.size}")
        }
    }

    fun actualizarLugarManual(lugar: LugarLocal) {
        viewModelScope.launch {
            repository.actualizarLugar(lugar)
            Log.d("ACTUALIZA NUEVOS", "✅ Lugares EDITADOS: ${lugar.nombre} ${lugar.id}")
        }
    }

    fun eliminarLugar(id: String) {
        viewModelScope.launch {
            repository.eliminarLugarPorId(id)

            Log.d("eliminado", "Lugar eliminado: $id")
        }
    }


    //**************** FUNCIONALIDADES BACKEND *****************//

    // subida
    fun sincronizarLugaresConApi() {
        viewModelScope.launch {
            val todos = dao.obtenerTodos().first()
            val usuarioId = obtenerUsuarioId()

            Log.d("SYNC_ANDROID", "📤 Lugares a sincronizar: ${todos.size}")
            todos.forEach {
                Log.d("SYNC_ANDROID", "📍 ${it.nombre} - ${it.latitud}, ${it.longitud}")
            }

            if (usuarioId != null) {
                repository.sincronizarConBackend(todos, usuarioId)
            }
        }
    }

    // descarga
    fun descargarLugaresDesdeBackend(context: Context) {
        viewModelScope.launch {
            try {
                val usuario = usuarioViewModel.obtenerUsuario()
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("jwt_token", null)

                if (usuario != null && token != null) {
                    RetrofitInstance.setTokenProvider { token }

                    val lugaresDesdeApi = RetrofitInstance.lugarApi.obtenerLugaresDelUsuario(usuario.id.toString())

                    // Guardar en la base de datos local
                    val lugaresLocal = lugaresDesdeApi.map { dto -> dto.toEntity() }
                    dao.insertarLugares(lugaresLocal)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Lugares descargados correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "⚠️ Usuario no autenticado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Error al descargar: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.d("LugarViewModel", "❌ Error al descargar lugares desde backend \"${e.message}\"")
                }
            }
        }
    }






    //FUNCIONES RUTAS ---

    // Agrega un lugar a la lista de selección
    fun agregarLugarARuta(lugar: LugarLocal) {
        if (!_lugaresSeleccionadosParaRuta.contains(lugar)) {
            _lugaresSeleccionadosParaRuta.add(lugar)
        }
    }

    // Elimina un lugar de la lista de selección
    fun eliminarLugarDeRuta(lugar: LugarLocal) {
        _lugaresSeleccionadosParaRuta.remove(lugar)
    }

    // Limpia la selección completa
    fun limpiarRuta() {
        _lugaresSeleccionadosParaRuta.clear()
    }

// descargas personalizadas ********************************************** esta es la funcion que estamos usando

    fun descargarLugaresPorSubcategoriasPersonalizadas(
        context: Context,
        subcategorias: Set<String>,
        apiKey: String,
        cantidadPorLado: Int = 0,
        radioMetros: Float = 3000f
    ) {
        viewModelScope.launch {
            val ubicacionActual = _ubicacion.value
            Log.d("LugarViewModel", "📍 Centro de descarga: ${ubicacionActual?.first}, ${ubicacionActual?.second}")

            if (ubicacionActual == null) {
                Log.w("LugarViewModel", "❗ Ubicación no disponible para descarga personalizada.")
                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                return@launch
            }

            _cargando.value = true

            val puntos = generarPuntosAlrededor(
                centroLat = ubicacionActual.first,
                centroLng = ubicacionActual.second,
                cantidadPorLado = cantidadPorLado
            ).toMutableList()

            val todosDescargados = mutableListOf<LugarLocal>()
            val todosFiltrados = mutableListOf<LugarLocal>()
            val todosGuardados = mutableListOf<LugarLocal>()

            Log.d("LugarViewModel", "🧭 Iniciando descarga personalizada con ${subcategorias.size} subcategorías")

            try {
                for ((lat, lng) in puntos) {
                    for (subtipo in subcategorias) {
                        try {
                            val lugares = placesService.obtenerLugaresCercanos(
                                lat = lat,
                                lng = lng,
                                apiKey = apiKey,
                                type = subtipo,
                                maxDistancia = radioMetros
                            )

                            Log.d("LugarViewModel", "🔍 Subtipo: $subtipo → Resultado: ${lugares.size} lugares")

                            val mapeados = lugares.map { lugar ->
                                lugar.copy(
                                    subcategoria = subtipo,
                                    photoReference = lugar.photoReference,
                                    businessStatus = lugar.businessStatus,
                                    userRatingsTotal = lugar.userRatingsTotal,
                                    abiertoAhora = lugar.abiertoAhora
                                )
                            }

                            todosDescargados.addAll(mapeados)

                            val filtrados = mapeados.filter {
                                it.photoReference != null &&
                                        (it.rating ?: 0f) >= 3.0f &&
                                        (it.userRatingsTotal ?: 0) >= 5
                            }

                            todosFiltrados.addAll(filtrados)

                            val unicos = filtrados.distinctBy { "${it.nombre}-${it.latitud}-${it.longitud}" }
                            repository.insertarLugares(unicos)
                            todosGuardados.addAll(unicos)

                            Log.d("LugarViewModel", "✅ Insertados ${unicos.size} lugares para subtipo $subtipo")

                        } catch (e: Exception) {
                            Log.e("LugarViewModel", "❌ Error en subtipo $subtipo", e)
                        }
                    }
                }

                Toast.makeText(context, "✅ Lugares descargados correctamente", Toast.LENGTH_SHORT).show()

                _eventoDescargaPersonalizada.value = true


            } catch (e: Exception) {
                Log.e("LugarViewModel", "❌ Error general en descarga personalizada", e)
                Toast.makeText(context, "Error en descarga personalizada", Toast.LENGTH_SHORT).show()
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetearEventoDescargaPersonalizada() {
        _eventoDescargaPersonalizada.value = false
    }

    fun actualizarUbicacionManual(latLng: LatLng) {
        _ubicacion.value = latLng.latitude to latLng.longitude
    }

    // funcion de agrupacion para pantalla de gestion de Lugares

    private val _lugaresPorZona = MutableStateFlow<Map<String, List<LugarLocal>>>(emptyMap())
    val lugaresPorZona: StateFlow<Map<String, List<LugarLocal>>> = _lugaresPorZona

    fun agruparLugaresPorZona(cantidadPorLado: Int = 1, separacionGrados: Double = 0.02) {
        val ubicacionCentral = _ubicacion.value
        val todos = _todosLosLugares.value

        if (ubicacionCentral == null || todos.isEmpty()) {
            Log.w("ZONAS", "⛔ Sin ubicación o sin lugares")
            return
        }

        val zonas = mutableMapOf<String, MutableList<LugarLocal>>()
        val centroLat = ubicacionCentral.first
        val centroLng = ubicacionCentral.second

        // Genera un mapa de zonas con coordenadas de cuadrícula
        val zonasOrdenadas = mutableListOf<Pair<Int, Int>>()
        for (i in -cantidadPorLado..cantidadPorLado) {
            for (j in -cantidadPorLado..cantidadPorLado) {
                zonasOrdenadas.add(i to j)
            }
        }

        val indexPorZona = zonasOrdenadas.mapIndexed { index, par -> par to "Zona ${index + 1}" }.toMap()

        // Agrupa los lugares en la zona más cercana
        for (lugar in todos) {
            val latOffset = ((lugar.latitud - centroLat) / separacionGrados).toInt().coerceIn(-cantidadPorLado, cantidadPorLado)
            val lngOffset = ((lugar.longitud - centroLng) / separacionGrados).toInt().coerceIn(-cantidadPorLado, cantidadPorLado)
            val clave = indexPorZona[latOffset to lngOffset] ?: continue

            zonas.getOrPut(clave) { mutableListOf() }.add(lugar)
        }

        _lugaresPorZona.value = zonas
        Log.d("ZONAS", "✅ Zonas agrupadas: ${zonas.size}")
    }






}