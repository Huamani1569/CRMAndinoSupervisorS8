package com.crmandino.supervisor.data.remote.dto

// Lead tal como lo devuelve la API
data class LeadDto(
    val id: Int,
    val uuid: String,
    val empresa: String,
    val contacto: String?,
    val valorPotencial: Double,
    val etapa: String,
    val vendedorId: Int,
    val vendedorNombre: String?,
    val version: Int,
    val eliminado: Boolean?
)

// Lead que se envía en POST / PUT (version = la que conocía el celular, para detectar conflictos)
data class LeadRequest(
    val uuid: String,
    val empresa: String,
    val contacto: String,
    val valorPotencial: Double,
    val etapa: String,
    val vendedorId: Int? = null, // el supervisor puede REASIGNAR el lead a otro vendedor
    val version: Int? = null
)
