package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.view.LivroDetailScreen
import com.example.myapplication.view.LivroListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var selectedLivroId by remember { mutableLongStateOf(0L) }
                var showDetail by remember { mutableStateOf(false) }

                if (showDetail && selectedLivroId > 0) {
                    LivroDetailScreen(
                        livroId = selectedLivroId,
                        onBackClick = { 
                            showDetail = false
                            selectedLivroId = 0L
                        }
                    )
                } else {
                    LivroListScreen(
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
