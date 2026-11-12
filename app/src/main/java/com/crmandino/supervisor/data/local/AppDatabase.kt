package com.crmandino.supervisor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crmandino.supervisor.data.local.dao.CacheDao
import com.crmandino.supervisor.data.local.dao.LeadDao
import com.crmandino.supervisor.data.local.dao.OperacionPendienteDao
import com.crmandino.supervisor.data.local.dao.SyncMetadataDao
import com.crmandino.supervisor.data.local.dao.TareaDao
import com.crmandino.supervisor.data.local.dao.UsuarioDao
import com.crmandino.supervisor.data.local.entities.CacheEntity
import com.crmandino.supervisor.data.local.entities.LeadEntity
import com.crmandino.supervisor.data.local.entities.OperacionPendienteEntity
import com.crmandino.supervisor.data.local.entities.SyncMetadataEntity
import com.crmandino.supervisor.data.local.entities.TareaEntity
import com.crmandino.supervisor.data.local.entities.UsuarioEntity

// Base de datos local (SQLite) de la app Supervisor: copia de los datos + cola de sincronización.
@Database(
    entities = [
        UsuarioEntity::class,
        LeadEntity::class,
        TareaEntity::class,
        OperacionPendienteEntity::class,
        SyncMetadataEntity::class,
        CacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun leadDao(): LeadDao
    abstract fun tareaDao(): TareaDao
    abstract fun operacionPendienteDao(): OperacionPendienteDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDB(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crm_supervisor_pf.db"   // Proyecto Final: base nueva (etapas nuevas y caché)
                ).build().also { INSTANCE = it }
            }
    }
}
