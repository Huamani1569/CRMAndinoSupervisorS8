package com.crmandino.supervisor.data.repository

import com.crmandino.supervisor.data.local.AppDatabase
import com.crmandino.supervisor.data.mapper.aModelo
import com.crmandino.supervisor.model.Tarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Tareas del equipo: el supervisor SOLO las consulta (rol de solo lectura para tareas).
class TareaRepository(db: AppDatabase) {

    private val dao = db.tareaDao()

    fun observarPendientes(): Flow<List<Tarea>> =
        dao.obtenerPendientes().map { lista -> lista.map { it.aModelo() } }
}
