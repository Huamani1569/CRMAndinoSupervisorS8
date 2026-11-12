package com.crmandino.supervisor.data.repository

import android.content.Context
import com.crmandino.supervisor.data.local.AppDatabase
import com.crmandino.supervisor.data.local.SesionLocal
import com.crmandino.supervisor.data.local.entities.CacheEntity
import com.crmandino.supervisor.data.remote.api.ApiService
import com.crmandino.supervisor.data.remote.dto.AprobacionRequest
import com.crmandino.supervisor.data.remote.dto.CampanaDto
import com.crmandino.supervisor.data.remote.dto.CampanaRequest
import com.crmandino.supervisor.data.remote.dto.ClientesRiesgoDto
import com.crmandino.supervisor.data.remote.dto.CotizacionDto
import com.crmandino.supervisor.data.remote.dto.EmbudoDto
import com.crmandino.supervisor.data.remote.dto.EstadoTicketRequest
import com.crmandino.supervisor.data.remote.dto.LeadEquipoDto
import com.crmandino.supervisor.data.remote.dto.MarcarLeidasRequest
import com.crmandino.supervisor.data.remote.dto.ModeloDto
import com.crmandino.supervisor.data.remote.dto.NotificacionDto
import com.crmandino.supervisor.data.remote.dto.PrevisualizarRequest
import com.crmandino.supervisor.data.remote.dto.ProductoDto
import com.crmandino.supervisor.data.remote.dto.PronosticoEquipoDto
import com.crmandino.supervisor.data.remote.dto.ReasignarRequest
import com.crmandino.supervisor.data.remote.dto.RentablesDto
import com.crmandino.supervisor.data.remote.dto.ResultadosCampanaDto
import com.crmandino.supervisor.data.remote.dto.ResumenDto
import com.crmandino.supervisor.data.remote.dto.RetencionRequest
import com.crmandino.supervisor.data.remote.dto.SegmentoCampana
import com.crmandino.supervisor.data.remote.dto.ServicioAnaliticaDto
import com.crmandino.supervisor.data.remote.dto.TicketDto
import com.crmandino.supervisor.data.remote.dto.VendedorCargaDto
import com.crmandino.supervisor.data.remote.dto.VentasMensualesDto
import com.crmandino.supervisor.util.Notificador
import com.crmandino.supervisor.util.Resultado
import com.crmandino.supervisor.util.llamarApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

/**
 * Semanas 12–14 en la app Supervisor: CRM analítico (KPIs, embudo, pronóstico, clientes en riesgo y rentables,
 * modelos ML), aprobaciones de descuento, reasignación en lote, campañas y la cola de servicio del agente.
 * Son datos del servidor: se leen EN LÍNEA y se guarda una copia en Room (caché) para verlos sin conexión.
 * Las acciones envían X-Operacion-UUID (si se repiten, el servidor no las duplica).
 */
class GestionRepository(
    private val context: Context,
    db: AppDatabase,
    private val api: ApiService,
    private val sesion: SesionLocal
) {
    private val gson = Gson()
    private val cache = db.cacheDao()

    private inline fun <reified T> observar(clave: String): Flow<T?> {
        val tipo = object : TypeToken<T>() {}.type
        return cache.observar(clave).map { fila -> fila?.let { runCatching { gson.fromJson<T>(it.json, tipo) }.getOrNull() } }
    }

    private suspend fun guardar(clave: String, dato: Any) = cache.guardar(CacheEntity(clave, gson.toJson(dato)))

    // Descarga y guarda en caché; devuelve el mensaje de error o null.
    private suspend fun <T : Any> traer(clave: String, llamada: suspend () -> retrofit2.Response<T>, extraer: (T) -> Any = { it }): String? =
        when (val r = llamarApi(llamada)) {
            is Resultado.Ok -> { guardar(clave, extraer(r.dato)); null }
            is Resultado.Error -> r.mensaje
        }

    // ---------- lecturas (caché) ----------
    val resumen: Flow<ResumenDto?> = observar<ResumenDto>("resumen")
    val embudo: Flow<EmbudoDto?> = observar<EmbudoDto>("embudo")
    val pronostico: Flow<PronosticoEquipoDto?> = observar<PronosticoEquipoDto>("pronostico")
    val ventasMensuales: Flow<VentasMensualesDto?> = observar<VentasMensualesDto>("ventas")
    val riesgo: Flow<ClientesRiesgoDto?> = observar<ClientesRiesgoDto>("riesgo")
    val rentables: Flow<RentablesDto?> = observar<RentablesDto>("rentables")
    val servicio: Flow<ServicioAnaliticaDto?> = observar<ServicioAnaliticaDto>("servicio")
    val modelos: Flow<List<ModeloDto>> = observar<List<ModeloDto>>("modelos").map { it.orEmpty() }
    val porAprobar: Flow<List<CotizacionDto>> = observar<List<CotizacionDto>>("por_aprobar").map { it.orEmpty() }
    val leadsEquipo: Flow<List<LeadEquipoDto>> = observar<List<LeadEquipoDto>>("leads_equipo").map { it.orEmpty() }
    val vendedores: Flow<List<VendedorCargaDto>> = observar<List<VendedorCargaDto>>("vendedores").map { it.orEmpty() }
    val campanas: Flow<List<CampanaDto>> = observar<List<CampanaDto>>("campanas").map { it.orEmpty() }
    val productos: Flow<List<ProductoDto>> = observar<List<ProductoDto>>("productos").map { it.orEmpty() }
    val tickets: Flow<List<TicketDto>> = observar<List<TicketDto>>("tickets").map { it.orEmpty() }
    val notificaciones: Flow<List<NotificacionDto>> = observar<List<NotificacionDto>>("notificaciones").map { it.orEmpty() }
    fun ticket(id: Int): Flow<TicketDto?> = observar<TicketDto>("ticket_$id")
    fun resultados(id: Int): Flow<ResultadosCampanaDto?> = observar<ResultadosCampanaDto>("resultados_$id")

    // ---------- refrescar ----------
    suspend fun refrescarPanel(): String? = listOf(
        traer("resumen", { api.resumen() }),
        traer("embudo", { api.embudo() }),
        traer("pronostico", { api.pronostico() }),
        traer("ventas", { api.ventasMensuales() }),
        traer("por_aprobar", { api.cotizacionesPorEstado("PEND_APROBACION") }, { it.datos.orEmpty() })
    ).firstOrNull { it != null }

    suspend fun refrescarClientes(): String? = listOf(
        traer("riesgo", { api.clientesRiesgo(0.5) }),
        traer("rentables", { api.clientesRentables() })
    ).firstOrNull { it != null }

    suspend fun refrescarEquipo(): String? = listOf(
        traer("leads_equipo", { api.leadsEquipo() }, { it.datos.orEmpty() }),
        traer("vendedores", { api.vendedoresCarga() }, { it.datos.orEmpty() }),
        traer("por_aprobar", { api.cotizacionesPorEstado("PEND_APROBACION") }, { it.datos.orEmpty() })
    ).firstOrNull { it != null }

    suspend fun refrescarCampanas(): String? = listOf(
        traer("campanas", { api.campanas() }, { it.datos.orEmpty() }),
        traer("productos", { api.productos() }, { it.datos.orEmpty() })
    ).firstOrNull { it != null }

    suspend fun refrescarModelos(): String? = listOf(
        traer("modelos", { api.modelos() }, { it.datos.orEmpty() }),
        traer("servicio", { api.servicioAnalitica() })
    ).firstOrNull { it != null }

    suspend fun refrescarResultados(id: Int): String? = traer("resultados_$id", { api.resultadosCampana(id) })

    suspend fun refrescarTickets(esAgente: Boolean): String? =
        traer("tickets", { if (esAgente) api.tickets() else api.tickets(cola = null, estado = "ABIERTA,EN_ATENCION,RESUELTA") }, { it.datos.orEmpty() })

    suspend fun refrescarTicket(id: Int): String? = traer("ticket_$id", { api.ticket(id) }, { it.dato ?: Unit })

    suspend fun fotoTicket(id: Int): ByteArray? = try {
        val r = api.fotoTicket(id)
        if (r.isSuccessful) r.body()?.bytes() else null
    } catch (e: IOException) { null }

    suspend fun refrescarNotificaciones(): String? = when (val r = llamarApi { api.notificaciones() }) {
        is Resultado.Ok -> {
            val lista = r.dato.datos.orEmpty()
            guardar("notificaciones", lista)
            val maximo = lista.maxOfOrNull { it.id } ?: 0L
            val ultima = sesion.ultimaNotificacion
            if (ultima < 0) sesion.ultimaNotificacion = maximo
            else {
                lista.filter { it.id > ultima && it.leida != true }.sortedBy { it.id }.takeLast(5).forEach {
                    Notificador.mostrar(context, it.id.toInt(), it.titulo ?: "CRM Andino", it.cuerpo ?: "")
                }
                if (maximo > ultima) sesion.ultimaNotificacion = maximo
            }
            null
        }
        is Resultado.Error -> r.mensaje
    }

    suspend fun marcarLeidas() {
        if (llamarApi { api.marcarLeidas(MarcarLeidasRequest(null)) } is Resultado.Ok) refrescarNotificaciones()
    }

    // ---------- acciones ----------
    private fun op() = UUID.randomUUID().toString()

    suspend fun aprobar(c: CotizacionDto, aprobar: Boolean, observacion: String?): Resultado<Any> =
        llamarApi { api.aprobar(op(), c.id, AprobacionRequest(aprobar, observacion)) }

    suspend fun reasignar(ids: List<Int>, vendedorId: Int): Resultado<com.crmandino.supervisor.data.remote.dto.ReasignarResponse> =
        llamarApi { api.reasignar(op(), ReasignarRequest(ids, vendedorId)) }

    suspend fun previsualizar(s: SegmentoCampana) = llamarApi { api.previsualizar(PrevisualizarRequest(s)) }

    suspend fun crearCampana(req: CampanaRequest) = llamarApi { api.crearCampana(op(), req) }

    suspend fun enviarCampana(id: Int) = llamarApi { api.enviarCampana(op(), id) }

    suspend fun crearRetencion(clienteId: Int, vendedorId: Int?, descripcion: String, fechaIso: String) =
        llamarApi { api.crearRetencion(op(), RetencionRequest(UUID.randomUUID().toString(), clienteId, vendedorId, descripcion, fechaIso)) }

    suspend fun entrenar() = llamarApi { api.entrenar() }

    suspend fun cambiarEstadoTicket(t: TicketDto, estado: String, solucion: String?, nota: String?) =
        llamarApi { api.cambiarEstadoTicket(op(), t.id, EstadoTicketRequest(estado, solucion, nota, t.version)) }
}
