package com.yucsan.mapgendafernandochang2025.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal

import com.yucsan.mapgendafernandochang2025.repository.RutaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugares
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugaresOrdenados
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaEntity

class RutaViewModel(private val repository: RutaRepository) : ViewModel() {

    private val _rutas = MutableStateFlow<List<RutaConLugares>>(emptyList())
    val rutas: StateFlow<List<RutaConLugares>> = _rutas.asStateFlow()

    init {
        observarRutas()
    }

    private fun observarRutas() {
        viewModelScope.launch {
            repository.obtenerRutasConLugares().collect { lista ->
                _rutas.value = lista
            }
        }
    }

    fun crearRuta(nombre: String, categoria: String?, ubicacionId: Long?, lugares: List<LugarLocal>, polylineCodificada: String? = null) {
        viewModelScope.launch {
            repository.crearRutaConLugares(
                nombre = nombre,
                categoria = categoria,
                ubicacionId = ubicacionId,
                lugares = lugares,
                polylineCodificada = polylineCodificada
            )
        }
        Log.d("RutaViewModel", "Ruta creada: $nombre")
    }

    fun eliminarRuta(rutaId: Long) {
        viewModelScope.launch {
            repository.eliminarRuta(rutaId)
        }
    }

    fun actualizarRuta(ruta: RutaEntity) {
        viewModelScope.launch {
            repository.actualizarRuta(ruta)
        }
    }

    fun agregarLugares(rutaId: Long, lugares: List<LugarLocal>) {
        viewModelScope.launch {
            repository.agregarLugaresARuta(rutaId, lugares)
        }
    }

    fun eliminarLugar(rutaId: Long, lugarId: String) {
        viewModelScope.launch {
            repository.eliminarLugarDeRuta(rutaId, lugarId)
        }
    }

    fun actualizarOrdenLugares(rutaId: Long, lugares: List<LugarLocal>) {
        Log.d("RutaViewModel", "\uD83D\uDCE6 Actualizando orden para rutaId=$rutaId con ${lugares.size} lugares")

        lugares.forEachIndexed { index, lugar ->
            Log.d("RutaViewModel", "\uD83D\uDD2D Pos $index: ${lugar.nombre} (${lugar.id})")
        }

        viewModelScope.launch {
            repository.actualizarOrdenLugares(rutaId, lugares)
        }
    }

    fun recargarRutaSeleccionada(rutaId: Long, onResultado: (RutaConLugaresOrdenados) -> Unit) {
        viewModelScope.launch {
            val resultado = repository.obtenerRutaConLugaresOrdenados(rutaId)
            onResultado(resultado)
        }
    }
}




























