package com.example.composetutorial

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Composable
fun Navigation(
    navController: NavHostController,
    viewModel: ContactViewModel,
    capturedImageUri: Uri?,
    onImageCaptured: (Uri) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Store captured image

    NavHost(navController = navController, startDestination = "messages") {
        composable("messages") {
            PreviewConversation(navController, capturedImageUri)
        }
        composable("contacts") {
            ContactScreen(state, onEvent = viewModel::onEvent, navController)
        }
        composable("camera") {
            CameraScreen(navController.context, navController) { uri ->
                onImageCaptured(uri)
                navController.navigate("messages") {
                    popUpTo("messages") { inclusive = true }
                }
            }
        }
    }
}

