package com.crmandino.supervisor.data.remote.api

import com.crmandino.supervisor.data.remote.dto.DatoResponse
import com.crmandino.supervisor.data.remote.dto.LeadDto
import com.crmandino.supervisor.data.remote.dto.LeadRequest
import com.crmandino.supervisor.data.remote.dto.ListaResponse
import com.crmandino.supervisor.data.remote.dto.LoginRequest
import com.crmandino.supervisor.data.remote.dto.LoginResponse
import com.crmandino.supervisor.data.remote.dto.SaludResponse
import com.crmandino.supervisor.data.remote.dto.TareaDto
import com.crmandino.supervisor.data.remote.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints de la API REST que usa la app SUPERVISOR (solo los permitidos para su rol):
// leads → consulta, edición/reasignación y eliminación · tareas → consulta · vendedores → consulta.
// Toda escritura envía la cabecera X-Operacion-UUID para que el servidor evite duplicados.
interface ApiService {

    @GET("api/salud")
    suspend fun salud(): Response<SaludResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // ---------- Leads ----------
    @GET("api/leads")
    suspend fun obtenerLeads(): Response<ListaResponse<LeadDto>>

    @PUT("api/leads/{id}")
    suspend fun actualizarLead(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Body body: LeadRequest
    ): Response<DatoResponse<LeadDto>>

    @DELETE("api/leads/{id}")
    suspend fun eliminarLead(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Query("version") version: Int
    ): Response<DatoResponse<LeadDto>>

    // ---------- Tareas (solo consulta) ----------
    @GET("api/tareas")
    suspend fun obtenerTareas(): Response<ListaResponse<TareaDto>>

    // ---------- Vendedores (para reasignar) ----------
    @GET("api/usuarios/vendedores")
    suspend fun obtenerVendedores(): Response<ListaResponse<UsuarioDto>>
}
