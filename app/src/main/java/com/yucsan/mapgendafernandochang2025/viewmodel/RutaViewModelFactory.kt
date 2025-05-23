package com.yucsan.aventurafernandochang2025.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yucsan.mapgendafernandochang2025.repository.RutaRepository
import com.yucsan.mapgendafernandochang2025.viewmodel.RutaViewModel

class RutaViewModelFactory(private val repository: RutaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RutaViewModel::class.java)) {
            return RutaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
