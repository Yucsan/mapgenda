package com.yucsan.mapgendafernandochang2025.repository


import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.yucsan.mapgendafernandochang2025.dao.RutaDao
import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugares
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaConLugaresOrdenados
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaEntity
import com.yucsan.mapmapgendafernandochang2025.entidad.RutaLugarCrossRef
import kotlinx.coroutines.flow.Flow

class RutaRepository(private val dao: RutaDao) {

    @OptIn(UnstableApi::class)
    suspend fun crearRutaConLugares(
        nombre: String,
        categoria: String?,
        ubicacionId: Long?,
        lugares: List<LugarLocal>,
        polylineCodificada: String? = null
    ) {
        Log.d("RutaRepository", "🗂️ Guardando ruta y lugares asociados")
        try {

        val rutaId = dao.insertarRuta(
            RutaEntity(
                nombre = nombre,
                categoria = categoria,
                ubicacionId = ubicacionId,
                polylineCodificada = polylineCodificada,
                fechaDeCreacion = System.currentTimeMillis()
            )
        )

        val refs = lugares.mapIndexed { index, it ->
            RutaLugarCrossRef(
                rutaId = rutaId,
                lugarId = it.id,
                orden = index
            )
        }
        dao.insertarReferencias(refs)

        } catch (e: Exception) {
            Log.e("RutaRepository", "❌ Error guardando ruta: ${e.message}", e)
            throw e
        }
    }

    fun obtenerRutasConLugares(): Flow<List<RutaConLugares>> {
        return dao.obtenerRutasConLugares()
    }

    suspend fun eliminarRuta(rutaId: Long) {
        dao.eliminarRutaConRelaciones(rutaId)
    }

    suspend fun actualizarRuta(ruta: RutaEntity) {
        dao.actualizarRuta(ruta)
    }

    suspend fun actualizarOrdenLugares(rutaId: Long, lugaresOrdenados: List<LugarLocal>) {
        // Borrar referencias anteriores
        dao.eliminarReferenciasPorRuta(rutaId)

        // Insertar nuevas referencias con orden actualizado
        val nuevasReferencias = lugaresOrdenados.mapIndexed { index, lugar ->
            RutaLugarCrossRef(rutaId = rutaId, lugarId = lugar.id, orden = index)
        }
        dao.insertarReferencias(nuevasReferencias)
    }

    suspend fun obtenerRutaConLugaresOrdenados(rutaId: Long): RutaConLugaresOrdenados {
        val ruta = dao.obtenerRutaPorId(rutaId)
        val lugaresConOrden = dao.obtenerLugaresOrdenadosPorRuta(rutaId)
        return RutaConLugaresOrdenados(
            ruta = ruta,
            lugares = lugaresConOrden.map { it.lugar }
        )
    }

    suspend fun agregarLugaresARuta(rutaId: Long, lugares: List<LugarLocal>) {
        val refs = lugares.mapIndexed { index, it ->
            RutaLugarCrossRef(
                rutaId = rutaId,
                lugarId = it.id,
                orden = index
            )
        }
        dao.agregarLugaresARuta(refs)
    }

    suspend fun eliminarLugarDeRuta(rutaId: Long, lugarId: String) {
        dao.eliminarLugarDeRuta(rutaId, lugarId)
    }

}
