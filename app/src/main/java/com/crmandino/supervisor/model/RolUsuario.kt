package com.crmandino.supervisor.model

// Roles que pueden entrar en ESTA app (Proyecto Final): supervisor comercial y agente de servicio.
object RolUsuario {
    const val APP = "SUPERVISOR"                 // la API valida con qué app se ingresa
    const val SUPERVISOR = "supervisor"
    const val AGENTE = "agente"
    const val ETIQUETA = "Supervisor comercial"
    const val ETIQUETA_AGENTE = "Agente de servicio"
    const val ROL_VENDEDOR = "vendedor"          // rol de los vendedores (para reasignar leads)

    fun etiqueta(rol: String?) = if (rol == AGENTE) ETIQUETA_AGENTE else ETIQUETA
}
