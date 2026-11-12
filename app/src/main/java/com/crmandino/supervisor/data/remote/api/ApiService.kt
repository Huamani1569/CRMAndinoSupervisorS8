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
import com.crmandino.supervisor.data.remote.dto.*
import com.google.gson.JsonObject
import okhttp3.ResponseBody
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

    // ================= Proyecto Final (Semanas 12–14) =================

    // ---------- Analítica ----------
    @GET("api/analitica/resumen")
    suspend fun resumen(@Query("mes") mes: String? = null): Response<ResumenDto>

    @GET("api/analitica/embudo")
    suspend fun embudo(@Query("mes") mes: String? = null): Response<EmbudoDto>

    @GET("api/analitica/pronostico")
    suspend fun pronostico(@Query("mes") mes: String? = null): Response<PronosticoEquipoDto>

    @GET("api/analitica/ventas-mensuales")
    suspend fun ventasMensuales(@Query("meses") meses: Int = 8): Response<VentasMensualesDto>

    @GET("api/analitica/clientes-riesgo")
    suspend fun clientesRiesgo(@Query("minimo") minimo: Double = 0.5): Response<ClientesRiesgoDto>

    @GET("api/analitica/clientes-rentables")
    suspend fun clientesRentables(): Response<RentablesDto>

    @GET("api/analitica/servicio")
    suspend fun servicioAnalitica(): Response<ServicioAnaliticaDto>

    @GET("api/analitica/modelos")
    suspend fun modelos(): Response<ListaResponse<ModeloDto>>

    @POST("api/ml/entrenar")
    suspend fun entrenar(): Response<JsonObject>

    // ---------- Equipo ----------
    @GET("api/leads")
    suspend fun leadsEquipo(@Query("abiertos") abiertos: String = "1", @Query("orden") orden: String = "puntaje"): Response<ListaResponse<LeadEquipoDto>>

    @GET("api/usuarios/vendedores")
    suspend fun vendedoresCarga(): Response<ListaResponse<VendedorCargaDto>>

    @PUT("api/leads/reasignar")
    suspend fun reasignar(@Header("X-Operacion-UUID") op: String, @Body body: ReasignarRequest): Response<ReasignarResponse>

    // ---------- Aprobaciones ----------
    @GET("api/cotizaciones")
    suspend fun cotizacionesPorEstado(@Query("estado") estado: String): Response<ListaResponse<CotizacionDto>>

    @PUT("api/cotizaciones/{id}/aprobacion")
    suspend fun aprobar(@Header("X-Operacion-UUID") op: String, @Path("id") id: Int, @Body body: AprobacionRequest): Response<DatoResponse<CotizacionDto>>

    // ---------- Campañas ----------
    @GET("api/campanas")
    suspend fun campanas(): Response<ListaResponse<CampanaDto>>

    @POST("api/campanas/previsualizar")
    suspend fun previsualizar(@Body body: PrevisualizarRequest): Response<PrevisualizarResponse>

    @POST("api/campanas")
    suspend fun crearCampana(@Header("X-Operacion-UUID") op: String, @Body body: CampanaRequest): Response<DatoResponse<CampanaDto>>

    @POST("api/campanas/{id}/envio")
    suspend fun enviarCampana(@Header("X-Operacion-UUID") op: String, @Path("id") id: Int): Response<EnvioCampanaResponse>

    @GET("api/campanas/{id}/resultados")
    suspend fun resultadosCampana(@Path("id") id: Int): Response<ResultadosCampanaDto>

    @GET("api/productos")
    suspend fun productos(): Response<ListaResponse<ProductoDto>>

    // ---------- Retención ----------
    @POST("api/tareas")
    suspend fun crearRetencion(@Header("X-Operacion-UUID") op: String, @Body body: RetencionRequest): Response<DatoResponse<TareaCreadaDto>>

    // ---------- Servicio (agente y supervisor) ----------
    @GET("api/servicio/solicitudes")
    suspend fun tickets(@Query("cola") cola: String? = null, @Query("estado") estado: String? = null): Response<ListaResponse<TicketDto>>

    @GET("api/servicio/solicitudes/{id}")
    suspend fun ticket(@Path("id") id: Int): Response<DatoResponse<TicketDto>>

    @GET("api/servicio/solicitudes/{id}/foto")
    suspend fun fotoTicket(@Path("id") id: Int): Response<ResponseBody>

    @PUT("api/servicio/solicitudes/{id}/estado")
    suspend fun cambiarEstadoTicket(@Header("X-Operacion-UUID") op: String, @Path("id") id: Int, @Body body: EstadoTicketRequest): Response<DatoResponse<TicketDto>>

    // ---------- Notificaciones ----------
    @GET("api/notificaciones")
    suspend fun notificaciones(): Response<NotificacionesResponse>

    @PUT("api/notificaciones/leidas")
    suspend fun marcarLeidas(@Body body: MarcarLeidasRequest): Response<MarcadasResponse>
}
