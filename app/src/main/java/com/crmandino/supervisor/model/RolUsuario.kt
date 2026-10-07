package com.crmandino.supervisor.model

// Rol que puede entrar en ESTA app.
// Según la guía del curso, el rol "administrador" representa al SUPERVISOR comercial.
object RolUsuario {
    const val VALOR = "administrador"           // Valor de la columna "rol" en la tabla usuarios
    const val ETIQUETA = "Supervisor comercial" // Lo que se muestra en pantalla
    const val ROL_VENDEDOR = "cliente"          // Rol de los vendedores (para reasignar leads)
}
