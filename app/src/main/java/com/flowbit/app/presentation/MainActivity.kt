package com.flowbit.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.flowbit.app.presentation.navigation.FlowbitNavGraph
import com.flowbit.app.presentation.theme.FlowbitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowbitTheme {
                val navController = rememberNavController()
                FlowbitNavGraph(navController = navController)
            }
        }
    }
}
