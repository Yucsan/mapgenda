package com.yucsan.mapgendafernandochang2025.dao

import androidx.room.*
import com.yucsan.mapgendafernandochang2025.entidad.ConteoSubcategoria

import com.yucsan.mapgendafernandochang2025.entidad.LugarLocal

import kotlinx.coroutines.flow.Flow

@Dao
interface LugarDao {

    @Query("SELECT * FROM lugares")
    fun obtenerTodos(): Flow<List<LugarLocal>>


    @Query("SELECT * FROM lugares")
    suspend fun obtenerTodosSuspend(): List<LugarLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLugar(lugar: LugarLocal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLugares(lugares: List<LugarLocal>)

    @Update
    suspend fun actualizarLugar(lugar: LugarLocal)

    @Query("DELETE FROM lugares WHERE id = :id")
    suspend fun eliminarPorId(id: String)


    @Query("SELECT * FROM lugares WHERE categoriaGeneral IN (:categorias)")
    fun obtenerPorCategorias(categorias: List<String>): Flow<List<LugarLocal>>

    @Query("""
    SELECT * FROM lugares 
    WHERE categoriaGeneral IN (:categorias)
    AND ((latitud - :latitud)*(latitud - :latitud) + (longitud - :longitud)*(longitud - :longitud)) < (:radioGrados * :radioGrados)
""")
    fun obtenerCercanos(
        categorias: List<String>,
        latitud: Double,
        longitud: Double,
        radioGrados: Double
    ): Flow<List<LugarLocal>>

    @Query("DELETE FROM lugares")
    suspend fun limpiarTodo()

    @Query("""
    SELECT * FROM lugares 
    WHERE categoriaGeneral = :categoria
    AND ((latitud - :latitud)*(latitud - :latitud) + (longitud - :longitud)*(longitud - :longitud)) < (:radioGrados * :radioGrados)
""")
    suspend fun obtenerCercanosSuspend(
        categoria: String,
        latitud: Double,
        longitud: Double,
        radioGrados: Double
    ): List<LugarLocal>

    @Query("""
    SELECT * FROM lugares
    WHERE subcategoria IN (:subtipos)
    AND ABS(latitud - :latitud) <= :radio
    AND ABS(longitud - :longitud) <= :radio
""")
    fun obtenerPorSubcategoriasCercanos(
        subtipos: List<String>,
        latitud: Double,
        longitud: Double,
        radio: Double
    ): Flow<List<LugarLocal>>

    @Query("""
    SELECT * FROM lugares
    WHERE subcategoria = :subtipo
    AND ABS(latitud - :latitud) <= :radio
    AND ABS(longitud - :longitud) <= :radio
""")
    suspend fun obtenerPorSubcategoriaCercanoSuspend(
        subtipo: String,
        latitud: Double,
        longitud: Double,
        radio: Double
    ): List<LugarLocal>

    @Query("SELECT subcategoria, COUNT(*) as cantidad FROM lugares GROUP BY subcategoria")
    suspend fun contarLugaresPorSubcategoria(): List<ConteoSubcategoria>

    @Query("SELECT EXISTS(SELECT 1 FROM lugares WHERE id = :id)")
    suspend fun existeLugar(id: String): Boolean

// nueva funcion para busqueda offline
@Query("SELECT * FROM lugares WHERE subcategoria IN (:subtipos)")
fun obtenerPorSubcategoriasSinFiltroGeografico(
    subtipos: List<String>
): Flow<List<LugarLocal>>




}
