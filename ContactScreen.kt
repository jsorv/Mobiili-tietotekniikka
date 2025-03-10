package com.example.composetutorial
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.Manifest
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.provider.MediaStore
import android.util.Log

@Composable
fun ContactScreen(
    state: ContactState,
    onEvent: (ContactEvent) -> Unit,
    navController: NavController

) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onEvent(ContactEvent.ShowDialog)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f) //
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (state.isAddingContact) {
                    AddContactDialog(
                        state = state,
                        onEvent = onEvent,
                        selectedImageUri = selectedImageUri
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SortType.values().forEach { sortType ->
                                Row(
                                    modifier = Modifier.clickable {
                                        onEvent(ContactEvent.SortContacts(sortType))
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = state.sortType == sortType,
                                        onClick = {
                                            onEvent(ContactEvent.SortContacts(sortType))
                                        }
                                    )
                                    Text(text = sortType.name)
                                }
                            }
                        }
                    }
                    items(state.contacts) { contact ->
                        ContactCard(contact, onEvent)
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("To messages")
            }
        }
    }
}
@Composable
fun RequestCameraPermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}

@Composable
fun CameraScreen(context: Context, navController: NavController, onImageCaptured: (Uri) -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
            saveImageToGallery(context, imageUri!!)
            onImageCaptured(imageUri!!)
            navController.navigate("messages") {
                popUpTo("messages") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            imageUri = createGalleryImageUri(context)
            if (imageUri != null) {
                cameraLauncher.launch(imageUri!!)
            } else {
                Toast.makeText(context, "Failed to create file URI", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Open Camera")
        }

        Button(onClick = { navController.navigate("messages") }) {
            Text("Back to Messages")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

fun createGalleryImageUri(context: Context): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        put(MediaStore.Images.Media.IS_PENDING, 1) // Set as pending
    }

    val contentResolver = context.contentResolver
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

fun saveImageToGallery(context: Context, imageUri: Uri) {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.IS_PENDING, 0)
    }
    contentResolver.update(imageUri, contentValues, null, null)

    Toast.makeText(context, "Image saved to gallery!", Toast.LENGTH_SHORT).show()
}


@Composable
fun ContactCard(contact: Contacts, onEvent: (ContactEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val imagePainter = if (!contact.imageUri.isNullOrEmpty()){
            rememberAsyncImagePainter(contact.imageUri)
        } else {
            painterResource(id = R.drawable.avatar)
        }

        Image(
            painter = imagePainter,
            contentDescription = "Contact Image",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${contact.firstName} ${contact.lastName}",
                fontSize = 20.sp
            )
            Text(
                text = contact.phoneNumber,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = { onEvent(ContactEvent.DeleteContact(contact)) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete contact"
            )
        }
    }
}
