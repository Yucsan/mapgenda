package com.yucsan.mapgendafernandochang2025.viewmodel


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition


class MapViewModel : ViewModel() {
    var camaraGuardada by mutableStateOf<CameraPosition?>(null)
        private set

    fun guardarCamara(position: CameraPosition) {
        camaraGuardada = position
        Log.d("MapViewModel", "guardarCamara: $position")
    }

    fun limpiarCamara() {
        camaraGuardada = null
    }
}
