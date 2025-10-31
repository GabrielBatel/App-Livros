package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "comentarios",
    foreignKeys = [
        ForeignKey(
            entity = LivroEntity::class,
            parentColumns = ["id"],
            childColumns = ["livro_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comentario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "livro_id", index = true)
    val livroId: Long,

    val texto: String
)
