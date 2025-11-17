package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.api.RetrofitInstance
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Comentario
import com.example.myapplication.data.LivroEntity
import com.example.myapplication.intent.createShareIntent
import com.example.myapplication.repository.ComentarioRepository
import com.example.myapplication.repository.LivroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado da UI de detalhes
data class LivroDetailState(
    val livro: LivroEntity? = null,
    val comentarios: List<Comentario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val novoComentario: String = "",
    val editingComentario: Comentario? = null,
    val deleted: Boolean = false
)

// ViewModel para detalhes do livro
class LivroDetailViewModel(
    private val repository: LivroRepository,
    private val comentarioRepository: ComentarioRepository,
    private val livroId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(LivroDetailState())
    val state: StateFlow<LivroDetailState> = _state.asStateFlow()

    init {
        loadLivroDetail()
        loadComentarios()
    }

    private fun loadLivroDetail() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val livro = repository.getLivroById(livroId)
                repository.getLivroFlowById(livroId).collect { livro ->
                    if (livro != null) {
                        _state.value = _state.value.copy(livro = livro, isLoading = false, error = null)
                    } else {
                        _state.value = _state.value.copy(livro = null, isLoading = false, error = "Livro não encontrado")
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Erro ao carregar detalhes: ${e.message}"
                )
            }
        }
    }

    private fun loadComentarios() {
        viewModelScope.launch {
            comentarioRepository.getComentariosByLivroId(livroId).collect { comentarios ->
                _state.value = _state.value.copy(comentarios = comentarios)
            }
        }
    }

    fun updateNovoComentario(texto: String) {
        _state.value = _state.value.copy(novoComentario = texto)
    }

    fun adicionarComentario() {
        val texto = _state.value.novoComentario.trim()
        if (texto.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val comentario = Comentario(
                        livroId = livroId,
                        texto = texto
                    )
                    comentarioRepository.insertComentario(comentario)
                    _state.value = _state.value.copy(novoComentario = "")
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        error = "Erro ao adicionar comentário: ${e.message}"
                    )
                }
            }
        }
    }

    fun editarComentario(comentario: Comentario) {
        _state.value = _state.value.copy(editingComentario = comentario)
    }

    fun cancelarEdicao() {
        _state.value = _state.value.copy(editingComentario = null)
    }

    fun salvarEdicao(novoTexto: String) {
        val comentario = _state.value.editingComentario
        if (comentario != null && novoTexto.trim().isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val comentarioAtualizado = comentario.copy(texto = novoTexto.trim())
                    comentarioRepository.updateComentario(comentarioAtualizado)
                    _state.value = _state.value.copy(editingComentario = null)
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        error = "Erro ao editar comentário: ${e.message}"
                    )
                }
            }
        }
    }

    fun deletarComentario(comentario: Comentario) {
        viewModelScope.launch {
            try {
                comentarioRepository.deleteComentario(comentario)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Erro ao deletar comentário: ${e.message}"
                )
            }
        }
    }

    fun deletarLivro() {
        val livro = _state.value.livro ?: return
        viewModelScope.launch {
            try {
                repository.deleteLivro(livro)
                _state.value = _state.value.copy(deleted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Erro ao deletar livro: ${e.message}"
                )
            }
        }
    }

    fun retry() {
        loadLivroDetail()
    }
}

// Factory do ViewModel para detalhes
@Composable
fun createLivroDetailViewModel(livroId: Long): LivroDetailViewModel {
    val context = LocalContext.current

    val apiService = remember { RetrofitInstance.api }
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { LivroRepository(database.livroDao(), apiService) }
    val comentarioRepository = remember { ComentarioRepository(database.comentarioDao()) }

    return viewModel(key = "livro_detail_$livroId") {
        LivroDetailViewModel(repository, comentarioRepository, livroId)
    }
}

// Tela de detalhes do livro
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivroDetailScreen(
    livroId: Long,
    onBackClick: () -> Unit,
    onEditClick: (LivroEntity) -> Unit // novo callback para editar
) {
    val viewModel = createLivroDetailViewModel(livroId)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Se foi deletado, volta automaticamente
    if (state.deleted) {
        LaunchedEffect(Unit) {
            onBackClick()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopBar
        TopAppBar(
            title = { Text("Detalhes do Livro") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            },
            actions = {
                val livro = state.livro
                if (livro != null) {
                    IconButton(onClick = { createShareIntent(context, livro) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar"
                        )
                    }

                    IconButton(onClick = { onEditClick(livro) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar livro"
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar livro",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        // diálogo de confirmação de deleção
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deletarLivro()
                    }) {
                        Text("Deletar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar remoção") },
                text = { Text("Deseja remover este livro? Esta ação não pode ser desfeita.") }
            )
        }

        // ...existing UI rendering (sem alterações)...
        val currentError = state.error
        val currentLivro = state.livro

        when {
            state.isLoading -> {
                LoadingDetailScreen()
            }

            currentError != null -> {
                ErrorDetailScreen(
                    error = currentError,
                    onRetry = { viewModel.retry() }
                )
            }

            currentLivro != null -> {
                LivroDetailContent(
                    livro = currentLivro,
                    comentarios = state.comentarios,
                    novoComentario = state.novoComentario,
                    editingComentario = state.editingComentario,
                    onNovoComentarioChange = viewModel::updateNovoComentario,
                    onAdicionarComentario = viewModel::adicionarComentario,
                    onEditarComentario = viewModel::editarComentario,
                    onSalvarEdicao = viewModel::salvarEdicao,
                    onCancelarEdicao = viewModel::cancelarEdicao,
                    onDeletarComentario = viewModel::deletarComentario
                )
            }
        }
    }
}

@Composable
private fun LoadingDetailScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Carregando detalhes...")
        }
    }
}

@Composable
private fun ErrorDetailScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
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
private fun LivroDetailContent(
    livro: LivroEntity,
    comentarios: List<Comentario>,
    novoComentario: String,
    editingComentario: Comentario?,
    onNovoComentarioChange: (String) -> Unit,
    onAdicionarComentario: () -> Unit,
    onEditarComentario: (Comentario) -> Unit,
    onSalvarEdicao: (String) -> Unit,
    onCancelarEdicao: () -> Unit,
    onDeletarComentario: (Comentario) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagem do livro (1/3 da largura)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📚",
                    style = MaterialTheme.typography.displayMedium
                )
            }
            
            // Informações do livro (2/3 da largura)
            Column(
                modifier = Modifier.weight(2f)
            ) {
                // Título
                Text(
                    text = livro.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Autor
                Text(
                    text = "Autor: ${livro.autor}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Idioma
                Text(
                    text = "Idioma: ${livro.linguagem}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sumário
        Text(
            text = "Sumário",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = livro.sumario,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Justify
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ComentariosSection(
            comentarios = comentarios,
            novoComentario = novoComentario,
            editingComentario = editingComentario,
            onNovoComentarioChange = onNovoComentarioChange,
            onAdicionarComentario = onAdicionarComentario,
            onEditarComentario = onEditarComentario,
            onSalvarEdicao = onSalvarEdicao,
            onCancelarEdicao = onCancelarEdicao,
            onDeletarComentario = onDeletarComentario
        )
    }
}

// Seção de Comentários
@Composable
private fun ComentariosSection(
    comentarios: List<Comentario>,
    novoComentario: String,
    editingComentario: Comentario?,
    onNovoComentarioChange: (String) -> Unit,
    onAdicionarComentario: () -> Unit,
    onEditarComentario: (Comentario) -> Unit,
    onSalvarEdicao: (String) -> Unit,
    onCancelarEdicao: () -> Unit,
    onDeletarComentario: (Comentario) -> Unit
) {
    Column {
        // Título da seção
        Text(
            text = "Comentários (${comentarios.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Campo para adicionar novo comentário
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = novoComentario,
                onValueChange = onNovoComentarioChange,
                label = { Text("Adicionar comentário") },
                modifier = Modifier.weight(1f),
                minLines = 2,
                maxLines = 4
            )
            
            Button(
                onClick = onAdicionarComentario,
                enabled = novoComentario.trim().isNotEmpty(),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Enviar")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de comentários
        if (comentarios.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Nenhum comentário ainda. Seja o primeiro a comentar!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            comentarios.forEach { comentario ->
                ComentarioCard(
                    comentario = comentario,
                    isEditing = editingComentario?.id == comentario.id,
                    onEdit = { onEditarComentario(comentario) },
                    onDelete = { onDeletarComentario(comentario) },
                    onSaveEdit = onSalvarEdicao,
                    onCancelEdit = onCancelarEdicao
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ComentarioCard(
    comentario: Comentario,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit
) {
    var editText by remember(isEditing) { mutableStateOf(if (isEditing) comentario.texto else "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSaveEdit(editText) },
                        enabled = editText.trim().isNotEmpty()
                    ) {
                        Text("Salvar")
                    }
                }
            } else {
                // Modo de visualização
                Text(
                    text = comentario.texto,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar comentário",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar comentário",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
