package com.crmandino.supervisor.data.remote.dto

// DTO de las Semanas 12–14 para la app Supervisor (y el agente de servicio).

// ---------- Analítica (Semana 14) ----------
data class PronosticoDto(
    val mes: String?,
    val ganadoReal: Double?,
    val esperadoAbiertos: Double?,
    val pronostico: Double?,
    val meta: Double?,
    val formula: String?,
    val vendedorId: Int? = null,
    val vendedor: String? = null
)

data class KpisDto(
    val tasaConversion: Double?,
    val tiempoMedioResolucionH: Double?,
    val csat: Double?,
    val tasaCancelacion: Double?,
    val cltvPromedio: Double?,
    val leadsAbiertos: Int?,
    val ticketsAbiertos: Int?,
    val cotizacionesPorAprobar: Int?,
    val clientesEnRiesgo: Int?
)

data class ResumenDto(val mes: String?, val pronostico: PronosticoDto?, val kpis: KpisDto?)

data class EtapaEmbudoDto(val etapa: String, val cantidad: Int?, val valor: Double?)
data class EmbudoDto(val mes: String?, val etapas: List<EtapaEmbudoDto>?)

data class PronosticoEquipoDto(
    val mes: String?,
    val ganadoReal: Double?,
    val esperadoAbiertos: Double?,
    val pronostico: Double?,
    val meta: Double?,
    val porVendedor: List<PronosticoDto>?
)

data class PuntoVentasDto(val mes: String, val real: Double?, val pronostico: Double?)
data class VentasMensualesDto(val serie: List<PuntoVentasDto>?, val meta: Double?)

data class FactorDto(val factor: String?, val peso: Double?)

data class ClienteRiesgoDto(
    val clienteId: Int,
    val razonSocial: String?,
    val ruc: String?,
    val probCancelacion: Double?,
    val cltv: Double?,
    val factores: List<FactorDto>?,
    val puntajeValor: Int?,
    val vendedorId: Int?,
    val vendedor: String?,
    val ultimaCompra: String?,
    val tieneTareaRetencion: Boolean?
)
data class ClientesRiesgoDto(val minimo: Double?, val total: Int?, val datos: List<ClienteRiesgoDto>?)

data class ClienteRentableDto(val clienteId: Int, val razonSocial: String?, val distrito: String?, val puntajeValor: Int?, val cltv: Double?, val ventas: Double?, val margen: Double?)
data class SegmentoDto(val segmento: String?, val clientes: Int?, val ventas: Double?, val margen: Double?)
data class RentablesDto(val top: List<ClienteRentableDto>?, val segmentos: List<SegmentoDto>?)

data class CeldaCalorDto(val dia: Int, val hora: Int, val n: Int)
data class SlaDto(val prioridad: String?, val total: Int?, val cumplimiento: Double?, val horasPromedio: Double?)
data class ServicioAnaliticaDto(val porDiaHora: List<CeldaCalorDto>?, val sla: List<SlaDto>?)

data class ModeloDto(val nombre: String?, val algoritmo: String?, val version: Int?, val metricas: Map<String, Any?>?, val filas: Int?, val entrenadoEn: String?)

// ---------- Leads del equipo (reasignación en lote) ----------
data class LeadEquipoDto(
    val id: Int,
    val uuid: String?,
    val empresa: String?,
    val contacto: String?,
    val valorPotencial: Double?,
    val etapa: String?,
    val canalOrigen: String?,
    val puntaje: Int?,
    val diasSinContacto: Int?,
    val vendedorId: Int?,
    val vendedorNombre: String?
)
data class ReasignarRequest(val leadIds: List<Int>, val vendedorDestinoId: Int)
data class ReasignarResponse(val reasignados: Int?, val vendedorDestino: String?, val duplicada: Boolean?)

data class VendedorCargaDto(val id: Int, val nombre: String?, val correo: String?, val territorio: String?, val leadsAbiertos: Int?)

// ---------- Cotizaciones por aprobar (RN-03) ----------
data class ItemCotizacionDto(val productoId: Int?, val nombre: String?, val cantidad: Int?, val precioUnit: Double?, val subtotal: Double?)
data class CotizacionDto(
    val id: Int,
    val numero: String?,
    val empresa: String?,
    val vendedorNombre: String?,
    val items: List<ItemCotizacionDto>?,
    val subtotal: Double?,
    val descuentoPct: Double?,
    val total: Double?,
    val estado: String?,
    val aprobadaPor: String?,
    val version: Int?
)
data class AprobacionRequest(val aprobar: Boolean, val observacion: String?)

// ---------- Campañas (Semana 13) ----------
data class SegmentoCampana(
    val distrito: String? = null,
    val segmento: String? = null,
    val tieneProducto: String? = null,
    val sinProducto: String? = null,
    val puntajeValorMin: Int? = null,
    val riesgoMin: Double? = null
)
data class CampanaRequest(
    val nombre: String,
    val oferta: String,
    val productoId: Int?,
    val descuentoPct: Double,
    val canales: List<String>,
    val inicio: String,
    val fin: String,
    val segmento: SegmentoCampana
)
data class PrevisualizarRequest(val segmento: SegmentoCampana)
data class PrevisualizarResponse(val clientes: Int?, val excluidosSinOfertas: Int?)
data class CampanaDto(
    val id: Int,
    val nombre: String?,
    val oferta: String?,
    val producto: String?,
    val descuentoPct: Double?,
    val canales: List<String>?,
    val inicio: String?,
    val fin: String?,
    val estado: String?,
    val creadaPor: String?,
    val alcanzados: Int?,
    val envios: Int?,
    val respuestas: Int?,
    val tasaRespuesta: Double?,
    val leadsGenerados: Int?,
    val ganados: Int?,
    val conversion: Double?,
    val tamanoSegmento: Int?
)
data class EnvioCampanaResponse(val clientes: Int?, val envios: Int?, val duplicada: Boolean?)
data class CanalResultadoDto(val canal: String?, val enviados: Int?, val respuestas: Int?, val tasaRespuesta: Double?)
data class ResultadosCampanaDto(val dato: CampanaDto?, val porCanal: List<CanalResultadoDto>?)
data class ProductoDto(val id: Int, val codigo: String?, val nombre: String?, val categoria: String?, val precio: Double?)

// ---------- Tarea de retención (RF-S11) ----------
data class RetencionRequest(val uuid: String, val clienteId: Int, val vendedorId: Int?, val descripcion: String, val fechaLimite: String)
data class TareaCreadaDto(val id: Int?, val descripcion: String?, val vendedorNombre: String?)

// ---------- Servicio al cliente (agente) ----------
data class EventoDto(val estado: String?, val estadoAnterior: String?, val nota: String?, val usuarioNombre: String?, val fecha: String?)
data class PerfilClienteDto(
    val id: Int?,
    val ruc: String?,
    val razonSocial: String?,
    val clienteDesde: String?,
    val vendedorNombre: String?,
    val puntajeValor: Int?,
    val probCancelacion: Double?,
    val cltv: Double?,
    val compras: Int?,
    val comprasTotal: Double?,
    val ticketsPrevios: Int?,
    val csatPromedio: Double?
)
data class TicketDto(
    val id: Int,
    val numero: String?,
    val clienteId: Int?,
    val empresa: String?,
    val categoria: String?,
    val descripcion: String?,
    val tieneFoto: Boolean?,
    val prioridad: String?,
    val puntajeCliente: Int?,
    val agenteId: Int?,
    val agenteNombre: String?,
    val agenteSenior: Boolean?,
    val estado: String?,
    val slaHasta: String?,
    val slaPorVencer: Boolean?,
    val slaVencido: Boolean?,
    val solucion: String?,
    val resueltaEn: String?,
    val calificacion: Int?,
    val comentarioCalificacion: String?,
    val creadoEn: String?,
    val version: Int?,
    val historial: List<EventoDto>?,
    val perfilCliente: PerfilClienteDto?
)
data class EstadoTicketRequest(val estado: String, val solucion: String?, val nota: String?, val version: Int?)

// ---------- Notificaciones ----------
data class NotificacionDto(val id: Long, val tipo: String?, val titulo: String?, val cuerpo: String?, val refTipo: String?, val refId: Int?, val leida: Boolean?, val creadoEn: String?)
data class NotificacionesResponse(val servidorTiempo: String?, val total: Int?, val noLeidas: Int?, val datos: List<NotificacionDto>?)
data class MarcarLeidasRequest(val hastaId: Long?)
data class MarcadasResponse(val marcadas: Int?)
data class EntrenamientoResponse(val ok: Boolean?)
