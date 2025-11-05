package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.LivroEntity
import com.example.myapplication.repository.LivroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado da UI
data class LivroListState(
    val livros: List<LivroEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel simples
class LivroListViewModel(private val repository: LivroRepository) : ViewModel() {

    private val _state = MutableStateFlow(LivroListState())
    val state: StateFlow<LivroListState> = _state.asStateFlow()

    init {
        loadLivros()
        refreshLivrosIfNeeded()
    }

    private fun loadLivros() {
        viewModelScope.launch {
            repository.getAllLivros().collect { livros ->
                _state.value = _state.value.copy(livros = livros)
            }
        }
    }

    private fun refreshLivrosIfNeeded() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                repository.refreshLivrosIfNeeded()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Erro ao carregar livros: ${e.message}"
                )
            }
        }
    }

    fun retry() {
        refreshLivrosIfNeeded()
    }
}

// Factory do ViewModel
@Composable
fun createLivroListViewModel(): LivroListViewModel {
    val context = LocalContext.current

    val apiService = remember { RetrofitInstance.api }
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { LivroRepository(database.livroDao(), apiService) }

    return viewModel { LivroListViewModel(repository) }
}

// Tela principal
@Composable
fun LivroListScreen(
    onLivroClick: (Long) -> Unit = {}
) {
    val viewModel = createLivroListViewModel()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título da tela
        Text(
            text = "Biblioteca de Livros",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val currentError = state.error
        when {
            state.isLoading && state.livros.isEmpty() -> {
                LoadingScreen()
            }

            currentError != null && state.livros.isEmpty() -> {
                ErrorScreen(
                    error = currentError,
                    onRetry = { viewModel.retry() }
                )
            }

            else -> {
                LivroGrid(
                    livros = state.livros,
                    onLivroClick = onLivroClick
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Carregando livros...")
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Tentar Novamente")
            }
        }
    }
}

@Composable
private fun LivroGrid(
    livros: List<LivroEntity>,
    onLivroClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(livros) { livro ->
            LivroCard(
                livro = livro,
                onClick = { onLivroClick(livro.id) }
            )
        }
    }
}

@Composable
private fun LivroCard(
    livro: LivroEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Slot para imagem (placeholder simples)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📚",
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título do livro
            Text(
                text = livro.titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Autor
            Text(
                text = livro.autor,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}