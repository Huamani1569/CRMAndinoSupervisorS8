package com.crmandino.supervisor.di

import android.content.Context
import com.crmandino.supervisor.data.local.AppDatabase
import com.crmandino.supervisor.data.local.SesionLocal
import com.crmandino.supervisor.data.remote.RetrofitClient
import com.crmandino.supervisor.data.repository.AuthRepository
import com.crmandino.supervisor.data.repository.ColaOperaciones
import com.crmandino.supervisor.data.repository.LeadRepository
import com.crmandino.supervisor.data.repository.SyncRepository
import com.crmandino.supervisor.data.repository.TareaRepository
import com.crmandino.supervisor.util.ConnectivityObserver
import com.crmandino.supervisor.worker.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Contenedor de DEPENDENCIAS (inyección manual): crea UNA sola vez Room, Retrofit,
 * la sesión segura y los repositorios, y se los entrega a los ViewModel y al SyncWorker.
 */
class AppContainer(private val context: Context) {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val sesion = SesionLocal(context)
    val db: AppDatabase = AppDatabase.getDB(context)
    val api = RetrofitClient.crear(sesion)

    val conectividad = ConnectivityObserver(context)
    val conectado: StateFlow<Boolean> =
        conectividad.estaConectado.stateIn(alcance, SharingStarted.Eagerly, conectividad.hayConexion())

    // Después de cada escritura se pide sincronizar (WorkManager espera a que haya red).
    private val programarSync: () -> Unit = { SyncWorker.sincronizarPronto(context) }

    private val cola = ColaOperaciones(db.operacionPendienteDao())

    val authRepository = AuthRepository(db, api, sesion)
    val leadRepository = LeadRepository(db, cola, programarSync)
    val tareaRepository = TareaRepository(db)
    val syncRepository = SyncRepository(db, api, sesion)

    fun sincronizarEnSegundoPlano() = SyncWorker.sincronizarPronto(context)
}
