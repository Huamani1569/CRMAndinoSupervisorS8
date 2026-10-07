package com.crmandino.supervisor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.supervisor.model.RolUsuario
import com.crmandino.supervisor.model.UsuarioSesion
import com.crmandino.supervisor.ui.components.AccesoRapido
import com.crmandino.supervisor.ui.components.BarraSuperior
import com.crmandino.supervisor.ui.components.EmbudoVentas
import com.crmandino.supervisor.ui.components.EstadoVacio
import com.crmandino.supervisor.ui.components.PanelSincronizacion
import com.crmandino.supervisor.ui.components.ResumenCard
import com.crmandino.supervisor.ui.components.TituloSeccion
import com.crmandino.supervisor.ui.components.formatearSoles
import com.crmandino.supervisor.ui.theme.DoradoInca
import com.crmandino.supervisor.ui.theme.NieveAndina
import com.crmandino.supervisor.ui.theme.RojoError
import com.crmandino.supervisor.ui.theme.TurquesaLago
import com.crmandino.supervisor.viewmodel.LeadViewModel
import com.crmandino.supervisor.viewmodel.SyncViewModel
import com.crmandino.supervisor.viewmodel.TareaViewModel
import com.crmandino.supervisor.viewmodel.TareasDeVendedor

// Panel del SUPERVISOR: sincronización, embudo con valor por etapa (RF06, Canvas)
// y tareas pendientes por vendedor (RF07).
@Composable
fun HomeScreen(
    usuario: UsuarioSesion,
    leadViewModel: LeadViewModel,
    tareaViewModel: TareaViewModel,
    syncViewModel: SyncViewModel,
    avisoSinConexion: String?,
    onDescartarAviso: () -> Unit,
    onVerLeads: (etapa: String?) -> Unit,
    onVerSincronizacion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val leads by leadViewModel.leads.collectAsState(initial = emptyList())
    val resumen by leadViewModel.resumenPorEtapa.collectAsState(initial = emptyList())
    val pendientes by tareaViewModel.pendientes.collectAsState(initial = emptyList())
    val porVendedor by tareaViewModel.pendientesPorVendedor.collectAsState(initial = emptyList())
    val conectado by syncViewModel.conectado.collectAsState()
    val opPendientes by syncViewModel.pendientes.collectAsState(initial = 0)
    val opErrores by syncViewModel.errores.collectAsState(initial = 0)
    val ultimaSync by syncViewModel.ultimaSincronizacion.collectAsState(initial = null)

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Panel comercial",
                subtitulo = "${usuario.nombre} · ${RolUsuario.ETIQUETA}",
                acciones = {
                    IconButton(onClick = onVerSincronizacion) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sincronización")
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (avisoSinConexion != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = DoradoInca.copy(alpha = 0.2f))) {
                        Column(Modifier.padding(12.dp)) {
                            Text(avisoSinConexion, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onDescartarAviso) { Text("Entendido") }
                        }
                    }
                }
            }

            // RF13 + RF14: estado de la sincronización y botón manual
            item {
                PanelSincronizacion(
                    conectado = conectado,
                    pendientes = opPendientes,
                    errores = opErrores,
                    ultimaSincronizacion = ultimaSync,
                    sincronizando = syncViewModel.sincronizando,
                    onSincronizar = syncViewModel::sincronizarAhora,
                    onVerDetalle = onVerSincronizacion
                )
            }
            syncViewModel.mensaje?.let { mensaje ->
                item {
                    Text(mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Indicadores del equipo
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ResumenCard(Icons.Filled.Groups, "${leads.size}", "Leads",
                        MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    ResumenCard(Icons.Filled.MonetizationOn, formatearSoles(resumen.sumOf { it.valor }),
                        "Valor total", TurquesaLago, Modifier.weight(1.5f))
                    ResumenCard(Icons.Filled.Alarm, "${pendientes.size}", "Pendientes",
                        DoradoInca, Modifier.weight(1f))
                }
            }

            item {
                AccesoRapido(Icons.Filled.FilterAlt, "Listado completo de leads", { onVerLeads(null) })
            }

            // RF06: valor potencial acumulado por etapa, dibujado como EMBUDO con Canvas
            item { TituloSeccion("Embudo de ventas · valor potencial por etapa") }
            item {
                if (leads.isEmpty()) {
                    EstadoVacio("Aún no hay datos descargados. Conéctate y pulsa \"Sincronizar\".")
                } else {
                    EmbudoVentas(resumen, onElegirEtapa = { onVerLeads(it) })
                }
            }

            // RF07: tareas pendientes por vendedor
            item { TituloSeccion("Tareas pendientes por vendedor") }
            if (porVendedor.isEmpty()) {
                item { EstadoVacio("El equipo no tiene tareas pendientes") }
            } else {
                items(porVendedor, key = { it.vendedor }) { TareasVendedorCard(it) }
            }
        }
    }
}

@Composable
private fun TareasVendedorCard(grupo: TareasDeVendedor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(grupo.vendedor, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    "${grupo.tareas.size} pendientes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DoradoInca
                )
            }
            grupo.tareas.forEach { tarea ->
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = NieveAndina)
                Row {
                    Column(Modifier.weight(1f)) {
                        Text(tarea.descripcion, style = MaterialTheme.typography.bodyLarge)
                        Text(tarea.leadEmpresa, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Vence\n${tarea.fechaLimite}", style = MaterialTheme.typography.labelMedium,
                        color = RojoError)
                }
            }
        }
    }
}
