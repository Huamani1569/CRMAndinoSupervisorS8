package com.crmandino.supervisor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.crmandino.supervisor.data.local.entities.SyncMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetadataDao {

    @Upsert
    suspend fun guardar(metadata: SyncMetadataEntity)

    @Query("SELECT * FROM sync_metadata WHERE recurso = :recurso")
    fun observar(recurso: String): Flow<SyncMetadataEntity?>

    @Query("DELETE FROM sync_metadata")
    suspend fun borrarTodo()
}
