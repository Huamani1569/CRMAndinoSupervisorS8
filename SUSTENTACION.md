# Sustentación Semana 8 — CRM Andino Supervisor
Jean Piero Huamani Rodriguez · Desarrollo de Aplicaciones Móviles · Docente: Guillermo Peña

**Historia de usuario:** Como supervisor comercial, quiero visualizar el embudo de ventas y las tareas pendientes guardadas en Room, para monitorear el rendimiento comercial entre sesiones. Debo poder consultar los datos locales y conservar las operaciones autorizadas sin conexión, para sincronizarlas con la API REST cuando el servidor esté disponible.

## Requerimientos funcionales
| RF | Descripción | Dónde se ve / código |
|---|---|---|
| RF06 | Valor potencial acumulado por etapa | Embudo dibujado con **Canvas** en el panel · `LeadViewModel.resumenPorEtapa` · `EmbudoVentas.kt` |
| RF07 | Tareas pendientes por vendedor | Panel → "Tareas pendientes por vendedor" · `TareaViewModel.pendientesPorVendedor` |
| RF08 | Editar o reasignar un lead | Lista → ✏️ · `LeadRepository.editarLead()` (PUT con `vendedorId` y `version`) |
| RF09 | Listado completo por etapa | Lista con chips de etapa · `LeadDao.obtenerPorEtapa()` |
| RF10 | Eliminar lead descartado | Lista → 🗑️ · `LeadRepository.eliminarLead()` (DELETE, borrado lógico en el servidor) |
| RF11 | Actualizar Room con datos de la API | `SyncRepository.descargar()` (leads, tareas y vendedores) |
| RF12 | Escritura + operación pendiente en transacción | `db.withTransaction { … cola.registrar(…) }` |
| RF13 | WorkManager + botón manual | `worker/SyncWorker.kt` · botón "Sincronizar" |
| RF14 | Pendientes, errores y última sincronización | Panel y pantalla **Sincronización** |
| RF15 | Sin duplicados (UUID) y conflictos visibles | `X-Operacion-UUID` · "Reenviar (prueba de duplicado)" · "Errores y conflictos" |

**Permisos del rol (validados en el servidor):** consulta todos los leads y tareas; edita, reasigna y elimina leads. **No** crea leads ni modifica tareas o interacciones (la API responde 403).

## Room (base `crm_supervisor_s8.db`)
`usuarios` (perfil de sesión + vendedores para reasignar) · `leads` (id local, remoteId, uuid, version, estadoSync…) · `tareas` (consulta) · `operaciones_pendientes` · `sync_metadata`.
