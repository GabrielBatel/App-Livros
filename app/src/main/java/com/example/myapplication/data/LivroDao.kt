package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT COUNT(*) FROM livros")
    suspend fun count(): Int
}
