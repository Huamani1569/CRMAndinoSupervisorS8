package com.crmandino.supervisor.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.crmandino.supervisor.ui.components.EstadoConexionUi
import com.crmandino.supervisor.ui.components.LocalEstadoConexion
import com.crmandino.supervisor.ui.screens.HomeScreen
import com.crmandino.supervisor.ui.screens.ListScreen
import com.crmandino.supervisor.ui.screens.LoginScreen
import com.crmandino.supervisor.ui.screens.SplashScreen
import com.crmandino.supervisor.ui.screens.SyncScreen
import com.crmandino.supervisor.viewmodel.AuthViewModel
import com.crmandino.supervisor.viewmodel.LeadViewModel
import com.crmandino.supervisor.viewmodel.SyncViewModel
import com.crmandino.supervisor.viewmodel.TareaViewModel

// Pantallas de la app Supervisor.
enum class Pantalla { SPLASH, LOGIN, HOME, LISTA, SINCRONIZACION }

/**
 * Flujo: Splash → Login (o directo al panel si hay sesión guardada) → Home (panel) → Lista de leads,
 * y la pantalla de Sincronización desde el panel.
 * Sin librería de navegación: la pantalla actual se guarda en una variable de estado.
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    leadViewModel: LeadViewModel,
    tareaViewModel: TareaViewModel,
    syncViewModel: SyncViewModel
) {
    var pantalla by rememberSaveable { mutableStateOf(Pantalla.SPLASH) }
    var etapaFiltro by rememberSaveable { mutableStateOf<String?>(null) }
    val usuario = authViewModel.usuarioActual

    val conectado by syncViewModel.conectado.collectAsState()
    val pendientes by syncViewModel.pendientes.collectAsState(initial = 0)
    val errores by syncViewModel.errores.collectAsState(initial = 0)

    // Botón "atrás" del sistema
    BackHandler(enabled = pantalla == Pantalla.LISTA || pantalla == Pantalla.SINCRONIZACION) {
        pantalla = Pantalla.HOME
    }

    // Sin sesión activa no se puede entrar a las pantallas internas.
    if (usuario == null && pantalla !in listOf(Pantalla.SPLASH, Pantalla.LOGIN)) {
        LaunchedEffect(Unit) { pantalla = Pantalla.LOGIN }
        return
    }

    CompositionLocalProvider(LocalEstadoConexion provides EstadoConexionUi(conectado, pendientes, errores)) {
        when (pantalla) {
            Pantalla.SPLASH -> SplashScreen(onFinalizar = {
                pantalla = if (authViewModel.usuarioActual != null) Pantalla.HOME else Pantalla.LOGIN
            })

            Pantalla.LOGIN -> LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = { pantalla = Pantalla.HOME }
            )

            Pantalla.HOME -> HomeScreen(
                usuario = usuario!!,
                leadViewModel = leadViewModel,
                tareaViewModel = tareaViewModel,
                syncViewModel = syncViewModel,
                avisoSinConexion = authViewModel.avisoSinConexion,
                onDescartarAviso = authViewModel::descartarAviso,
                onVerLeads = { etapa ->
                    etapaFiltro = etapa
                    pantalla = Pantalla.LISTA
                },
                onVerSincronizacion = { pantalla = Pantalla.SINCRONIZACION },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    pantalla = Pantalla.LOGIN
                }
            )

            Pantalla.LISTA -> ListScreen(
                leadViewModel = leadViewModel,
                etapaFiltro = etapaFiltro,
                onCambiarFiltro = { etapaFiltro = it },
                onAtras = { pantalla = Pantalla.HOME }
            )

            Pantalla.SINCRONIZACION -> SyncScreen(
                syncViewModel = syncViewModel,
                onAtras = { pantalla = Pantalla.HOME }
            )
        }
    }
}
