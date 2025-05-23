package com.yucsan.mapgendafernandochang2025.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.yucsan.mapgendafernandochang2025.util.state.AuthState


class AuthViewModel(
    private val usuarioViewModel: UsuarioViewModel
) : ViewModel() {

    private val _authState = MutableStateFlow<com.yucsan.mapgendafernandochang2025.util.state.AuthState>(AuthState.Loading)
    val authState: StateFlow<com.yucsan.mapgendafernandochang2025.util.state.AuthState> = _authState.asStateFlow()

    init {
        // Cargar sesión en el arranque
        viewModelScope.launch {
            val usuario = usuarioViewModel.obtenerUsuario()

            if (usuario != null) {
                _authState.value = AuthState.Autenticado(usuario)
            } else {
                _authState.value = AuthState.NoAutenticado
            }
        }
    }

    fun iniciarSesion(usuario: UsuarioEntity) {
        viewModelScope.launch {
            usuarioViewModel.guardarUsuario(usuario)
            _authState.value = AuthState.Autenticado(usuario)
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            usuarioViewModel.cerrarSesion()
            _authState.value = AuthState.NoAutenticado
        }
    }
}
