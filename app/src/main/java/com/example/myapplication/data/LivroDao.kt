package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface LivroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(livros: List<LivroEntity>)

    @Query("SELECT * FROM livros ORDER BY titulo ASC")
    fun getAllLivros(): Flow<List<LivroEntity>>

    @Query("SELECT * FROM livros WHERE id = :id")
    suspend fun getLivroById(id: Long): LivroEntity?

    @Query("DELETE FROM livros")
    suspend fun deleteAll()

    @Query("SELECT * FROM livros WHERE id = :id LIMIT 1")
    fun getLivroByIdFlow(id: Long): Flow<LivroEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLivro(livro: LivroEntity): Long

    @Update
    suspend fun updateLivro(livro: LivroEntity)

    @Delete
    suspend fun deleteLivro(livro: LivroEntity)
    @Query("SELECT COUNT(*) FROM livros")
    suspend fun count(): Int
}
