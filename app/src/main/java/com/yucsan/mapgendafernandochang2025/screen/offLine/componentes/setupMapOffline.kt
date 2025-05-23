package com.yucsan.mapgendafernandochang2025.screen.offLine.componentes

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.util.MapPainter

fun setupMapOffline(
    context: Context,
    map: GoogleMap,
    lugares: List<LugarLocal>,
    radio: Float,
    ubicacionCentro: LatLng? = null,
    ubicaciones: List<UbicacionLocal> = emptyList(),
    lugaresSeleccionados: List<LugarLocal> = emptyList(),
    centrarCamara: Boolean = true
) {
    try {
        map.clear()
        map.isMyLocationEnabled = true

        // ⬇️ Centrar cámara si corresponde
        if (ubicacionCentro != null && centrarCamara) {
            val zoomLevel = when {
                radio <= 300f -> 17f
                radio <= 800f -> 16f
                radio <= 1500f -> 15f
                radio <= 3000f -> 14f
                else -> 13f
            }

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCentro, zoomLevel))
            map.addMarker(com.google.android.gms.maps.model.MarkerOptions().position(ubicacionCentro).title("Ubicación seleccionada"))
            Log.d("MAPA_OFFLINE", "📍 Mapa centrado en ubicación manual: $ubicacionCentro")
        } else {
            Log.w("MAPA_OFFLINE", "⚠️ No se proporcionó ubicaciónCentro; el mapa no se centrará.")
        }

        // ⬇️ Pintar ubicaciones
        ubicaciones.forEach { ubi ->
            MapPainter.pintarUbicacion(context, map, ubi)
        }

        // ⬇️ Pintar lugares con íconos o números si están en la ruta
        lugares.forEach { lugar ->
            val index = lugaresSeleccionados.indexOfFirst {
                it.latitud == lugar.latitud && it.longitud == lugar.longitud
            }.takeIf { it != -1 }

            MapPainter.pintarLugar(context, map, lugar, index)
        }

        // ⬇️ Dibujar polilínea si hay al menos dos
        MapPainter.pintarPolylineRuta(map, lugaresSeleccionados)

        // ⬇️ Ajustar cámara a los lugares visibles si se indica
        if (centrarCamara && lugares.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            lugares.forEach { builder.include(LatLng(it.latitud, it.longitud)) }

            val bounds = builder.build()
            val padding = 100
            map.setOnMapLoadedCallback {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            }
        }

    } catch (e: SecurityException) {
        Toast.makeText(context, "Sin permisos de ubicación", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("MAPA", "Error al obtener ubicación o pintar mapa", e)
    }
}
