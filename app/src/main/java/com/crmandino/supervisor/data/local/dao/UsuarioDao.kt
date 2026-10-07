package com.crmandino.supervisor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.crmandino.supervisor.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Upsert
    suspend fun guardar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE esSesion = 1 LIMIT 1")
    fun observarSesion(): Flow<UsuarioEntity?>

    // Vendedores descargados de la API (para reasignar leads)
    @Query("SELECT * FROM usuarios WHERE esSesion = 0 AND rol = :rol ORDER BY nombre")
    fun observarPorRol(rol: String): Flow<List<UsuarioEntity>>

    @Query("DELETE FROM usuarios WHERE esSesion = 0")
    suspend fun borrarNoSesion()

    @Query("DELETE FROM usuarios")
    suspend fun borrarTodo()
}
