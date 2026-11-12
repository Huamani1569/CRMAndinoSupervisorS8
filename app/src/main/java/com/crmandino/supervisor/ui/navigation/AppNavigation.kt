package com.crmandino.supervisor.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.crmandino.supervisor.model.RolUsuario
import com.crmandino.supervisor.ui.components.ConexionUi
import com.crmandino.supervisor.ui.components.EstadoConexionUi
import com.crmandino.supervisor.ui.components.FondoAndino
import com.crmandino.supervisor.ui.components.LocalConexion
import com.crmandino.supervisor.ui.components.LocalEstadoConexion
import com.crmandino.supervisor.ui.screens.AgenteColaScreen
import com.crmandino.supervisor.ui.screens.HomeScreen
import com.crmandino.supervisor.ui.screens.ListScreen
import com.crmandino.supervisor.ui.screens.LoginScreen
import com.crmandino.supervisor.ui.screens.ModelosScreen
import com.crmandino.supervisor.ui.screens.NotificacionesScreen
import com.crmandino.supervisor.ui.screens.NuevaCampanaScreen
import com.crmandino.supervisor.ui.screens.ResultadosCampanaScreen
import com.crmandino.supervisor.ui.screens.ServicioSupervisorScreen
import com.crmandino.supervisor.ui.screens.SplashScreen
import com.crmandino.supervisor.ui.screens.SupervisorPrincipalScreen
import com.crmandino.supervisor.ui.screens.SyncScreen
import com.crmandino.supervisor.ui.screens.TicketDetalleScreen
import com.crmandino.supervisor.viewmodel.AuthViewModel
import com.crmandino.supervisor.viewmodel.GestionViewModel
import com.crmandino.supervisor.viewmodel.LeadViewModel
import com.crmandino.supervisor.viewmodel.SyncViewModel
import com.crmandino.supervisor.viewmodel.TareaViewModel

// Pantallas de la app Supervisor (supervisor comercial y agente de servicio).
enum class Pantalla {
    SPLASH, LOGIN, PRINCIPAL, AGENTE, HOME, LISTA, SINCRONIZACION,
    NUEVA_CAMPANA, RESULTADOS, MODELOS, SERVICIO, TICKET, NOTIFICACIONES
}

/**
 * Flujo: Splash → Login → (supervisor) Panel con pestañas Panel · Equipo · Campañas · Clientes · Más
 *                        → (agente) Cola de servicio → Detalle del ticket.
 * Las pantallas de la Semana 8 (panel de leads, lista y sincronización) siguen disponibles desde "Más".
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    leadViewModel: LeadViewModel,
    tareaViewModel: TareaViewModel,
    syncViewModel: SyncViewModel,
    gestionViewModel: GestionViewModel,
    abrirNotificaciones: Int
) {
    var pantalla by rememberSaveable { mutableStateOf(Pantalla.SPLASH) }
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var etapaFiltro by rememberSaveable { mutableStateOf<String?>(null) }
    var idAbierto by rememberSaveable { mutableIntStateOf(0) }
    var origenTicket by rememberSaveable { mutableStateOf(Pantalla.AGENTE) }
    val usuario = authViewModel.usuarioActual
    val inicio = if (usuario?.rol == RolUsuario.AGENTE) Pantalla.AGENTE else Pantalla.PRINCIPAL

    val conectado by syncViewModel.conectado.collectAsState()
    val pendientes by syncViewModel.pendientes.collectAsState(initial = 0)
    val errores by syncViewModel.errores.collectAsState(initial = 0)

    LaunchedEffect(abrirNotificaciones) {
        if (abrirNotificaciones > 0 && authViewModel.usuarioActual != null) pantalla = Pantalla.NOTIFICACIONES
    }

    BackHandler(enabled = pantalla !in listOf(Pantalla.SPLASH, Pantalla.LOGIN, Pantalla.AGENTE) && !(pantalla == Pantalla.PRINCIPAL && pestana == 0)) {
        pantalla = when (pantalla) {
            Pantalla.PRINCIPAL -> { pestana = 0; Pantalla.PRINCIPAL }
            Pantalla.LISTA -> Pantalla.HOME
            Pantalla.TICKET -> origenTicket
            Pantalla.HOME, Pantalla.SINCRONIZACION, Pantalla.SERVICIO, Pantalla.MODELOS -> { pestana = 4; inicio }
            else -> inicio
        }
    }

    if (usuario == null && pantalla !in listOf(Pantalla.SPLASH, Pantalla.LOGIN)) {
        LaunchedEffect(Unit) { pantalla = Pantalla.LOGIN }
        return
    }

    val cerrarSesion = {
        authViewModel.cerrarSesion()
        pestana = 0
        pantalla = Pantalla.LOGIN
    }

    CompositionLocalProvider(
        LocalEstadoConexion provides EstadoConexionUi(conectado, pendientes, errores),
        LocalConexion provides ConexionUi(conectado, pendientes)
    ) {
        when (pantalla) {
            Pantalla.SPLASH -> SplashScreen(onFinalizar = {
                val u = authViewModel.usuarioActual
                pantalla = when {
                    u == null -> Pantalla.LOGIN
                    u.rol == RolUsuario.AGENTE -> Pantalla.AGENTE
                    else -> Pantalla.PRINCIPAL
                }
            })

            Pantalla.LOGIN -> FondoAndino {
                LoginScreen(viewModel = authViewModel, onLoginExitoso = {
                    pantalla = if (authViewModel.usuarioActual?.rol == RolUsuario.AGENTE) Pantalla.AGENTE else Pantalla.PRINCIPAL
                })
            }

            Pantalla.PRINCIPAL -> SupervisorPrincipalScreen(
                usuario = usuario!!,
                vm = gestionViewModel,
                pestana = pestana,
                onPestana = { pestana = it },
                onNuevaCampana = { pantalla = Pantalla.NUEVA_CAMPANA },
                onResultadosCampana = { idAbierto = it; pantalla = Pantalla.RESULTADOS },
                onServicio = { pantalla = Pantalla.SERVICIO },
                onModelos = { pantalla = Pantalla.MODELOS },
                onPanelParcial = { pantalla = Pantalla.HOME },
                onLeadsParcial = { etapaFiltro = null; pantalla = Pantalla.LISTA },
                onSincronizacion = { pantalla = Pantalla.SINCRONIZACION },
                onNotificaciones = { pantalla = Pantalla.NOTIFICACIONES },
                onCerrarSesion = cerrarSesion
            )

            Pantalla.AGENTE -> AgenteColaScreen(
                usuario = usuario!!,
                vm = gestionViewModel,
                onAbrirTicket = { idAbierto = it; origenTicket = Pantalla.AGENTE; pantalla = Pantalla.TICKET },
                onNotificaciones = { pantalla = Pantalla.NOTIFICACIONES },
                onCerrarSesion = cerrarSesion
            )

            Pantalla.TICKET -> TicketDetalleScreen(usuario!!, gestionViewModel, idAbierto, onAtras = { pantalla = origenTicket })
            Pantalla.SERVICIO -> ServicioSupervisorScreen(gestionViewModel,
                onAbrirTicket = { idAbierto = it; origenTicket = Pantalla.SERVICIO; pantalla = Pantalla.TICKET },
                onAtras = { pestana = 4; pantalla = Pantalla.PRINCIPAL })
            Pantalla.MODELOS -> ModelosScreen(gestionViewModel, onAtras = { pestana = 4; pantalla = Pantalla.PRINCIPAL })
            Pantalla.NUEVA_CAMPANA -> NuevaCampanaScreen(gestionViewModel, onAtras = { pestana = 2; pantalla = Pantalla.PRINCIPAL })
            Pantalla.RESULTADOS -> ResultadosCampanaScreen(gestionViewModel, idAbierto, onAtras = { pestana = 2; pantalla = Pantalla.PRINCIPAL })
            Pantalla.NOTIFICACIONES -> NotificacionesScreen(gestionViewModel, onAtras = { pantalla = inicio })

            // ---------- Pantallas de la Semana 8 ----------
            Pantalla.HOME -> FondoAndino {
                HomeScreen(
                    usuario = usuario!!,
                    leadViewModel = leadViewModel,
                    tareaViewModel = tareaViewModel,
                    syncViewModel = syncViewModel,
                    avisoSinConexion = authViewModel.avisoSinConexion,
                    onDescartarAviso = authViewModel::descartarAviso,
                    onVerLeads = { etapa -> etapaFiltro = etapa; pantalla = Pantalla.LISTA },
                    onVerSincronizacion = { pantalla = Pantalla.SINCRONIZACION },
                    onCerrarSesion = { pestana = 4; pantalla = Pantalla.PRINCIPAL }
                )
            }

            Pantalla.LISTA -> FondoAndino {
                ListScreen(
                    leadViewModel = leadViewModel,
                    etapaFiltro = etapaFiltro,
                    onCambiarFiltro = { etapaFiltro = it },
                    onAtras = { pantalla = Pantalla.HOME }
                )
            }

            Pantalla.SINCRONIZACION -> FondoAndino {
                SyncScreen(syncViewModel = syncViewModel, onAtras = { pestana = 4; pantalla = inicio })
            }
        }
    }
}
