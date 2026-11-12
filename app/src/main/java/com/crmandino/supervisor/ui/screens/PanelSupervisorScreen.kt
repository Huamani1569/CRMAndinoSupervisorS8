package com.crmandino.supervisor.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.supervisor.model.ResumenEtapa
import com.crmandino.supervisor.model.UsuarioSesion
import com.crmandino.supervisor.ui.components.BarraNavegacionVidrio
import com.crmandino.supervisor.ui.components.BarraProgreso
import com.crmandino.supervisor.ui.components.BarraVidrio
import com.crmandino.supervisor.ui.components.BarrasHorizontales
import com.crmandino.supervisor.ui.components.BotonCircular
import com.crmandino.supervisor.ui.components.EmbudoVentas
import com.crmandino.supervisor.ui.components.FondoAndino
import com.crmandino.supervisor.ui.components.GraficoVentas
import com.crmandino.supervisor.ui.components.IconoCirculo
import com.crmandino.supervisor.ui.components.ItemNavegacion
import com.crmandino.supervisor.ui.components.KpiVidrio
import com.crmandino.supervisor.ui.components.MensajeVidrio
import com.crmandino.supervisor.ui.components.Seccion
import com.crmandino.supervisor.ui.components.TarjetaVidrio
import com.crmandino.supervisor.ui.components.VacioVidrio
import com.crmandino.supervisor.ui.theme.AzulAndino
import com.crmandino.supervisor.ui.theme.DoradoInca
import com.crmandino.supervisor.ui.theme.RojoError
import com.crmandino.supervisor.ui.theme.TextoSecundario
import com.crmandino.supervisor.ui.theme.TurquesaLago
import com.crmandino.supervisor.ui.theme.VerdeExito
import com.crmandino.supervisor.util.Formato
import com.crmandino.supervisor.viewmodel.GestionViewModel

// App SUPERVISOR (Proyecto Final): barra inferior de vidrio con Panel · Equipo · Campañas · Clientes · Más
@Composable
fun SupervisorPrincipalScreen(
    usuario: UsuarioSesion,
    vm: GestionViewModel,
    pestana: Int,
    onPestana: (Int) -> Unit,
    onNuevaCampana: () -> Unit,
    onResultadosCampana: (Int) -> Unit,
    onServicio: () -> Unit,
    onModelos: () -> Unit,
    onPanelParcial: () -> Unit,
    onLeadsParcial: () -> Unit,
    onSincronizacion: () -> Unit,
    onNotificaciones: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val noLeidas by vm.noLeidas.collectAsState()
    val porAprobar by vm.porAprobar.collectAsState()
    val riesgo by vm.riesgo.collectAsState()
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        vm.iniciarSondeo(false)
    }
    LaunchedEffect(pestana) {
        when (pestana) {
            0 -> vm.refrescarPanel()
            1 -> vm.refrescarEquipo()
            2 -> vm.refrescarCampanas()
            3 -> vm.refrescarClientes()
        }
    }
    val titulos = listOf("Panel comercial", "Mi equipo", "Campañas", "Clientes", "Más opciones")
    val subtitulos = listOf("CRM analítico · ${Formato.fecha(Formato.hoyIso())}", "Aprobaciones y reasignación", "Marketing segmentado", "Riesgo y rentabilidad (ML)", usuario.nombre)

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(
                titulo = titulos[pestana],
                subtitulo = subtitulos[pestana],
                acciones = {
                    if (vm.cargando) CircularProgressIndicator(Modifier.padding(10.dp).size(22.dp), strokeWidth = 2.dp, color = TurquesaLago)
                    else BotonCircular(Icons.Filled.Refresh, "Actualizar", {
                        when (pestana) { 0 -> vm.refrescarPanel(); 1 -> vm.refrescarEquipo(); 2 -> vm.refrescarCampanas(); 3 -> vm.refrescarClientes() }
                    })
                    BotonCircular(Icons.Filled.Notifications, "Notificaciones", onNotificaciones, insignia = noLeidas)
                }
            )
            vm.mensaje?.let { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { MensajeVidrio(it, vm.mensajeEsError, vm::cerrarMensaje) } }
            when (pestana) {
                1 -> EquipoTab(vm)
                2 -> CampanasTab(vm, onNuevaCampana, onResultadosCampana)
                3 -> ClientesTab(vm)
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (pestana == 0) panel(vm, { onPestana(1) }, { onPestana(3) })
                    else mas(usuario, onServicio, onModelos, onPanelParcial, onLeadsParcial, onSincronizacion, onCerrarSesion)
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter)) {
            BarraNavegacionVidrio(
                items = listOf(
                    ItemNavegacion("Panel", Icons.Filled.Dashboard),
                    ItemNavegacion("Equipo", Icons.Filled.Groups, porAprobar.size),
                    ItemNavegacion("Campañas", Icons.Filled.Campaign),
                    ItemNavegacion("Clientes", Icons.Filled.Store, riesgo?.datos?.count { it.tieneTareaRetencion != true && (it.probCancelacion ?: 0.0) >= 0.6 } ?: 0),
                    ItemNavegacion("Más", Icons.Filled.MoreHoriz)
                ),
                seleccionado = pestana,
                onSeleccionar = onPestana
            )
        }
    }
}

// ============================================================ PANEL (HU-S01, HU-S05)
private fun LazyListScope.panel(vm: GestionViewModel, onAprobaciones: () -> Unit, onRiesgo: () -> Unit) {
    item { PanelContenido(vm, onAprobaciones, onRiesgo) }
}

@Composable
private fun PanelContenido(vm: GestionViewModel, onAprobaciones: () -> Unit, onRiesgo: () -> Unit) {
    val resumen by vm.resumen.collectAsState()
    val embudo by vm.embudo.collectAsState()
    val pronostico by vm.pronostico.collectAsState()
    val ventas by vm.ventasMensuales.collectAsState()
    val k = resumen?.kpis
    val p = resumen?.pronostico

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (resumen == null) VacioVidrio(Icons.Filled.Insights, "Cargando indicadores…", "Necesitas conexión la primera vez.")

        // Pronóstico del mes vs meta (RF-S02)
        if (p != null) {
            TarjetaVidrio(tinte = TurquesaLago) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoCirculo(Icons.Filled.AutoGraph, TurquesaLago, 46.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pronóstico de ventas del mes", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                        Text(Formato.soles(p.pronostico, false), style = MaterialTheme.typography.headlineLarge, color = AzulAndino)
                    }
                }
                val meta = p.meta
                if (meta != null && meta > 0) {
                    val avance = ((p.pronostico ?: 0.0) / meta).toFloat()
                    Spacer(Modifier.height(8.dp))
                    BarraProgreso(avance, if (avance >= 1f) VerdeExito else DoradoInca)
                    Text("${(avance * 100).toInt()} % de la meta (${Formato.soles(meta, false)})", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                }
                Spacer(Modifier.height(6.dp))
                Text("Ya vendido: ${Formato.soles(p.ganadoReal, false)} · esperado de leads abiertos: ${Formato.soles(p.esperadoAbiertos, false)}",
                    style = MaterialTheme.typography.bodyMedium)
                Text("Pronóstico = vendido + Σ (valor × probabilidad de cierre del modelo ML-01)", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
        }

        // Alertas accionables
        val porAprobar = k?.cotizacionesPorAprobar ?: 0
        val enRiesgo = k?.clientesEnRiesgo ?: 0
        if (porAprobar > 0) Alerta(Icons.Filled.Percent, "$porAprobar descuento(s) por aprobar", "Los vendedores esperan tu respuesta", DoradoInca, onAprobaciones)
        if (enRiesgo > 0) Alerta(Icons.Filled.Warning, "$enRiesgo cliente(s) con riesgo alto de cancelar", "Asigna tareas de retención", RojoError, onRiesgo)

        // KPIs
        if (k != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiVidrio(Icons.AutoMirrored.Filled.TrendingUp, Formato.porcentaje(k.tasaConversion), "Conversión (180 d)", TurquesaLago, Modifier.weight(1f))
                KpiVidrio(Icons.Filled.ViewList, "${k.leadsAbiertos ?: 0}", "Leads abiertos", AzulAndino, Modifier.weight(1f))
                KpiVidrio(Icons.Filled.Store, Formato.solesCorto(k.cltvPromedio), "CLTV promedio", DoradoInca, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiVidrio(Icons.Filled.SentimentSatisfied, k.csat?.let { "$it ★" } ?: "—", "Satisfacción (CSAT)", VerdeExito, Modifier.weight(1f))
                KpiVidrio(Icons.Filled.Timer, k.tiempoMedioResolucionH?.let { "$it h" } ?: "—", "Resolución promedio", AzulAndino, Modifier.weight(1f))
                KpiVidrio(Icons.Filled.Warning, Formato.porcentaje(k.tasaCancelacion), "Cancelación anual", RojoError, Modifier.weight(1f))
            }
        }

        // Ventas por mes y pronóstico
        val serie = ventas?.serie.orEmpty()
        if (serie.isNotEmpty()) {
            Seccion("Ventas por mes")
            TarjetaVidrio { GraficoVentas(serie, ventas?.meta) }
        }

        // Embudo (RF-S01)
        val etapas = embudo?.etapas.orEmpty()
        if (etapas.isNotEmpty()) {
            Seccion("Embudo del equipo")
            EmbudoVentas(etapas.filter { it.etapa != "PERDIDO" }.map { ResumenEtapa(it.etapa, it.cantidad ?: 0, it.valor ?: 0.0) })
            val perdidos = etapas.firstOrNull { it.etapa == "PERDIDO" }?.cantidad ?: 0
            Text("Perdidos: $perdidos lead(s)", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
        }

        // Pronóstico por vendedor
        val porVendedor = pronostico?.porVendedor.orEmpty()
        if (porVendedor.isNotEmpty()) {
            Seccion("Pronóstico por vendedor")
            TarjetaVidrio {
                BarrasHorizontales(porVendedor.map { (it.vendedor ?: "—") to (it.pronostico ?: 0.0) }, TurquesaLago, { Formato.solesCorto(it) })
            }
        }
    }
}

@Composable
private fun Alerta(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, detalle: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    TarjetaVidrio(tinte = color, onClick = onClick, relleno = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoCirculo(icono, color)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(detalle, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
            }
        }
    }
}

// ============================================================ MÁS
private fun LazyListScope.mas(
    usuario: UsuarioSesion,
    onServicio: () -> Unit,
    onModelos: () -> Unit,
    onPanelParcial: () -> Unit,
    onLeadsParcial: () -> Unit,
    onSincronizacion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val opciones = listOf(
        Triple(Icons.Filled.SupportAgent, "Servicio al cliente", "Tickets, cumplimiento de SLA y demanda por hora") to onServicio,
        Triple(Icons.Filled.Psychology, "Modelos de Machine Learning", "Métricas de los 4 modelos y reentrenamiento") to onModelos,
        Triple(Icons.Filled.Dashboard, "Panel de leads (parcial)", "Valor por etapa y tareas por vendedor desde Room") to onPanelParcial,
        Triple(Icons.Filled.ViewList, "Todos los leads (parcial)", "Editar, reasignar o descartar con cola offline") to onLeadsParcial,
        Triple(Icons.Filled.Sync, "Sincronización", "Cola local ⇄ API REST, conflictos y duplicados") to onSincronizacion,
        Triple(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", usuario.correo) to onCerrarSesion
    )
    items(opciones.size) { i ->
        val (datos, accion) = opciones[i]
        TarjetaVidrio(onClick = accion, relleno = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCirculo(datos.first, if (i == opciones.lastIndex) RojoError else AzulAndino)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(datos.second, style = MaterialTheme.typography.titleMedium)
                    Text(datos.third, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                }
            }
        }
    }
    item {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            androidx.compose.material3.Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TurquesaLago)
            Text("  CRM Andino Perú · Proyecto Final", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
        }
    }
}
