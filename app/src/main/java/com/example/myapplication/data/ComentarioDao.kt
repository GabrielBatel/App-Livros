package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface ComentarioDao {

    @Insert
    suspend fun insertComentario(comentario: Comentario)

    @Update
    suspend fun updateComentario(comentario: Comentario)

    @Delete
    suspend fun deleteComentario(comentario: Comentario)
}
