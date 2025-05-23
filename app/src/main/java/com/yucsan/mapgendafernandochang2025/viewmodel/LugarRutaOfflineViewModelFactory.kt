package com.yucsan.mapgendafernandochang2025.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LugarRutaOfflineViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LugarRutaOfflineViewModel::class.java)) {
            return LugarRutaOfflineViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
