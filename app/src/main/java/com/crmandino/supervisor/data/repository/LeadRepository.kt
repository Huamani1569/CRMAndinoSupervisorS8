package com.crmandino.supervisor.data.repository

import androidx.room.withTransaction
import com.crmandino.supervisor.data.local.AppDatabase
import com.crmandino.supervisor.data.mapper.aModelo
import com.crmandino.supervisor.data.mapper.aRequest
import com.crmandino.supervisor.model.EntidadSync
import com.crmandino.supervisor.model.EstadoSincronizacion
import com.crmandino.supervisor.model.Lead
import com.crmandino.supervisor.model.RolUsuario
import com.crmandino.supervisor.model.TipoOperacion
import com.crmandino.supervisor.model.UsuarioSesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Repositorio de LEADS del supervisor: consulta (Room), edición/reasignación y eliminación.
// Cada escritura guarda el cambio + su operación pendiente en UNA transacción y pide sincronizar.
class LeadRepository(
    private val db: AppDatabase,
    private val cola: ColaOperaciones,
    private val programarSync: () -> Unit
) {
    private val leadDao = db.leadDao()

    fun observarLeads(): Flow<List<Lead>> =
        leadDao.obtenerTodos().map { lista -> lista.map { it.aModelo() } }

    fun observarPorEtapa(etapa: String?): Flow<List<Lead>> =
        (if (etapa == null) leadDao.obtenerTodos() else leadDao.obtenerPorEtapa(etapa))
            .map { lista -> lista.map { it.aModelo() } }

    // Vendedores descargados de la API (a quiénes se puede reasignar un lead)
    fun observarVendedores(): Flow<List<UsuarioSesion>> =
        db.usuarioDao().observarPorRol(RolUsuario.ROL_VENDEDOR)
            .map { lista -> lista.map { UsuarioSesion(it.id, it.nombre, it.correo, it.rol) } }

    // RF08: editar o reasignar
    suspend fun editarLead(
        id: Int, empresa: String, valor: Double, etapa: String, vendedorId: Int, vendedorNombre: String
    ) {
        db.withTransaction {
            val actual = leadDao.porId(id) ?: return@withTransaction
            val editado = actual.copy(
                empresa = empresa,
                valorPotencial = valor,
                etapa = etapa,
                vendedorId = vendedorId,
                vendedorNombre = vendedorNombre,
                estadoSync = EstadoSincronizacion.PENDIENTE.name,
                mensajeError = null,
                actualizadoEnLocal = System.currentTimeMillis()
            )
            leadDao.actualizar(editado)
            val descripcion = if (actual.vendedorId != vendedorId) {
                "Reasignar \"$empresa\": ${actual.vendedorNombre} → $vendedorNombre"
            } else {
                "Editar lead \"$empresa\""
            }
            cola.registrar(EntidadSync.LEAD, id, TipoOperacion.ACTUALIZAR, editado.aRequest(), descripcion)
        }
        programarSync()
    }

    // RF10: eliminar un lead descartado (se oculta ya; se borra del celular cuando el servidor confirma)
    suspend fun eliminarLead(id: Int) {
        db.withTransaction {
            val actual = leadDao.porId(id) ?: return@withTransaction
            leadDao.actualizar(actual.copy(eliminado = true, estadoSync = EstadoSincronizacion.PENDIENTE.name, mensajeError = null))
            cola.registrar(EntidadSync.LEAD, id, TipoOperacion.ELIMINAR, actual.aRequest(), "Eliminar lead descartado \"${actual.empresa}\"")
        }
        programarSync()
    }
}
