package com.crmandino.supervisor

import android.app.Application
import com.crmandino.supervisor.di.AppContainer
import com.crmandino.supervisor.worker.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

// Clase Application (registrada en el AndroidManifest): crea el contenedor de dependencias
// y programa la sincronización en segundo plano.
class CrmSupervisorApplication : Application() {

    lateinit var contenedor: AppContainer
        private set

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
        SyncWorker.programarPeriodica(this)

        // Al RECUPERAR la conexión se sincroniza de inmediato (RF13).
        alcance.launch {
            contenedor.conectado
                .drop(1)
                .filter { it }
                .collect { contenedor.sincronizarEnSegundoPlano() }
        }
    }
}
