package com.example.myapplication.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.LivroEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivroEditScreen(
    livro: LivroEntity? = null,
    onSave: (LivroEntity) -> Unit,
    onCancel: () -> Unit
) {
    var titulo by rememberSaveable { mutableStateOf(livro?.titulo ?: "") }
    var autor by rememberSaveable { mutableStateOf(livro?.autor ?: "") }
    var sumario by rememberSaveable { mutableStateOf(livro?.sumario ?: "") }
    var linguagem by rememberSaveable { mutableStateOf(livro?.linguagem ?: "") }

    val isEditing = rememberSaveable { mutableStateOf(livro != null) }

    LaunchedEffect(livro?.id) {
        if ((titulo.isBlank() && autor.isBlank() && sumario.isBlank() && linguagem.isBlank()) && livro != null) {
            titulo = livro.titulo
            autor = livro.autor
            sumario = livro.sumario
            linguagem = livro.linguagem
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing.value) "Editar Livro" else "Novo Livro") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = autor, onValueChange = { autor = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = sumario, onValueChange = { sumario = it }, label = { Text("Sumário") }, modifier = Modifier.fillMaxWidth(), maxLines = 6)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = linguagem, onValueChange = { linguagem = it }, label = { Text("Idioma") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val id = livro?.id ?: 0L
                    onSave(LivroEntity(id = id, titulo = titulo.trim(), autor = autor.trim(), sumario = sumario.trim(), linguagem = linguagem.trim()))
                }, enabled = titulo.isNotBlank()) {
                    Text("Salvar")
                }
            }
        }
    }
}