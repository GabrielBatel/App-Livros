package com.example.myapplication.intent

import android.content.Context
import android.content.Intent
import com.example.myapplication.data.LivroEntity

fun createShareIntent(context: Context, livro: LivroEntity) {
    val shareText = "Confira este livro: ${livro.titulo} por ${livro.autor}. sinopse : ${livro.sumario}"
    
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    
    val chooser = Intent.createChooser(intent, "Compartilhar Livro")
    context.startActivity(chooser)
}
