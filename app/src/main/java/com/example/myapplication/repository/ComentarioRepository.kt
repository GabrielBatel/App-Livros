package com.example.myapplication.repository

import com.example.myapplication.data.Comentario
import com.example.myapplication.data.ComentarioDao

class ComentarioRepository(private val comentarioDao: ComentarioDao) {

    suspend fun insertComentario(comentario: Comentario) {
        comentarioDao.insertComentario(comentario)
    }

    suspend fun updateComentario(comentario: Comentario) {
        comentarioDao.updateComentario(comentario)
    }

    suspend fun deleteComentario(comentario: Comentario) {
        comentarioDao.deleteComentario(comentario)
    }
}
