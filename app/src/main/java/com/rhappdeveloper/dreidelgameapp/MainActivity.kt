package com.rhappdeveloper.dreidelgameapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.rhappdeveloper.dreidelgameapp.ui.screen.DreidelScreen
import com.rhappdeveloper.dreidelgameapp.ui.theme.DreidelGameAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreidelGameAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DreidelScreen(
                        systemPaddingValues = innerPadding
                    )
                }
            }
        }
    }
}