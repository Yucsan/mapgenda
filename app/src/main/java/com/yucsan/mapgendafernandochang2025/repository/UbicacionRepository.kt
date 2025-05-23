package com.yucsan.mapgendafernandochang2025.repository



import com.yucsan.mapgendafernandochang2025.dao.UbicacionDao
import com.yucsan.mapgendafernandochang2025.entidad.UbicacionLocal
import kotlinx.coroutines.flow.Flow

class UbicacionRepository(private val dao: UbicacionDao) {

    suspend fun insertarUbicacion(ubicacion: UbicacionLocal) {
        dao.insertarUbicacion(ubicacion)
    }

    suspend fun eliminarUbicacion(ubicacion: UbicacionLocal) {
        dao.eliminarUbicacion(ubicacion)
    }

    suspend fun eliminarPorId(id: Int) {
        dao.eliminarPorId(id)
    }

    suspend fun actualizarTipo(id: Int, nuevoTipo: String) {
        dao.actualizarTipo(id, nuevoTipo)
    }

    fun obtenerTodas(): Flow<List<UbicacionLocal>> {
        return dao.obtenerTodas()
    }

    fun buscar(query: String): Flow<List<UbicacionLocal>> {
        return dao.buscarPorNombreOTipo(query)
    }

    suspend fun actualizarUbicacionCompleta(id: Int, nuevoNombre: String, nuevoTipo: String) {
        dao.actualizarUbicacionCompleta(id, nuevoNombre, nuevoTipo)
    }

    suspend fun insertarUbicacionYRetornarId(ubicacion: UbicacionLocal): Long {
        return dao.insertarUbicacionYRetornarId(ubicacion)
    }


}
