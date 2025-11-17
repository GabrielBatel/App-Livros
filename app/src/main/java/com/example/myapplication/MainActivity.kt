package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.LivroEntity
import com.example.myapplication.repository.ComentarioRepository
import com.example.myapplication.repository.LivroRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.view.LivroDetailScreen
import com.example.myapplication.view.LivroEditScreen
import com.example.myapplication.view.LivroListScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val apiService = remember { RetrofitInstance.api }
                val database = remember { AppDatabase.getDatabase(context) }
                val repo = remember { LivroRepository(database.livroDao(), apiService) }
                val comentarioRepo = remember { ComentarioRepository(database.comentarioDao()) }

                var selectedLivroId by rememberSaveable { mutableLongStateOf(0L) }
                var showDetail by rememberSaveable { mutableStateOf(false) }
                var showEdit by rememberSaveable { mutableStateOf(false) }
                var editingLivro by remember { mutableStateOf<LivroEntity?>(null) }

                val scope = rememberCoroutineScope()

                when {
                    showEdit -> {
                        // Tela de criação/edição
                        LivroEditScreen(
                            livro = editingLivro,
                            onSave = { livro ->
                                scope.launch {
                                    try {
                                        if (livro.id > 0L) {
                                            // substituir/atualizar via insert REPLACE
                                            repo.insertLivro(livro)
                                            selectedLivroId = livro.id
                                            showDetail = true
                                        } else {
                                            repo.insertLivro(livro)
                                            showDetail = false
                                            selectedLivroId = 0L
                                        }
                                    } catch (e: Exception) {
                                        // erro simples: mantém a tela de edição aberta
                                    } finally {
                                        showEdit = false
                                        editingLivro = null
                                    }
                                }
                            },
                            onCancel = {
                                showEdit = false
                                editingLivro = null
                            }
                        )
                    }

                    showDetail && selectedLivroId > 0L -> {
                        // Tela de detalhe com callback para editar
                        LivroDetailScreen(
                            livroId = selectedLivroId,
                            onBackClick = {
                                showDetail = false
                                selectedLivroId = 0L
                            },
                            onEditClick = { livro ->
                                // abrir edição com o livro carregado
                                editingLivro = livro
                                showEdit = true
                            }
                        )
                    }

                    else -> {
                        // Lista de livros com FAB para criar e clique para abrir detalhe
                        LivroListScreen(
                            onCreateClick = {
                                editingLivro = null
                                showEdit = true
                            },
                            onLivroClick = { livroId ->
                                selectedLivroId = livroId
                                showDetail = true
                            }
                        )
                    }
                }
            }
        }
    }
}