package com.crmandino.supervisor.data.mapper

import com.crmandino.supervisor.data.local.entities.TareaEntity
import com.crmandino.supervisor.data.remote.dto.TareaDto
import com.crmandino.supervisor.data.remote.dto.TareaRequest
import com.crmandino.supervisor.model.EstadoSincronizacion
import com.crmandino.supervisor.model.Tarea

fun TareaDto.aEntity(idLocal: Int = 0) = TareaEntity(
    id = idLocal,
    remoteId = id,
    uuid = uuid,
    leadUuid = leadUuid,
    leadEmpresa = leadEmpresa ?: "",
    descripcion = descripcion,
    fechaLimite = FechaMapper.isoAVisible(fechaLimite),
    completada = completada,
    vendedorId = vendedorId ?: 0,
    vendedorNombre = vendedorNombre ?: "",
    version = version,
    estadoSync = EstadoSincronizacion.SINCRONIZADO.name,
    mensajeError = null,
    eliminado = eliminado ?: false
)

fun TareaEntity.aModelo() = Tarea(
    id = id,
    remoteId = remoteId,
    uuid = uuid,
    leadUuid = leadUuid,
    leadEmpresa = leadEmpresa,
    descripcion = descripcion,
    fechaLimite = fechaLimite,
    completada = completada,
    vendedorNombre = vendedorNombre,
    version = version,
    estadoSync = EstadoSincronizacion.desdeTexto(estadoSync),
    mensajeError = mensajeError
)

fun TareaEntity.aRequest() = TareaRequest(
    uuid = uuid,
    leadUuid = leadUuid,
    descripcion = descripcion,
    fechaLimite = FechaMapper.visibleAIso(fechaLimite),
    completada = completada
)
