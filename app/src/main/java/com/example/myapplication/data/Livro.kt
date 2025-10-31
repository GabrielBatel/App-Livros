package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "livros")
data class LivroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val autor: String,
    val sumario: String,
    val linguagem: String
)

data class LivroApiResponse(
    @SerializedName("results")
    val results: List<LivroApi>
)

data class LivroApi(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: List<PersonApi>,
    @SerializedName("subjects") val subjects: List<String>,
    @SerializedName("summaries") val summaries: List<String>,
    @SerializedName("languages") val languages: List<String>,
    @SerializedName("download_count") val downloadCount: Long,
    @SerializedName("formats") val formats: FormatsApi?
)

data class PersonApi(
    @SerializedName("name")
    val name: String
)

data class FormatsApi(
    @SerializedName("image/jpeg")
    val imageJpeg: String?
)

fun LivroApi.toEntity(): LivroEntity {
    return LivroEntity(
        titulo = this.title,
        autor = this.authors.joinToString(separator = ", ") { it.name },
        sumario = this.summaries.firstOrNull()?.replace("\r\n", " ") ?: this.subjects.firstOrNull() ?: "Sem sumário",
        linguagem = this.languages.joinToString(", ")
    )
}
