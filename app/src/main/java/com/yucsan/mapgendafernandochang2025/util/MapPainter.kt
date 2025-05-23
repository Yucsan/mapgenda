package com.yucsan.mapgendafernandochang2025.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import com.yucsan.mapgendafernandochang2025.R

object MapPainter {

    private fun getIconoPersonalizado(context: Context, drawableId: Int, size: Int = 120): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, false)
        return BitmapDescriptorFactory.fromBitmap(scaled)
    }

    fun pintarLugar(
        context: Context,
        map: GoogleMap,
        lugar: LugarLocal,
        index: Int? = null
    ): Marker {
        val icono = if (index != null) {
            IconosMapa.generarIconoNumerado(context, index + 1)
        } else {
            when (lugar.categoriaGeneral) {
                "restaurant" -> getIconoPersonalizado(context, R.drawable.restaurant)
                "cafe" -> getIconoPersonalizado(context, R.drawable.cafe)
                "clothing_store" -> getIconoPersonalizado(context, R.drawable.shop)
                "park" -> getIconoPersonalizado(context, R.drawable.tree)
                "tourist_attraction" -> getIconoPersonalizado(context, R.drawable.turistatraction)
                "museum" -> getIconoPersonalizado(context, R.drawable.museum)
                "art_gallery" -> getIconoPersonalizado(context, R.drawable.art_galery)
                "aquarium" -> getIconoPersonalizado(context, R.drawable.acuario)
                "stadium" -> getIconoPersonalizado(context, R.drawable.estadio)
                "night_club" -> getIconoPersonalizado(context, R.drawable.night_club)
                "movie_theater" -> getIconoPersonalizado(context, R.drawable.cine)
                "casino" -> getIconoPersonalizado(context, R.drawable.casino)
                "lodging" -> getIconoPersonalizado(context, R.drawable.hotel)
                "bus_station", "train_station", "subway_station" -> getIconoPersonalizado(context, R.drawable.bus)
                "custom" -> getIconoPersonalizado(context, R.drawable.custom)
                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
            }
        }

        return map.addMarker(
            MarkerOptions()
                .position(LatLng(lugar.latitud, lugar.longitud))
                .title(lugar.nombre)
                .snippet(lugar.direccion)
                .icon(icono)
        )!!
    }

    fun pintarUbicacion(
        context: Context,
        map: GoogleMap,
        ubicacion: UbicacionLocal
    ): Marker {
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(ubicacion.latitud, ubicacion.longitud))
                .title("📌 ${ubicacion.nombre}")
                .snippet("Tipo: ${ubicacion.tipo}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
        marker?.tag = ubicacion
        return marker!!
    }

    fun pintarPolylineRuta(
        map: GoogleMap,
        lugares: List<LugarLocal>
    ) {
        if (lugares.size < 2) return

        val puntos = lugares.map { LatLng(it.latitud, it.longitud) }

        map.addPolyline(
            PolylineOptions()
                .addAll(puntos)
                .width(8f)
                .color(Color.BLUE)
                .geodesic(true)
        )
    }
}
