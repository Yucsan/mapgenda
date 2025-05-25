package com.yucsan.mapgendafernandochang2025.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioViewModel: UsuarioViewModel
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 👇 ahora el contexto lo recibimos desde afuera
    fun initAuth(context: Context) {
        viewModelScope.launch {
            val usuario = usuarioViewModel.obtenerUsuario()
            val token = obtenerToken(context)

            if (usuario != null && token != null) {
                _authState.value = AuthState.Autenticado(usuario, token)
            } else {
                _authState.value = AuthState.NoAutenticado
            }
        }
    }

    fun iniciarSesion(context: Context, usuario: UsuarioEntity, token: String) {
        viewModelScope.launch {
            usuarioViewModel.guardarUsuario(usuario)
            guardarToken(context, token)
            _authState.value = AuthState.Autenticado(usuario, token)

            usuarioViewModel.sincronizarUsuarioConBackend(usuario.id) // Aseguramos que el usuario esté sincronizado
        }
    }

    fun cerrarSesion(context: Context) {
        viewModelScope.launch {
            usuarioViewModel.cerrarSesion()
            clearToken(context)
            _authState.value = AuthState.NoAutenticado
        }
    }

    private fun guardarToken(context: Context, token: String) {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("jwt_token", token)
            .apply()
    }

    private fun obtenerToken(context: Context): String? {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("jwt_token", null)
    }

    private fun clearToken(context: Context) {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("jwt_token")
            .apply()
    }
}
