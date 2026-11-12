package com.crmandino.supervisor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.crmandino.supervisor.util.BarrasDelSistema
import com.crmandino.supervisor.util.prepararTema
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crmandino.supervisor.di.AppContainer
import com.crmandino.supervisor.ui.navigation.AppNavigation
import com.crmandino.supervisor.ui.theme.CRMAndinoTheme
import com.crmandino.supervisor.viewmodel.AuthViewModel
import com.crmandino.supervisor.viewmodel.GestionViewModel
import com.crmandino.supervisor.viewmodel.LeadViewModel
import com.crmandino.supervisor.viewmodel.SyncViewModel
import com.crmandino.supervisor.viewmodel.TareaViewModel

// Punto de entrada: crea los ViewModel con los repositorios del contenedor (di/AppContainer).
class MainActivity : ComponentActivity() {

    private val contenedor: AppContainer
        get() = (application as CrmSupervisorApplication).contenedor

    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { initializer { AuthViewModel(contenedor.authRepository, contenedor.syncRepository) } }
    }

    private val leadViewModel: LeadViewModel by viewModels {
        viewModelFactory { initializer { LeadViewModel(contenedor.leadRepository) } }
    }

    private val tareaViewModel: TareaViewModel by viewModels {
        viewModelFactory { initializer { TareaViewModel(contenedor.tareaRepository) } }
    }

    private val syncViewModel: SyncViewModel by viewModels {
        viewModelFactory {
            initializer {
                SyncViewModel(contenedor.syncRepository, contenedor.conectado, contenedor::sincronizarEnSegundoPlano)
            }
        }
    }

    private val gestionViewModel: GestionViewModel by viewModels {
        viewModelFactory { initializer { GestionViewModel(contenedor.gestionRepository, contenedor.conectado) } }
    }

    // Se incrementa cada vez que el usuario toca una notificación del sistema
    private val abrirNotificaciones = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepararTema()   // modo claro / oscuro guardado
        if (intent?.getBooleanExtra("abrir_notificaciones", false) == true) abrirNotificaciones.intValue = 1
        setContent {
            CRMAndinoTheme {
                BarrasDelSistema()
                AppNavigation(authViewModel, leadViewModel, tareaViewModel, syncViewModel, gestionViewModel, abrirNotificaciones.intValue)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("abrir_notificaciones", false)) abrirNotificaciones.intValue += 1
    }
}
