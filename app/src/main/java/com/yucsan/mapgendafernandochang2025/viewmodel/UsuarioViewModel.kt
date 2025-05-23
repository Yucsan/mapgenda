package com.yucsan.mapgendafernandochang2025.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.repository.UsuarioRepository
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(private val repository: UsuarioRepository) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario

    @OptIn(UnstableApi::class)
    fun cargarUsuario() {
        viewModelScope.launch {
            _usuario.value = repository.obtenerUsuario()
            Log.d("CARGARUSU", "Usuario cargado desde Room: ${_usuario.value?.fotoPerfilUri}")
        }
    }

    suspend fun obtenerUsuario(): UsuarioEntity? {
        return repository.obtenerUsuario()
    }


    fun actualizarFoto(uri: String) {
        viewModelScope.launch {
            repository.actualizarFoto(uri)
            _usuario.value = repository.obtenerUsuario() // refresca en UI
        }
    }

    @OptIn(UnstableApi::class)
    fun guardarUsuario(usuario: UsuarioEntity) {
        viewModelScope.launch {
            repository.guardarUsuario(usuario)
            Log.d("GUARDAR", "Usuario guardado con URI: ${usuario.fotoPerfilUri}")
            _usuario.value = usuario
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            repository.cerrarSesion()
            _usuario.value = null
        }
    }
}