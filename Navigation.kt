package com.example.composetutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Composable
fun Navigation(navController: NavHostController, viewModel: ContactViewModel) {
    val state by viewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = "messages") {
        composable("messages") {
            PreviewConversation(navController)
        }
        composable("contacts") {
            ContactScreen(state, onEvent = viewModel::onEvent, navController)
        }
    }
}

