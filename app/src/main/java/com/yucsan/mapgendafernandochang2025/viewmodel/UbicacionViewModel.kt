package com.yucsan.mapgendafernandochang2025.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal

import com.yucsan.mapgendafernandochang2025.repository.UbicacionRepository

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UbicacionViewModel(private val repository: UbicacionRepository) : ViewModel() {

    private val _ubicaciones = MutableStateFlow<List<UbicacionLocal>>(emptyList())
    val ubicaciones: StateFlow<List<UbicacionLocal>> = _ubicaciones.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        observarUbicaciones()
    }

    private fun observarUbicaciones() {
        viewModelScope.launch {
            query.flatMapLatest { texto ->
                if (texto.isBlank()) {
                    repository.obtenerTodas()
                } else {
                    repository.buscar(texto)
                }
            }.collect { lista ->
                _ubicaciones.value = lista
            }
        }
    }

    fun actualizarQuery(nuevoTexto: String) {
        _query.value = nuevoTexto
    }

    fun guardarUbicacion(nombre: String, lat: Double, lng: Double, tipo: String) {
        val nuevaUbicacion = UbicacionLocal(
            nombre = nombre,
            latitud = lat,
            longitud = lng,
            tipo = tipo
        )
        viewModelScope.launch {
            repository.insertarUbicacion(nuevaUbicacion)
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

    suspend fun guardarYRetornarId(ubicacion: UbicacionLocal): Long {
        return repository.insertarUbicacionYRetornarId(ubicacion)
    }



}
