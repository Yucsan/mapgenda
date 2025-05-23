package com.yucsan.mapgendafernandochang2025.dao

import androidx.room.*
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity

import java.util.UUID

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario LIMIT 1")
    suspend fun obtenerUsuario(): UsuarioEntity?

    @Query("DELETE FROM usuario")
    suspend fun eliminarUsuario()
}