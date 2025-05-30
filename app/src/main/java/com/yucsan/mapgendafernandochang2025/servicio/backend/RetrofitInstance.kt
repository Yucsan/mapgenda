package com.yucsan.mapgendafernandochang2025.servicio.backend

import com.yucsan.mapgendafernandochang2025.util.Auth.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    //private const val BASE_URL = "http://192.168.0.11:8080/aventura/" // LOCAL
    private const val BASE_URL = "https://backend-mapgenda.onrender.com/" // RENDER

    private var tokenProvider: () -> String? = { null } // inyectado desde fuera

    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { tokenProvider() }) // 👈 usa el token dinámico
        .build()

    val api: UsuarioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApiService::class.java)
    }

    val lugarApi: LugarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // 👈 incluye interceptor con token
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LugarApiService::class.java)
    }


}