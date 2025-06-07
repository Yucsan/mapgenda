package com.yucsan.mapgendafernandochang2025.repository


import com.yucsan.mapgendafernandochang2025.dao.UsuarioDao
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun guardarUsuario(usuario: UsuarioEntity) {
        withContext(Dispatchers.IO) {
            usuarioDao.insertarUsuario(usuario)
        }
    }

    suspend fun actualizarFoto(uri: String) {
        withContext(Dispatchers.IO) {
            val usuarioActual = usuarioDao.obtenerUsuario()
            if (usuarioActual != null) {
                val actualizado = usuarioActual.copy(fotoPerfilUri = uri)
                usuarioDao.insertarUsuario(actualizado)
            }
        }
    }

    suspend fun obtenerUsuario(): UsuarioEntity? {
        return withContext(Dispatchers.IO) {
            usuarioDao.obtenerUsuario()
        }
    }

    suspend fun obtenerUsuarioSincronizado(): UsuarioEntity? {
        return withContext(Dispatchers.IO) {
            usuarioDao.obtenerUsuarioSincronizado()
        }
    }


    suspend fun cerrarSesion() {
        withContext(Dispatchers.IO) {
            usuarioDao.eliminarUsuario()
        }
    }
}