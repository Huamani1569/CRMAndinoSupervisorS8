package com.crmandino.supervisor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.supervisor.data.remote.dto.CampanaRequest
import com.crmandino.supervisor.data.remote.dto.ClienteRiesgoDto
import com.crmandino.supervisor.data.remote.dto.CotizacionDto
import com.crmandino.supervisor.data.remote.dto.ResultadosCampanaDto
import com.crmandino.supervisor.data.remote.dto.SegmentoCampana
import com.crmandino.supervisor.data.remote.dto.TicketDto
import com.crmandino.supervisor.data.repository.GestionRepository
import com.crmandino.supervisor.util.Formato
import com.crmandino.supervisor.util.Resultado
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val CANALES_CAMPANA = linkedMapOf("PUSH" to "Push (Portal)", "WHATSAPP" to "WhatsApp", "CORREO" to "Correo", "LLAMADA" to "Llamada", "VISITA" to "Visita")

// ViewModel de las Semanas 12–14 en la app Supervisor (y del agente de servicio).
class GestionViewModel(private val repo: GestionRepository, val conectado: StateFlow<Boolean>) : ViewModel() {

    private fun <T> Flow<T>.estado(inicial: T): StateFlow<T> = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), inicial)

    val resumen = repo.resumen.estado(null)
    val embudo = repo.embudo.estado(null)
    val pronostico = repo.pronostico.estado(null)
    val ventasMensuales = repo.ventasMensuales.estado(null)
    val riesgo = repo.riesgo.estado(null)
    val rentables = repo.rentables.estado(null)
    val servicio = repo.servicio.estado(null)
    val modelos = repo.modelos.estado(emptyList())
    val porAprobar = repo.porAprobar.estado(emptyList())
    val leadsEquipo = repo.leadsEquipo.estado(emptyList())
    val vendedores = repo.vendedores.estado(emptyList())
    val campanas = repo.campanas.estado(emptyList())
    val productos = repo.productos.estado(emptyList())
    val tickets = repo.tickets.estado(emptyList())
    val notificaciones = repo.notificaciones.estado(emptyList())
    val noLeidas: StateFlow<Int> = repo.notificaciones.map { l -> l.count { it.leida != true } }.estado(0)

    fun ticket(id: Int): Flow<TicketDto?> = repo.ticket(id)
    fun resultados(id: Int): Flow<ResultadosCampanaDto?> = repo.resultados(id)
    suspend fun fotoTicket(id: Int): ByteArray? = repo.fotoTicket(id)

    var cargando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var mensajeEsError by mutableStateOf(false)
        private set
    var vistaPrevia by mutableStateOf<String?>(null)
        private set

    fun avisar(texto: String, error: Boolean = false) {
        mensaje = texto
        mensajeEsError = error
        viewModelScope.launch { delay(6_000); if (mensaje == texto) mensaje = null }
    }

    fun cerrarMensaje() { mensaje = null }

    private fun cargar(bloque: suspend () -> String?) = viewModelScope.launch {
        cargando = true
        val error = bloque()
        if (error != null) avisar(if (conectado.value) error else "Sin conexión: se muestran los últimos datos guardados.", true)
        cargando = false
    }

    fun refrescarPanel() = cargar { repo.refrescarPanel() }
    fun refrescarClientes() = cargar { repo.refrescarClientes() }
    fun refrescarEquipo() = cargar { repo.refrescarEquipo() }
    fun refrescarCampanas() = cargar { repo.refrescarCampanas() }
    fun refrescarModelos() = cargar { repo.refrescarModelos() }
    fun refrescarResultados(id: Int) = cargar { repo.refrescarResultados(id) }
    fun refrescarTickets(esAgente: Boolean) = cargar { repo.refrescarTickets(esAgente) }
    fun refrescarTicket(id: Int) = viewModelScope.launch { repo.refrescarTicket(id) }
    fun marcarLeidas() = viewModelScope.launch { repo.marcarLeidas() }

    private var sondeo: Job? = null
    fun iniciarSondeo(esAgente: Boolean) {
        if (sondeo?.isActive == true) return
        sondeo = viewModelScope.launch {
            while (isActive) {
                if (conectado.value) {
                    repo.refrescarNotificaciones()
                    if (esAgente) repo.refrescarTickets(true)
                }
                delay(30_000)
            }
        }
    }

    // Ejecuta una acción en línea y avisa el resultado.
    private fun <T> accion(textoOk: (T) -> String, despues: suspend () -> Unit = {}, bloque: suspend () -> Resultado<T>) = viewModelScope.launch {
        if (!conectado.value) { avisar("Necesitas conexión para esta acción.", true); return@launch }
        cargando = true
        when (val r = bloque()) {
            is Resultado.Ok -> { avisar(textoOk(r.dato)); despues() }
            is Resultado.Error -> avisar(r.mensaje, true)
        }
        cargando = false
    }

    // ---------- Aprobación de descuentos (RN-03) ----------
    fun aprobar(c: CotizacionDto) = accion({ _: Any -> "Descuento aprobado: ${c.numero} fue enviada a ${c.empresa}." }, { repo.refrescarPanel() }) {
        repo.aprobar(c, true, null)
    }

    fun observar(c: CotizacionDto, observacion: String) = accion({ _: Any -> "Se devolvió ${c.numero} al vendedor con tu observación." }, { repo.refrescarPanel() }) {
        repo.aprobar(c, false, observacion.trim().ifBlank { null })
    }

    // ---------- Reasignación en lote (RF-S03) ----------
    fun reasignar(ids: Set<Int>, vendedorId: Int?, alTerminar: () -> Unit) {
        if (ids.isEmpty() || vendedorId == null) { avisar("Elige los leads y el vendedor destino.", true); return }
        accion({ r: com.crmandino.supervisor.data.remote.dto.ReasignarResponse -> "${r.reasignados ?: 0} lead(s) reasignados a ${r.vendedorDestino}. Se le notificó." },
            { repo.refrescarEquipo(); alTerminar() }) {
            repo.reasignar(ids.toList(), vendedorId)
        }
    }

    // ---------- Campañas (RF-S06, RF-S07, RF-S08) ----------
    fun previsualizar(s: SegmentoCampana) = viewModelScope.launch {
        vistaPrevia = "Calculando…"
        vistaPrevia = when (val r = repo.previsualizar(s)) {
            is Resultado.Ok -> "${r.dato.clientes ?: 0} cliente(s) recibirán la campaña · ${r.dato.excluidosSinOfertas ?: 0} excluidos porque no aceptan ofertas (Ley 29733)"
            is Resultado.Error -> r.mensaje
        }
    }

    fun limpiarVistaPrevia() { vistaPrevia = null }

    fun crearCampana(
        nombre: String, oferta: String, productoId: Int?, descuentoTexto: String, canales: Set<String>,
        inicio: String, fin: String, segmento: SegmentoCampana, enviarYa: Boolean, alTerminar: () -> Unit
    ): String? {
        val descuento = descuentoTexto.replace(",", ".").ifBlank { "0" }.toDoubleOrNull()
        val error = when {
            nombre.isBlank() -> "Escribe el nombre de la campaña"
            oferta.isBlank() -> "Describe la oferta"
            canales.isEmpty() -> "Elige al menos un canal"
            descuento == null || descuento < 0 || descuento > 50 -> "El descuento debe estar entre 0 % y 50 %"
            !Regex("\\d{4}-\\d{2}-\\d{2}").matches(inicio) || !Regex("\\d{4}-\\d{2}-\\d{2}").matches(fin) -> "Fechas en formato AAAA-MM-DD"
            fin < inicio -> "La fecha de fin no puede ser anterior al inicio"
            else -> null
        }
        if (error != null || descuento == null) return error
        val req = CampanaRequest(nombre.trim(), oferta.trim(), productoId, descuento, canales.toList(), inicio, fin, segmento)
        viewModelScope.launch {
            if (!conectado.value) { avisar("Necesitas conexión para crear la campaña.", true); return@launch }
            cargando = true
            when (val r = repo.crearCampana(req)) {
                is Resultado.Ok -> {
                    val id = r.dato.dato?.id
                    if (enviarYa && id != null) {
                        when (val e = repo.enviarCampana(id)) {
                            is Resultado.Ok -> avisar("Campaña enviada a ${e.dato.clientes ?: 0} cliente(s) (${e.dato.envios ?: 0} envíos).")
                            is Resultado.Error -> avisar("Campaña creada, pero no se pudo enviar: ${e.mensaje}", true)
                        }
                    } else avisar("Campaña guardada como borrador.")
                    repo.refrescarCampanas()
                    alTerminar()
                }
                is Resultado.Error -> avisar(r.mensaje, true)
            }
            cargando = false
        }
        return null
    }

    fun enviarCampana(id: Int) = accion({ e: com.crmandino.supervisor.data.remote.dto.EnvioCampanaResponse ->
        "Campaña enviada a ${e.clientes ?: 0} cliente(s) (${e.envios ?: 0} envíos)."
    }, { repo.refrescarCampanas() }) { repo.enviarCampana(id) }

    // ---------- Retención (RF-S11) ----------
    fun crearRetencion(c: ClienteRiesgoDto, descripcion: String, dias: Int) =
        accion({ _: Any -> "Tarea de retención creada para ${c.vendedor ?: "el vendedor"}. Se le notificó." }, { repo.refrescarClientes() }) {
            repo.crearRetencion(c.clienteId, c.vendedorId, descripcion.trim(), Formato.hoyIso(dias))
        }

    // ---------- Modelos ML (Semana 14) ----------
    fun entrenar() = accion({ _: Any -> "Modelos reentrenados con los datos actuales." }, { repo.refrescarModelos(); repo.refrescarPanel() }) {
        repo.entrenar()
    }

    // ---------- Servicio: agente (proceso de lealtad) ----------
    fun tomar(t: TicketDto, esAgente: Boolean) = accion({ _: Any -> "Tomaste ${t.numero}: ahora está EN ATENCIÓN." }, { repo.refrescarTicket(t.id); repo.refrescarTickets(esAgente) }) {
        repo.cambiarEstadoTicket(t, "EN_ATENCION", null, null)
    }

    fun resolver(t: TicketDto, solucion: String, esAgente: Boolean): String? {
        if (solucion.trim().length < 5) return "Describe la solución (mínimo 5 caracteres)"
        accion({ _: Any -> "${t.numero} marcada como RESUELTA. El cliente confirmará y calificará." }, { repo.refrescarTicket(t.id); repo.refrescarTickets(esAgente) }) {
            repo.cambiarEstadoTicket(t, "RESUELTA", solucion.trim(), null)
        }
        return null
    }

    fun devolver(t: TicketDto, nota: String, esAgente: Boolean) = accion({ _: Any -> "${t.numero} volvió a la cola general." }, { repo.refrescarTicket(t.id); repo.refrescarTickets(esAgente) }) {
        repo.cambiarEstadoTicket(t, "ABIERTA", null, nota.trim().ifBlank { null })
    }
}
