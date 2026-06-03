package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.ui.StrideMainScreen
import com.example.ui.TaskViewModel
import com.example.ui.TaskViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize local persistent database & core repository layers
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        
        // 2. Initialize application state container (ViewModel) safely via central Factory
        val factory = TaskViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StrideMainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
