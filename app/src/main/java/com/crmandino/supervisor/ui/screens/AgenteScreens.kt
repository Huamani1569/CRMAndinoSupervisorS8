package com.crmandino.supervisor.ui.screens

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crmandino.supervisor.data.remote.dto.TicketDto
import com.crmandino.supervisor.model.UsuarioSesion
import com.crmandino.supervisor.ui.components.BarraVidrio
import com.crmandino.supervisor.ui.components.BotonCapsula
import com.crmandino.supervisor.ui.components.BotonCapsulaSecundario
import com.crmandino.supervisor.ui.components.BotonCircular
import com.crmandino.supervisor.ui.components.CampoVidrio
import com.crmandino.supervisor.ui.components.Capsula
import com.crmandino.supervisor.ui.components.FilaDato
import com.crmandino.supervisor.ui.components.FondoAndino
import com.crmandino.supervisor.ui.components.IconoCirculo
import com.crmandino.supervisor.ui.components.KpiVidrio
import com.crmandino.supervisor.ui.components.MensajeVidrio
import com.crmandino.supervisor.ui.components.PuntajeCirculo
import com.crmandino.supervisor.ui.components.SegmentadoVidrio
import com.crmandino.supervisor.ui.components.TarjetaVidrio
import com.crmandino.supervisor.ui.components.VacioVidrio
import com.crmandino.supervisor.ui.theme.AzulAndino
import com.crmandino.supervisor.ui.theme.DoradoInca
import com.crmandino.supervisor.ui.theme.EtapaContactado
import com.crmandino.supervisor.ui.theme.RojoError
import com.crmandino.supervisor.ui.theme.TextoSecundario
import com.crmandino.supervisor.ui.theme.TurquesaLago
import com.crmandino.supervisor.ui.theme.VerdeExito
import com.crmandino.supervisor.util.Formato
import com.crmandino.supervisor.viewmodel.GestionViewModel

private val CATEGORIAS = mapOf("FACTURACION" to "Facturación", "ACCESO" to "Acceso", "ERROR_SISTEMA" to "Error del sistema", "CAPACITACION" to "Capacitación", "OTRO" to "Otro")

private fun textoEstado(e: String?) = when (e) {
    "ABIERTA" -> "Abierta"; "EN_ATENCION" -> "En atención"; "RESUELTA" -> "Resuelta"; "CERRADA" -> "Cerrada"; else -> e ?: "—"
}

private fun colorEstado(e: String?) = when (e) {
    "ABIERTA" -> EtapaContactado; "EN_ATENCION" -> DoradoInca; "RESUELTA" -> TurquesaLago; else -> VerdeExito
}

// ============================================================ COLA DEL AGENTE (proceso de lealtad, RF-S04)
// Orden: primero la prioridad ALTA (clientes de alto valor), luego la más antigua. El SLA en rojo indica vencido.
@Composable
fun AgenteColaScreen(
    usuario: UsuarioSesion,
    vm: GestionViewModel,
    onAbrirTicket: (Int) -> Unit,
    onNotificaciones: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val tickets by vm.tickets.collectAsState()
    val noLeidas by vm.noLeidas.collectAsState()
    var sub by rememberSaveable { mutableIntStateOf(0) }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        vm.refrescarTickets(true)
        vm.iniciarSondeo(true)
    }
    val mios = tickets.filter { it.agenteId == usuario.id }
    val general = tickets.filter { it.agenteId == null }
    val visibles = if (sub == 0) mios else general
    val vencidos = tickets.count { it.slaVencido == true }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(
                "Hola, ${usuario.nombre.substringBefore(" ")}", "Agente de servicio",
                acciones = {
                    BotonCircular(Icons.Filled.Refresh, "Actualizar", { vm.refrescarTickets(true) })
                    BotonCircular(Icons.Filled.Notifications, "Notificaciones", onNotificaciones, insignia = noLeidas)
                    BotonCircular(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", onCerrarSesion)
                }
            )
            vm.mensaje?.let { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { MensajeVidrio(it, vm.mensajeEsError, vm::cerrarMensaje) } }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiVidrio(Icons.Filled.AssignmentInd, "${mios.count { it.estado != "RESUELTA" }}", "Asignadas a mí", AzulAndino, Modifier.weight(1f))
                        KpiVidrio(Icons.Filled.Inbox, "${general.size}", "En cola general", TurquesaLago, Modifier.weight(1f))
                        KpiVidrio(Icons.Filled.Timer, "$vencidos", "SLA vencido", if (vencidos > 0) RojoError else VerdeExito, Modifier.weight(1f))
                    }
                }
                item { SegmentadoVidrio(listOf("Mi cola (${mios.size})", "Cola general (${general.size})"), sub, { sub = it }) }
                if (visibles.isEmpty()) {
                    item { VacioVidrio(Icons.Filled.CheckCircle, if (sub == 0) "No tienes solicitudes asignadas" else "La cola general está vacía", null) }
                }
                items(visibles, key = { it.id }) { t -> TicketCard(t) { onAbrirTicket(t.id) } }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun TicketCard(t: TicketDto, onClick: () -> Unit) {
    val colorSla = when {
        t.slaVencido == true -> RojoError
        t.slaPorVencer == true -> DoradoInca
        else -> VerdeExito
    }
    TarjetaVidrio(onClick = onClick, tinte = if (t.slaVencido == true) RojoError else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PuntajeCirculo(t.puntajeCliente, tamano = 40.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${t.numero} · ${t.empresa}", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(CATEGORIAS[t.categoria] ?: t.categoria ?: "", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
            Capsula(textoEstado(t.estado), colorEstado(t.estado))
        }
        Text(t.descripcion ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (t.prioridad == "ALTA") Capsula("Prioridad ALTA", DoradoInca, icono = Icons.Filled.Verified)
            if (t.estado == "ABIERTA" || t.estado == "EN_ATENCION") {
                val h = Formato.horasHasta(t.slaHasta)
                Capsula(
                    when {
                        h == null -> "SLA —"
                        h < 0 -> "SLA vencido hace ${(-h).toInt()} h"
                        else -> "SLA: quedan ${h.toInt()} h"
                    }, colorSla, icono = Icons.Filled.Timer
                )
            }
        }
        Text((t.agenteNombre?.let { "Agente: $it · " } ?: "Sin agente · ") + Formato.haceCuanto(t.creadoEn),
            style = MaterialTheme.typography.labelMedium, color = TextoSecundario, modifier = Modifier.padding(top = 4.dp))
    }
}

// ============================================================ DETALLE DEL TICKET (agente y supervisor)
// Perfil del cliente (valor, CLTV, compras, satisfacción) + foto + historial. El agente toma, resuelve o devuelve.
@Composable
fun TicketDetalleScreen(usuario: UsuarioSesion, vm: GestionViewModel, id: Int, onAtras: () -> Unit) {
    val lista by vm.tickets.collectAsState()
    val detalle by remember(id) { vm.ticket(id) }.collectAsState(initial = null)
    val t = detalle ?: lista.firstOrNull { it.id == id }
    var resolver by remember { mutableStateOf(false) }
    var devolver by remember { mutableStateOf(false) }
    val esAgente = usuario.rol == "agente"
    LaunchedEffect(id) { vm.refrescarTicket(id) }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(t?.numero ?: "Solicitud", t?.empresa, onAtras = onAtras)
            vm.mensaje?.let { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { MensajeVidrio(it, vm.mensajeEsError, vm::cerrarMensaje) } }
            if (t == null) {
                Box(Modifier.padding(16.dp)) { VacioVidrio(Icons.Filled.SupportAgent, "Cargando solicitud…", null) }
            } else {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        TarjetaVidrio(tinte = colorEstado(t.estado)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconoCirculo(Icons.Filled.SupportAgent, colorEstado(t.estado), 46.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(textoEstado(t.estado), style = MaterialTheme.typography.titleLarge, color = AzulAndino)
                                    Text("${CATEGORIAS[t.categoria] ?: t.categoria} · creada ${Formato.fechaHora(t.creadoEn)}", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                                }
                                if (t.prioridad == "ALTA") Capsula("ALTA", DoradoInca, icono = Icons.Filled.Verified)
                            }
                            Spacer(Modifier.height(8.dp))
                            FilaDato("Agente", t.agenteNombre?.let { it + if (t.agenteSenior == true) " (senior)" else "" } ?: "Cola general")
                            FilaDato("Responder antes de", Formato.fechaHora(t.slaHasta))
                            t.calificacion?.let { FilaDato("Calificación del cliente", "$it ★" + (t.comentarioCalificacion?.let { c -> " · “$c”" } ?: "")) }
                        }
                    }
                    t.perfilCliente?.let { p ->
                        item {
                            TarjetaVidrio {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PuntajeCirculo(p.puntajeValor, tamano = 54.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Perfil del cliente", style = MaterialTheme.typography.titleMedium)
                                        Text(if ((p.puntajeValor ?: 0) >= 70) "Alto valor: atención preferente" else "Cliente regular",
                                            style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                FilaDato("RUC", p.ruc ?: "—")
                                FilaDato("Cliente desde", Formato.fecha(p.clienteDesde))
                                FilaDato("Compras", "${p.compras ?: 0} · ${Formato.soles(p.comprasTotal, false)}")
                                FilaDato("Valor de vida (CLTV)", Formato.soles(p.cltv, false))
                                FilaDato("Riesgo de cancelación", p.probCancelacion?.let { "${(it * 100).toInt()} %" } ?: "—")
                                FilaDato("Tickets previos", "${p.ticketsPrevios ?: 0}" + (p.csatPromedio?.let { " · satisfacción $it ★" } ?: ""))
                                FilaDato("Vendedor", p.vendedorNombre ?: "—")
                            }
                        }
                    }
                    item {
                        TarjetaVidrio {
                            Text("Problema reportado", style = MaterialTheme.typography.titleMedium)
                            Text(t.descripcion ?: "", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
                            if (t.tieneFoto == true) {
                                Spacer(Modifier.height(10.dp))
                                FotoTicket(vm, t.id)
                            }
                            t.solucion?.let {
                                Spacer(Modifier.height(10.dp))
                                Text("Solución", style = MaterialTheme.typography.titleMedium, color = TurquesaLago)
                                Text(it, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                    val historial = t.historial.orEmpty()
                    if (historial.isNotEmpty()) {
                        item {
                            TarjetaVidrio {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconoCirculo(Icons.Filled.History, AzulAndino, 34.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Historial", style = MaterialTheme.typography.titleMedium)
                                }
                                historial.forEach { e ->
                                    Text("${Formato.fechaHora(e.fecha)} · ${textoEstado(e.estadoAnterior)} → ${textoEstado(e.estado)}" +
                                        (e.usuarioNombre?.let { " ($it)" } ?: "") + (e.nota?.let { ": $it" } ?: ""),
                                        style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }
                }
                // Acciones del agente
                if (esAgente) {
                    Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                        when {
                            t.estado == "ABIERTA" && (t.agenteId == null || t.agenteId == usuario.id) ->
                                BotonCapsula("Tomar la solicitud", { vm.tomar(t, true) }, Modifier.fillMaxWidth(), icono = Icons.Filled.AssignmentInd, cargando = vm.cargando)
                            t.estado == "EN_ATENCION" && t.agenteId == usuario.id ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    BotonCapsulaSecundario("Devolver", { devolver = true }, Modifier.weight(1f), icono = Icons.Filled.Replay, color = RojoError)
                                    BotonCapsula("Resolver", { resolver = true }, Modifier.weight(1.4f), icono = Icons.Filled.CheckCircle, color = VerdeExito)
                                }
                            t.estado == "RESUELTA" -> Capsula("Esperando que el cliente confirme y califique", TurquesaLago, icono = Icons.Filled.Star)
                            t.agenteId != null && t.agenteId != usuario.id -> Capsula("La atiende ${t.agenteNombre}", AzulAndino)
                        }
                    }
                }
            }
        }
    }

    if (t != null && resolver) {
        var solucion by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { resolver = false },
            title = { Text("Resolver ${t.numero}") },
            text = {
                Column {
                    Text("El cliente recibirá una notificación para confirmar la solución y calificar la atención.", color = TextoSecundario)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(solucion, { solucion = it; error = null }, "¿Qué hiciste para resolverlo?", lineas = 3, error = error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    error = vm.resolver(t, solucion, esAgente)
                    if (error == null) resolver = false
                }) { Text("Marcar resuelta", color = VerdeExito, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { resolver = false }) { Text("Cancelar") } }
        )
    }
    if (t != null && devolver) {
        var nota by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { devolver = false },
            title = { Text("Devolver a la cola") },
            text = { CampoVidrio(nota, { nota = it }, "Motivo (opcional)", lineas = 2) },
            confirmButton = { TextButton(onClick = { vm.devolver(t, nota, esAgente); devolver = false }) { Text("Devolver", color = RojoError, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { devolver = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FotoTicket(vm: GestionViewModel, id: Int) {
    val bytes by produceState<ByteArray?>(initialValue = null, id) { value = vm.fotoTicket(id) }
    val imagen = remember(bytes) { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() } }
    if (imagen == null) {
        Text("Cargando foto…", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
    } else {
        Image(imagen, contentDescription = "Foto enviada por el cliente", contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(18.dp)))
    }
}
