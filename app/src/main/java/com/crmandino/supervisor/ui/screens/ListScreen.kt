package com.crmandino.supervisor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crmandino.supervisor.model.EtapaVenta
import com.crmandino.supervisor.model.Lead
import com.crmandino.supervisor.model.UsuarioSesion
import com.crmandino.supervisor.ui.components.BarraSuperior
import com.crmandino.supervisor.ui.components.EstadoVacio
import com.crmandino.supervisor.ui.components.FiltroEtapas
import com.crmandino.supervisor.ui.components.LeadCard
import com.crmandino.supervisor.ui.components.SelectorChips
import com.crmandino.supervisor.ui.components.formatearSoles
import com.crmandino.supervisor.viewmodel.LeadViewModel

// RF09: listado completo por etapa (leído desde Room). Cada lead se puede editar/reasignar (RF08)
// o eliminar (RF10); los cambios quedan en cola y se sincronizan con la API.
@Composable
fun ListScreen(
    leadViewModel: LeadViewModel,
    etapaFiltro: String?,
    onCambiarFiltro: (String?) -> Unit,
    onAtras: () -> Unit
) {
    val leads by remember(etapaFiltro) { leadViewModel.leadsPorEtapa(etapaFiltro) }
        .collectAsState(initial = emptyList())
    val vendedores by leadViewModel.vendedores.collectAsState(initial = emptyList())

    var leadEditando by remember { mutableStateOf<Lead?>(null) }
    var leadEliminando by remember { mutableStateOf<Lead?>(null) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Leads del equipo",
                subtitulo = "${leads.size} leads · ${formatearSoles(leads.sumOf { it.valorPotencial })}",
                onAtras = onAtras
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
            item {
                FiltroEtapas(
                    etapas = EtapaVenta.etiquetas,
                    seleccionada = etapaFiltro,
                    onSeleccionar = onCambiarFiltro
                )
            }
            if (leads.isEmpty()) {
                item { EstadoVacio("No hay leads en esta etapa (o aún no se descargaron datos)") }
            } else {
                items(leads, key = { it.id }) { lead ->
                    LeadCard(lead) {
                        IconButton(onClick = {
                            leadViewModel.limpiarError()
                            leadEditando = lead
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar o reasignar")
                        }
                        IconButton(onClick = { leadEliminando = lead }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    leadEditando?.let { lead ->
        EditarLeadDialog(
            lead = lead,
            vendedores = vendedores,
            error = leadViewModel.errorEdicion,
            onGuardar = { empresa, valor, vendedor, etapa ->
                leadViewModel.editarLead(lead, empresa, valor, vendedor, etapa, onListo = { leadEditando = null })
            },
            onCancelar = { leadEditando = null }
        )
    }

    // RF10: confirmación antes de eliminar
    leadEliminando?.let { lead ->
        AlertDialog(
            onDismissRequest = { leadEliminando = null },
            title = { Text("Eliminar lead") },
            text = {
                Text("¿Eliminar \"${lead.empresa}\" como lead descartado? También se eliminarán sus tareas. " +
                    "Si no hay conexión, se eliminará en el servidor al sincronizar.")
            },
            confirmButton = {
                TextButton(onClick = {
                    leadViewModel.eliminarLead(lead)
                    leadEliminando = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { leadEliminando = null }) { Text("Cancelar") } }
        )
    }
}

// RF08: editar o reasignar un lead (empresa, valor, vendedor y etapa).
@Composable
private fun EditarLeadDialog(
    lead: Lead,
    vendedores: List<UsuarioSesion>,
    error: String?,
    onGuardar: (empresa: String, valor: String, vendedor: UsuarioSesion?, etapa: String) -> Unit,
    onCancelar: () -> Unit
) {
    var empresa by remember { mutableStateOf(lead.empresa) }
    var valor by remember {
        mutableStateOf(if (lead.valorPotencial % 1.0 == 0.0) "${lead.valorPotencial.toLong()}" else "${lead.valorPotencial}")
    }
    var vendedorNombre by remember { mutableStateOf(lead.vendedorNombre) }
    var etapa by remember { mutableStateOf(lead.etapa) }

    // Si la lista de vendedores aún no se descargó, al menos aparece el vendedor actual
    val opciones = vendedores.ifEmpty { listOf(UsuarioSesion(lead.vendedorId, lead.vendedorNombre, "", "cliente")) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar / reasignar lead") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = empresa, onValueChange = { empresa = it },
                    label = { Text("Empresa") }, singleLine = true)
                OutlinedTextField(value = valor, onValueChange = { valor = it },
                    label = { Text("Valor potencial (S/)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Text("Vendedor", style = MaterialTheme.typography.labelMedium)
                SelectorChips(
                    opciones = opciones.map { it.nombre },
                    seleccionada = vendedorNombre,
                    onSeleccionar = { vendedorNombre = it }
                )
                Text("Etapa", style = MaterialTheme.typography.labelMedium)
                SelectorChips(opciones = EtapaVenta.etiquetas, seleccionada = etapa, onSeleccionar = { etapa = it })
                // Datos de sincronización (evidencia: id local, id remoto, uuid y versión)
                Text(
                    "ID local ${lead.id} · ID remoto ${lead.remoteId ?: "—"} · versión ${lead.version} · ${lead.estadoSync.etiqueta}\nUUID ${lead.uuid}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                lead.mensajeError?.let { Text("Motivo: $it", color = MaterialTheme.colorScheme.error) }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(empresa, valor, opciones.find { it.nombre == vendedorNombre }, etapa)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
