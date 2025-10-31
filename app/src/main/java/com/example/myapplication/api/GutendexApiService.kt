package com.example.myapplication.api

import com.example.myapplication.data.LivroApiResponse
import retrofit2.http.GET


interface GutendexApiService {

    @GET("books")
    suspend fun getPopularBooks(): LivroApiResponse

}
