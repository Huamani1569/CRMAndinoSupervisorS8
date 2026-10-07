package com.crmandino.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import com.crmandino.supervisor.data.repository.TareaRepository
import com.crmandino.supervisor.model.Tarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Tareas pendientes de un vendedor.
data class TareasDeVendedor(val vendedor: String, val tareas: List<Tarea>)

// RF07: consulta de tareas pendientes agrupadas por vendedor (solo lectura).
class TareaViewModel(tareaRepository: TareaRepository) : ViewModel() {

    val pendientes: Flow<List<Tarea>> = tareaRepository.observarPendientes()

    val pendientesPorVendedor: Flow<List<TareasDeVendedor>> = pendientes.map { tareas ->
        tareas
            .groupBy { it.vendedorNombre.ifBlank { "Sin vendedor" } }
            .map { (vendedor, lista) -> TareasDeVendedor(vendedor, lista) }
            .sortedByDescending { it.tareas.size }
    }
}
