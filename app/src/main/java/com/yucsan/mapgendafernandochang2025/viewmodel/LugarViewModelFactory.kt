package com.yucsan.mapgendafernandochang2025.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LugarViewModelFactory(
    private val application: Application,
    private val authViewModel: AuthViewModel,
    private val usuarioViewModel: UsuarioViewModel
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LugarViewModel(application, authViewModel, usuarioViewModel) as T
    }
}
