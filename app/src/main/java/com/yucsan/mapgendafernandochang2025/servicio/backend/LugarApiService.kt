package com.yucsan.mapgendafernandochang2025.servicio.backend


import com.yucsan.mapgendafernandochang2025.dto.LugarDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LugarApiService {
    @POST("lugares")
    suspend fun crearLugar(@Body dto: LugarDTO): Response<LugarDTO>

    @POST("lugares/batch")
    suspend fun subirLugares(@Body lugares: List<LugarDTO>): Response<Unit>
}
