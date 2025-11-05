package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {

    @Insert
    suspend fun insertComentario(comentario: Comentario)

    @Update
    suspend fun updateComentario(comentario: Comentario)

    @Delete
    suspend fun deleteComentario(comentario: Comentario)

    @Query("SELECT * FROM comentarios WHERE livro_id = :livroId ORDER BY id DESC")
    fun getComentariosByLivroId(livroId: Long): Flow<List<Comentario>>
}
