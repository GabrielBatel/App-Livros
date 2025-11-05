package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.api.GutendexApiService
import com.example.myapplication.data.LivroDao
import com.example.myapplication.data.LivroEntity
import com.example.myapplication.data.toEntity
import kotlinx.coroutines.flow.Flow

class LivroRepository(
    private val livroDao: LivroDao,
    private val apiService: GutendexApiService
) {

    fun getAllLivros(): Flow<List<LivroEntity>> {
        return livroDao.getAllLivros()
    }

    suspend fun getLivroById(id: Long): LivroEntity? {
        return livroDao.getLivroById(id)
    }

    suspend fun refreshLivrosIfNeeded() {
        val bookCount = livroDao.count()
        if (bookCount == 0) {
            try {
                val response = apiService.getPopularBooks()
                val livrosEntity = response.results.map { it.toEntity() }
                livroDao.insertAll(livrosEntity)
                Log.d("LivroRepository", "Carregados ${livrosEntity.size} livros da API")
            } catch (e: Exception) {
                Log.e("LivroRepository", "Erro ao carregar livros da API: ${e.message}")
                throw e
            }
        }
    }
}