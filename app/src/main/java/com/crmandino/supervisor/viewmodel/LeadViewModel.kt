package com.crmandino.supervisor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.supervisor.data.repository.LeadRepository
import com.crmandino.supervisor.model.EtapaVenta
import com.crmandino.supervisor.model.Lead
import com.crmandino.supervisor.model.ResumenEtapa
import com.crmandino.supervisor.model.UsuarioSesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Gestión de leads del SUPERVISOR: valor por etapa, listado, edición/reasignación y eliminación.
// Valida antes de guardar y llama al Repository (nunca al DAO ni a Retrofit directamente).
class LeadViewModel(private val leadRepository: LeadRepository) : ViewModel() {

    val leads: Flow<List<Lead>> = leadRepository.observarLeads()

    // RF06: valor potencial acumulado por etapa (se muestran las 5 etapas, aunque estén vacías).
    val resumenPorEtapa: Flow<List<ResumenEtapa>> = leads.map { lista ->
        EtapaVenta.etiquetas.map { etapa ->
            val deLaEtapa = lista.filter { it.etapa == etapa }
            ResumenEtapa(etapa, deLaEtapa.size, deLaEtapa.sumOf { it.valorPotencial })
        }
    }

    // Vendedores a los que se puede reasignar un lead (descargados de la API).
    val vendedores: Flow<List<UsuarioSesion>> = leadRepository.observarVendedores()

    var errorEdicion by mutableStateOf<String?>(null)
        private set

    // RF09: listado completo filtrado por etapa (null = todas).
    fun leadsPorEtapa(etapa: String?): Flow<List<Lead>> = leadRepository.observarPorEtapa(etapa)

    // RF08: editar o reasignar (empresa, valor, vendedor y etapa), con validaciones.
    fun editarLead(
        lead: Lead, empresa: String, valorTexto: String, vendedor: UsuarioSesion?, etapa: String,
        onListo: () -> Unit
    ) {
        val valor = valorTexto.trim().replace(",", "").toDoubleOrNull()
        errorEdicion = when {
            empresa.isBlank() -> "La empresa no puede estar vacía"
            valor == null -> "Ingresa un valor potencial válido"
            valor < 0 -> "El valor potencial no puede ser negativo"
            vendedor == null -> "Elige un vendedor"
            else -> null
        }
        if (errorEdicion != null || valor == null || vendedor == null) return

        viewModelScope.launch {
            leadRepository.editarLead(lead.id, empresa.trim(), valor, etapa, vendedor.id, vendedor.nombre)
            onListo()
        }
    }

    // RF10: eliminar un lead descartado (y sus tareas).
    fun eliminarLead(lead: Lead) {
        viewModelScope.launch { leadRepository.eliminarLead(lead.id) }
    }

    fun limpiarError() {
        errorEdicion = null
    }
}
