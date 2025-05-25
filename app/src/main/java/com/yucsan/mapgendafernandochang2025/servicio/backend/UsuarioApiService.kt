package com.yucsan.mapgendafernandochang2025.servicio.backend


import com.yucsan.mapgendafernandochang2025.dto.LoginResponse
import com.yucsan.mapgendafernandochang2025.dto.UsuarioDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioApiService {
    @PUT("usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: String,
        @Body usuarioDTO: UsuarioDTO
    ): Response<UsuarioDTO>


    @POST("usuarios/login-google")
    suspend fun loginConGoogle(@Body body: Map<String, String>): Response<LoginResponse>

    @GET("usuarios/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: String): Response<UsuarioDTO>


}
