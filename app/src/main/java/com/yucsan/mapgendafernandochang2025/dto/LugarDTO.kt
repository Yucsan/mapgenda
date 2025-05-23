package com.yucsan.mapgendafernandochang2025.dto



data class LugarDTO(
    val id: String,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val direccion: String,
    val tipo: String,
    val calificacion: Double,
    val fotoUrl: String,
    val abiertoAhora: Boolean,
    val duracionEstimadaMinutos: Int
)
