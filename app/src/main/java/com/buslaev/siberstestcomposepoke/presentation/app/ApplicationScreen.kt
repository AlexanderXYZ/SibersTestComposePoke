package com.buslaev.siberstestcomposepoke.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.buslaev.siberstestcomposepoke.presentation.detail.DetailScreen
import com.buslaev.siberstestcomposepoke.presentation.main.MainScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ApplicationScreen(
    appState: AppState = rememberAppState()
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.White
        )
    }

    val viewModel: AppViewModel = hiltViewModel()
    NavHost(
        navController = appState.navController,
        startDestination = Screens.MainScreen.route
    ) {
        composable(Screens.MainScreen.route) {
            MainScreen(
                navController = appState.navController,
                viewModel = viewModel,
                isOnline = appState.isOnline
            )
        }
        composable(Screens.DetailScreen.route) {
            viewModel.uiState.selectedPokemon?.let {
                DetailScreen(
                    navController = appState.navController,
                    pokemon = it
                )
            }
        }
    }
}