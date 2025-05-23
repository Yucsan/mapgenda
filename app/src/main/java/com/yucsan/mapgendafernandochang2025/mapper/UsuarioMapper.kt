package com.yucsan.mapgendafernandochang2025.mapper

import com.yucsan.mapgendafernandochang2025.dto.UsuarioDTO
import com.yucsan.mapgendafernandochang2025.entidad.UsuarioEntity

fun UsuarioDTO.toEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = id.toString(),
        nombre = nombre,
        apellido = apellido,
        email = email,
        telefono = telefono,
        rol = rol,
        pais = pais,
        ciudad = ciudad,
        direccion = direccion,
        descripcion = descripcion,
        verificado = verificado
    )
}
