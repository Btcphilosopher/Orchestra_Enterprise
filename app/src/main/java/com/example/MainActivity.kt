package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlatformViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) { // Explicitly dark darkTheme for premium corporate vibe
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val viewModel: PlatformViewModel = viewModel()
          DashboardScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
