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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crmandino.supervisor.data.remote.dto.CampanaDto
import com.crmandino.supervisor.data.remote.dto.ClienteRiesgoDto
import com.crmandino.supervisor.data.remote.dto.CotizacionDto
import com.crmandino.supervisor.data.remote.dto.LeadEquipoDto
import com.crmandino.supervisor.ui.components.BarraProgreso
import com.crmandino.supervisor.ui.components.BarrasHorizontales
import com.crmandino.supervisor.ui.components.BotonCapsula
import com.crmandino.supervisor.ui.components.BotonCapsulaSecundario
import com.crmandino.supervisor.ui.components.CampoVidrio
import com.crmandino.supervisor.ui.components.Capsula
import com.crmandino.supervisor.ui.components.ChipsVidrio
import com.crmandino.supervisor.ui.components.EtapaChip
import com.crmandino.supervisor.ui.components.FilaDato
import com.crmandino.supervisor.ui.components.IconoCirculo
import com.crmandino.supervisor.ui.components.PuntajeCirculo
import com.crmandino.supervisor.ui.components.Seccion
import com.crmandino.supervisor.ui.components.SegmentadoVidrio
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

private val RELLENO_LISTA = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp)

// ============================================================ EQUIPO: aprobaciones (RN-03) y reasignación en lote (RF-S03)
@Composable
fun EquipoTab(vm: GestionViewModel) {
    val porAprobar by vm.porAprobar.collectAsState()
    val leads by vm.leadsEquipo.collectAsState()
    val vendedores by vm.vendedores.collectAsState()
    var sub by rememberSaveable { mutableIntStateOf(0) }
    var observando by remember { mutableStateOf<CotizacionDto?>(null) }
    val seleccion = remember { mutableStateOf(setOf<Int>()) }
    var destino by remember { mutableStateOf<Int?>(null) }
    var filtroVendedor by rememberSaveable { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = RELLENO_LISTA, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { SegmentadoVidrio(listOf("Por aprobar (${porAprobar.size})", "Reasignar leads"), sub, { sub = it }) }
            if (sub == 0) {
                if (porAprobar.isEmpty()) item { VacioVidrio(Icons.Filled.CheckCircle, "No hay descuentos por aprobar", "Las cotizaciones con más de 10 % aparecen aquí.") }
                items(porAprobar, key = { it.id }) { c ->
                    AprobacionCard(c, onAprobar = { vm.aprobar(c) }, onObservar = { observando = c })
                }
            } else {
                // Carga de cada vendedor (para elegir el destino)
                item {
                    TarjetaVidrio {
                        Text("1. Vendedor destino", style = MaterialTheme.typography.titleMedium)
                        Text("Entre paréntesis: leads abiertos que ya tiene", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                        Spacer(Modifier.height(6.dp))
                        ChipsVidrio(vendedores.map { "${it.nombre} (${it.leadsAbiertos ?: 0})" },
                            vendedores.firstOrNull { it.id == destino }?.let { "${it.nombre} (${it.leadsAbiertos ?: 0})" },
                            { t -> destino = vendedores.firstOrNull { "${it.nombre} (${it.leadsAbiertos ?: 0})" == t }?.id })
                    }
                }
                item {
                    Text("2. Elige los leads (filtra por vendedor actual)", style = MaterialTheme.typography.titleMedium)
                    ChipsVidrio(listOf("Todos") + leads.mapNotNull { it.vendedorNombre }.distinct(), filtroVendedor ?: "Todos",
                        { filtroVendedor = if (it == "Todos") null else it }, Modifier.padding(top = 6.dp))
                }
                val visibles = leads.filter { filtroVendedor == null || it.vendedorNombre == filtroVendedor }
                if (visibles.isEmpty()) item { VacioVidrio(Icons.Filled.SwapHoriz, "No hay leads abiertos", null) }
                items(visibles, key = { "l" + it.id }) { l ->
                    LeadSeleccionable(l, l.id in seleccion.value) { marcado ->
                        seleccion.value = if (marcado) seleccion.value + l.id else seleccion.value - l.id
                    }
                }
            }
        }
        if (sub == 1 && seleccion.value.isNotEmpty()) {
            Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 92.dp)) {
                BotonCapsula(
                    "Reasignar ${seleccion.value.size} lead(s)",
                    { vm.reasignar(seleccion.value, destino) { seleccion.value = emptySet() } },
                    Modifier.fillMaxWidth(), icono = Icons.Filled.SwapHoriz, color = TurquesaLago, cargando = vm.cargando
                )
            }
        }
    }

    observando?.let { c ->
        var obs by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { observando = null },
            title = { Text("Observar ${c.numero}") },
            text = {
                Column {
                    Text("La cotización vuelve al vendedor como borrador con tu comentario.", color = TextoSecundario)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(obs, { obs = it }, "Observación (ej.: máximo 12 %)", lineas = 2)
                }
            },
            confirmButton = { TextButton(onClick = { vm.observar(c, obs); observando = null }) { Text("Devolver", color = RojoError, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { observando = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun AprobacionCard(c: CotizacionDto, onAprobar: () -> Unit, onObservar: () -> Unit) {
    TarjetaVidrio(tinte = DoradoInca) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoCirculo(Icons.Filled.Percent, DoradoInca)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${c.numero} · ${c.empresa}", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Vendedor: ${c.vendedorNombre}", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
            Capsula("−${c.descuentoPct} %", RojoError)
        }
        Spacer(Modifier.height(8.dp))
        c.items.orEmpty().forEach { Text("${it.cantidad} × ${it.nombre} · ${Formato.soles(it.subtotal, false)}", style = MaterialTheme.typography.bodyMedium) }
        Spacer(Modifier.height(6.dp))
        FilaDato("Subtotal", Formato.soles(c.subtotal))
        FilaDato("Descuento", "− " + Formato.soles((c.subtotal ?: 0.0) - (c.total ?: 0.0)))
        FilaDato("Total", Formato.soles(c.total), destacado = true)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotonCapsulaSecundario("Observar", onObservar, Modifier.weight(1f), icono = Icons.Filled.Cancel, color = RojoError)
            BotonCapsula("Aprobar y enviar", onAprobar, Modifier.weight(1.4f), icono = Icons.Filled.CheckCircle, color = VerdeExito)
        }
    }
}

@Composable
private fun LeadSeleccionable(l: LeadEquipoDto, marcado: Boolean, onMarcar: (Boolean) -> Unit) {
    TarjetaVidrio(relleno = 10.dp, tinte = if (marcado) TurquesaLago else null, onClick = { onMarcar(!marcado) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = marcado, onCheckedChange = onMarcar, colors = CheckboxDefaults.colors(checkedColor = TurquesaLago))
            PuntajeCirculo(l.puntaje, tamano = 38.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(l.empresa ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${l.vendedorNombre} · ${l.diasSinContacto ?: 0} día(s) sin contacto", style = MaterialTheme.typography.labelMedium,
                    color = if ((l.diasSinContacto ?: 0) >= 7) RojoError else TextoSecundario)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Formato.solesCorto(l.valorPotencial), fontWeight = FontWeight.Bold, color = AzulAndino)
                l.etapa?.let { EtapaChip(it) }
            }
        }
    }
}

// ============================================================ CAMPAÑAS (HU-S04)
@Composable
fun CampanasTab(vm: GestionViewModel, onNueva: () -> Unit, onResultados: (Int) -> Unit) {
    val campanas by vm.campanas.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = RELLENO_LISTA, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BotonCapsula("Nueva campaña", onNueva, Modifier.fillMaxWidth(), icono = Icons.Filled.Add, color = TurquesaLago) }
        if (campanas.isEmpty()) item { VacioVidrio(Icons.Filled.Campaign, "Sin campañas", "Crea una para un segmento de clientes.") }
        items(campanas, key = { it.id }) { c -> CampanaCard(c, onEnviar = { vm.enviarCampana(c.id) }, onVer = { onResultados(c.id) }) }
    }
}

@Composable
private fun CampanaCard(c: CampanaDto, onEnviar: () -> Unit, onVer: () -> Unit) {
    val borrador = c.estado == "BORRADOR"
    TarjetaVidrio(onClick = onVer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoCirculo(Icons.Filled.Campaign, if (borrador) GrisNeutro else TurquesaLago)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(c.nombre ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${Formato.fecha(c.inicio)} → ${Formato.fecha(c.fin)}", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
            Capsula(if (borrador) "Borrador" else "Enviada", if (borrador) GrisNeutro else VerdeExito)
        }
        Text(c.oferta ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
        Text(c.canales.orEmpty().joinToString(" · ") { CANALES_CAMPANA[it] ?: it }, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
        if (!borrador) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Metrica("${c.alcanzados ?: 0}", "clientes")
                Metrica(Formato.porcentaje(c.tasaRespuesta), "respuesta")
                Metrica("${c.leadsGenerados ?: 0}", "leads")
                Metrica(Formato.porcentaje(c.conversion), "conversión")
            }
        } else {
            Spacer(Modifier.height(8.dp))
            BotonCapsula("Enviar ahora", onEnviar, Modifier.fillMaxWidth(), icono = Icons.Filled.Send)
        }
    }
}

@Composable
private fun Metrica(valor: String, etiqueta: String) {
    Column {
        Text(valor, style = MaterialTheme.typography.titleMedium, color = AzulAndino, fontWeight = FontWeight.Bold)
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
    }
}

// ============================================================ CLIENTES: riesgo (ML-02) y rentabilidad (ML-03)
@Composable
fun ClientesTab(vm: GestionViewModel) {
    val riesgo by vm.riesgo.collectAsState()
    val rentables by vm.rentables.collectAsState()
    var sub by rememberSaveable { mutableIntStateOf(0) }
    var reteniendo by remember { mutableStateOf<ClienteRiesgoDto?>(null) }
    val lista = riesgo?.datos.orEmpty()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = RELLENO_LISTA, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SegmentadoVidrio(listOf("En riesgo (${lista.size})", "Más rentables"), sub, { sub = it }) }
        if (sub == 0) {
            item {
                Text("Probabilidad de cancelar en los próximos 90 días según el modelo ML-02 (regresión logística). Se muestran los de 50 % o más.",
                    style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
            if (lista.isEmpty()) item { VacioVidrio(Icons.Filled.Shield, "No hay clientes en riesgo alto", null) }
            items(lista, key = { it.clienteId }) { c -> RiesgoCard(c, onRetener = { reteniendo = c }) }
        } else {
            val top = rentables?.top.orEmpty()
            val segmentos = rentables?.segmentos.orEmpty()
            if (top.isEmpty()) item { VacioVidrio(Icons.Filled.Star, "Cargando clientes rentables…", null) }
            items(top.size) { i ->
                val c = top[i]
                TarjetaVidrio(relleno = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}", style = MaterialTheme.typography.titleLarge, color = DoradoInca, modifier = Modifier.width(28.dp))
                        PuntajeCirculo(c.puntajeValor, tamano = 38.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.razonSocial ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${c.distrito ?: "—"} · CLTV ${Formato.solesCorto(c.cltv)}", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(Formato.solesCorto(c.margen), fontWeight = FontWeight.Bold, color = VerdeExito)
                            Text("margen", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                        }
                    }
                }
            }
            if (segmentos.isNotEmpty()) {
                item { Seccion("Margen por distrito") }
                item {
                    TarjetaVidrio {
                        BarrasHorizontales(segmentos.map { "${it.segmento} (${it.clientes})" to (it.margen ?: 0.0) }, VerdeExito, { Formato.solesCorto(it) })
                    }
                }
            }
        }
    }

    reteniendo?.let { c ->
        var texto by remember { mutableStateOf("Retención: visitar a ${c.razonSocial} y ofrecer una solución") }
        var dias by remember { mutableIntStateOf(3) }
        AlertDialog(
            onDismissRequest = { reteniendo = null },
            title = { Text("Tarea de retención") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se asigna a ${c.vendedor ?: "su vendedor"} y le llega una notificación.", color = TextoSecundario)
                    CampoVidrio(texto, { texto = it }, "Qué debe hacer", lineas = 2)
                    Text("Plazo", style = MaterialTheme.typography.labelMedium)
                    ChipsVidrio(listOf("3 días", "7 días", "14 días"), "$dias días", { dias = it.substringBefore(" ").toInt() })
                }
            },
            confirmButton = { TextButton(onClick = { vm.crearRetencion(c, texto, dias); reteniendo = null }, enabled = texto.isNotBlank()) { Text("Crear tarea", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { reteniendo = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun RiesgoCard(c: ClienteRiesgoDto, onRetener: () -> Unit) {
    val prob = c.probCancelacion ?: 0.0
    val color = if (prob >= 0.75) RojoError else if (prob >= 0.6) DoradoInca else GrisNeutro
    TarjetaVidrio(tinte = color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.razonSocial ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Vendedor: ${c.vendedor ?: "—"} · última compra ${Formato.fecha(c.ultimaCompra)}", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${(prob * 100).toInt()} %", style = MaterialTheme.typography.titleLarge, color = color)
                Text("riesgo", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
        }
        BarraProgreso(prob.toFloat(), color, Modifier.padding(vertical = 6.dp))
        FilaDato("Valor de vida (CLTV)", Formato.soles(c.cltv, false), destacado = true)
        c.factores.orEmpty().forEach { f ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(Icons.Filled.Warning, contentDescription = null, tint = color, modifier = Modifier.padding(end = 6.dp))
                Text(f.factor ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (c.tieneTareaRetencion == true) {
            Capsula("Ya tiene una tarea de retención en curso", VerdeExito, icono = Icons.Filled.CheckCircle)
        } else {
            BotonCapsula("Crear tarea de retención", onRetener, Modifier.fillMaxWidth(), icono = Icons.Filled.AssignmentInd, color = color)
        }
    }
}
