package com.crmandino.supervisor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crmandino.supervisor.data.remote.dto.SegmentoCampana
import com.crmandino.supervisor.ui.components.BarraVidrio
import com.crmandino.supervisor.ui.components.BarrasHorizontales
import com.crmandino.supervisor.ui.components.BotonCapsula
import com.crmandino.supervisor.ui.components.BotonCapsulaSecundario
import com.crmandino.supervisor.ui.components.BotonCircular
import com.crmandino.supervisor.ui.components.CampoVidrio
import com.crmandino.supervisor.ui.components.Capsula
import com.crmandino.supervisor.ui.components.ChipsVidrio
import com.crmandino.supervisor.ui.components.FilaDato
import com.crmandino.supervisor.ui.components.FondoAndino
import com.crmandino.supervisor.ui.components.IconoCirculo
import com.crmandino.supervisor.ui.components.KpiVidrio
import com.crmandino.supervisor.ui.components.MapaCalor
import com.crmandino.supervisor.ui.components.MensajeVidrio
import com.crmandino.supervisor.ui.components.Seccion
import com.crmandino.supervisor.ui.components.TarjetaVidrio
import com.crmandino.supervisor.ui.components.VacioVidrio
import com.crmandino.supervisor.ui.theme.AzulAndino
import com.crmandino.supervisor.ui.theme.DoradoInca
import com.crmandino.supervisor.ui.theme.GrisNeutro
import com.crmandino.supervisor.ui.theme.RojoError
import com.crmandino.supervisor.ui.theme.TextoSecundario
import com.crmandino.supervisor.ui.theme.TurquesaLago
import com.crmandino.supervisor.ui.theme.VerdeExito
import com.crmandino.supervisor.util.Formato
import com.crmandino.supervisor.viewmodel.CANALES_CAMPANA
import com.crmandino.supervisor.viewmodel.GestionViewModel
import java.util.Locale

// ============================================================ NUEVA CAMPAÑA (RF-S06, RF-S07)
// Segmento → vista previa del tamaño → oferta y canales → guardar o enviar. Solo reciben clientes que aceptan ofertas.
@Composable
fun NuevaCampanaScreen(vm: GestionViewModel, onAtras: () -> Unit) {
    val productos by vm.productos.collectAsState()
    var nombre by rememberSaveable { mutableStateOf("") }
    var oferta by rememberSaveable { mutableStateOf("") }
    var descuento by rememberSaveable { mutableStateOf("10") }
    var inicio by rememberSaveable { mutableStateOf(Formato.hoyIso()) }
    var fin by rememberSaveable { mutableStateOf(Formato.hoyIso(14)) }
    var canales by remember { mutableStateOf(setOf("PUSH")) }
    var productoOferta by remember { mutableStateOf<Int?>(null) }
    // Segmento
    var distrito by rememberSaveable { mutableStateOf("") }
    var sinProducto by remember { mutableStateOf<String?>(null) }
    var valorMin by rememberSaveable { mutableStateOf("") }
    var riesgoMin by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun segmento() = SegmentoCampana(
        distrito = distrito.trim().ifBlank { null },
        sinProducto = sinProducto,
        puntajeValorMin = valorMin.toIntOrNull(),
        riesgoMin = riesgoMin.toIntOrNull()?.let { it / 100.0 }
    )
    LaunchedEffect(Unit) { vm.limpiarVistaPrevia(); if (productos.isEmpty()) vm.refrescarCampanas() }

    FondoAndino {
        Column(Modifier.fillMaxSize().imePadding()) {
            BarraVidrio("Nueva campaña", "Segmenta, previsualiza y envía", onAtras = onAtras)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                vm.mensaje?.let { MensajeVidrio(it, vm.mensajeEsError, vm::cerrarMensaje) }
                TarjetaVidrio {
                    Text("1. Segmento de clientes", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(distrito, { distrito = it; vm.limpiarVistaPrevia() }, "Distrito (opcional, ej.: El Tambo)")
                    Spacer(Modifier.height(8.dp))
                    Text("Que NO hayan comprado (venta cruzada):", style = MaterialTheme.typography.labelMedium)
                    ChipsVidrio(listOf("Cualquiera") + productos.mapNotNull { it.codigo }, sinProducto ?: "Cualquiera",
                        { sinProducto = if (it == "Cualquiera") null else it; vm.limpiarVistaPrevia() })
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoVidrio(valorMin, { valorMin = it.filter { c -> c.isDigit() }.take(3); vm.limpiarVistaPrevia() }, "Valor mín. (0–100)",
                            Modifier.weight(1f), teclado = KeyboardOptions(keyboardType = KeyboardType.Number))
                        CampoVidrio(riesgoMin, { riesgoMin = it.filter { c -> c.isDigit() }.take(3); vm.limpiarVistaPrevia() }, "Riesgo mín. %",
                            Modifier.weight(1f), teclado = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Spacer(Modifier.height(10.dp))
                    BotonCapsulaSecundario("Ver tamaño del segmento", { vm.previsualizar(segmento()) }, Modifier.fillMaxWidth(), icono = Icons.Filled.Visibility, color = TurquesaLago)
                    vm.vistaPrevia?.let { Capsula(it, TurquesaLago, Modifier.padding(top = 8.dp), icono = Icons.Filled.Groups) }
                }
                TarjetaVidrio {
                    Text("2. Oferta", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(nombre, { nombre = it }, "Nombre interno (ej.: Kit sensores Q4)")
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(oferta, { oferta = it }, "Mensaje para el cliente", lineas = 3)
                    Spacer(Modifier.height(8.dp))
                    Text("Producto en oferta (opcional):", style = MaterialTheme.typography.labelMedium)
                    ChipsVidrio(listOf("Ninguno") + productos.map { it.nombre ?: "" }, productos.firstOrNull { it.id == productoOferta }?.nombre ?: "Ninguno",
                        { t -> productoOferta = productos.firstOrNull { it.nombre == t }?.id })
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(descuento, { descuento = it.take(5) }, "Descuento %", teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoVidrio(inicio, { inicio = it }, "Inicio (AAAA-MM-DD)", Modifier.weight(1f))
                        CampoVidrio(fin, { fin = it }, "Fin (AAAA-MM-DD)", Modifier.weight(1f))
                    }
                }
                TarjetaVidrio {
                    Text("3. Canales", style = MaterialTheme.typography.titleMedium)
                    Text("Push llega al Portal del cliente como notificación; los demás los ejecutan los vendedores.",
                        style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                    Spacer(Modifier.height(8.dp))
                    CANALES_CAMPANA.forEach { (codigo, texto) ->
                        val activo = codigo in canales
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(checked = activo, onCheckedChange = { canales = if (it) canales + codigo else canales - codigo })
                            Text(texto, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                error?.let { MensajeVidrio(it, esError = true) }
            }
            Row(Modifier.navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonCapsulaSecundario("Guardar", {
                    error = vm.crearCampana(nombre, oferta, productoOferta, descuento, canales, inicio, fin, segmento(), false, onAtras)
                }, Modifier.weight(1f), icono = Icons.Filled.Save)
                BotonCapsula("Enviar ahora", {
                    error = vm.crearCampana(nombre, oferta, productoOferta, descuento, canales, inicio, fin, segmento(), true, onAtras)
                }, Modifier.weight(1.3f), icono = Icons.Filled.Send, color = TurquesaLago, cargando = vm.cargando)
            }
        }
    }
}

// ============================================================ RESULTADOS DE UNA CAMPAÑA (RF-S08)
@Composable
fun ResultadosCampanaScreen(vm: GestionViewModel, id: Int, onAtras: () -> Unit) {
    val r by remember(id) { vm.resultados(id) }.collectAsState(initial = null)
    LaunchedEffect(id) { vm.refrescarResultados(id) }
    val c = r?.dato
    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(c?.nombre ?: "Campaña", c?.let { "${Formato.fecha(it.inicio)} → ${Formato.fecha(it.fin)}" }, onAtras = onAtras)
            if (c == null) {
                Box(Modifier.padding(16.dp)) { VacioVidrio(Icons.Filled.Campaign, "Cargando resultados…", null) }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        TarjetaVidrio {
                            Text(c.oferta ?: "", style = MaterialTheme.typography.bodyLarge)
                            c.producto?.let { FilaDato("Producto", "$it · −${c.descuentoPct} %") }
                            FilaDato("Creada por", c.creadaPor ?: "—")
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KpiVidrio(Icons.Filled.Groups, "${c.alcanzados ?: 0}", "Clientes alcanzados", AzulAndino, Modifier.weight(1f))
                            KpiVidrio(Icons.Filled.Notifications, Formato.porcentaje(c.tasaRespuesta), "Tasa de respuesta", TurquesaLago, Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KpiVidrio(Icons.Filled.Campaign, "${c.leadsGenerados ?: 0}", "Leads generados", DoradoInca, Modifier.weight(1f))
                            KpiVidrio(Icons.Filled.Send, Formato.porcentaje(c.conversion), "Conversión a venta", VerdeExito, Modifier.weight(1f))
                        }
                    }
                    val canales = r?.porCanal.orEmpty()
                    if (canales.isNotEmpty()) {
                        item { Seccion("Respuesta por canal") }
                        item {
                            TarjetaVidrio {
                                BarrasHorizontales(
                                    canales.map { "${CANALES_CAMPANA[it.canal] ?: it.canal} (${it.respuestas}/${it.enviados})" to (it.tasaRespuesta ?: 0.0) },
                                    TurquesaLago, { Formato.porcentaje(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================ MODELOS DE ML (Semana 14)
@Composable
fun ModelosScreen(vm: GestionViewModel, onAtras: () -> Unit) {
    val modelos by vm.modelos.collectAsState()
    LaunchedEffect(Unit) { vm.refrescarModelos() }
    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio("Modelos de ML", "Entrenados con el historial de ventas y servicio", onAtras = onAtras)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                vm.mensaje?.let { m -> item { MensajeVidrio(m, vm.mensajeEsError, vm::cerrarMensaje) } }
                if (modelos.isEmpty()) item { VacioVidrio(Icons.Filled.Psychology, "Cargando modelos…", null) }
                items(modelos, key = { it.nombre ?: "" }) { m ->
                    TarjetaVidrio {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconoCirculo(Icons.Filled.Psychology, TurquesaLago)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(nombreModelo(m.nombre), style = MaterialTheme.typography.titleMedium)
                                Text(m.algoritmo ?: "", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                            }
                            Capsula("v${m.version ?: 1}", AzulAndino)
                        }
                        Spacer(Modifier.height(8.dp))
                        m.metricas.orEmpty().forEach { (k, v) -> FilaDato(k, formatoMetrica(v)) }
                        FilaDato("Filas de entrenamiento", "${m.filas ?: 0}")
                        FilaDato("Entrenado", Formato.fechaHora(m.entrenadoEn))
                    }
                }
                item {
                    BotonCapsula("Reentrenar con los datos actuales", { vm.entrenar() }, Modifier.fillMaxWidth(), icono = Icons.Filled.Refresh,
                        color = TurquesaLago, cargando = vm.cargando)
                }
                item {
                    Text("El servidor también reentrena solo una vez al día. El puntaje de cada lead se recalcula al registrar interacciones.",
                        style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                }
            }
        }
    }
}

private fun nombreModelo(n: String?): String = when (n) {
    "LEAD_SCORING" -> "ML-01 · Probabilidad de cierre de leads"
    "CHURN" -> "ML-02 · Riesgo de cancelación"
    "RFM" -> "ML-03 · Valor del cliente (RFM + CLTV)"
    "APRIORI" -> "ML-04 · Recomendaciones (reglas de asociación)"
    else -> n ?: "Modelo"
}

private fun formatoMetrica(v: Any?): String = when (v) {
    null -> "—"
    is Double -> if (v % 1.0 == 0.0) "${v.toLong()}" else String.format(Locale.US, "%.3f", v)
    is Number -> v.toString()
    is List<*> -> "${v.size} elemento(s)"
    is Map<*, *> -> v.entries.take(3).joinToString { "${it.key}: ${it.value}" }
    else -> v.toString()
}

// ============================================================ SERVICIO (vista del supervisor)
@Composable
fun ServicioSupervisorScreen(vm: GestionViewModel, onAbrirTicket: (Int) -> Unit, onAtras: () -> Unit) {
    val tickets by vm.tickets.collectAsState()
    val analitica by vm.servicio.collectAsState()
    LaunchedEffect(Unit) { vm.refrescarTickets(false); vm.refrescarModelos() }
    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio("Servicio al cliente", "${tickets.size} solicitud(es) en curso", onAtras = onAtras,
                acciones = { BotonCircular(Icons.Filled.Refresh, "Actualizar", { vm.refrescarTickets(false) }) })
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val sla = analitica?.sla.orEmpty()
                if (sla.isNotEmpty()) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            sla.forEach { s ->
                                KpiVidrio(Icons.Filled.SupportAgent, Formato.porcentaje(s.cumplimiento),
                                    "SLA ${if (s.prioridad == "ALTA") "preferentes (4 h)" else "normal (24 h)"} · ${s.horasPromedio} h prom.",
                                    if ((s.cumplimiento ?: 0.0) >= 90) VerdeExito else DoradoInca, Modifier.weight(1f))
                            }
                        }
                    }
                }
                val calor = analitica?.porDiaHora.orEmpty()
                if (calor.isNotEmpty()) {
                    item { Seccion("¿Cuándo llegan las solicitudes? (último año)") }
                    item { TarjetaVidrio { MapaCalor(calor) } }
                }
                item { Seccion("Solicitudes abiertas") }
                if (tickets.isEmpty()) item { VacioVidrio(Icons.Filled.SupportAgent, "No hay solicitudes en curso", null) }
                items(tickets, key = { it.id }) { t -> TicketCard(t) { onAbrirTicket(t.id) } }
            }
        }
    }
}

// ============================================================ NOTIFICACIONES
@Composable
fun NotificacionesScreen(vm: GestionViewModel, onAtras: () -> Unit) {
    val lista by vm.notificaciones.collectAsState()
    LaunchedEffect(Unit) { vm.marcarLeidas() }
    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio("Notificaciones", "${lista.size} aviso(s)", onAtras = onAtras)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (lista.isEmpty()) item { VacioVidrio(Icons.Filled.Notifications, "No tienes notificaciones", null) }
                items(lista, key = { it.id }) { n ->
                    TarjetaVidrio(relleno = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconoCirculo(Icons.Filled.Notifications, if (n.leida == true) GrisNeutro else TurquesaLago, 36.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(n.titulo ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = if (n.leida == true) FontWeight.Normal else FontWeight.Bold)
                                Text(n.cuerpo ?: "", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Text(Formato.haceCuanto(n.creadoEn), style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("unused")
private val ROJO = RojoError
