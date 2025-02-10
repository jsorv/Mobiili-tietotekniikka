package com.example.composetutorial
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ContactScreen(
    state: ContactState,
    onEvent: (ContactEvent) -> Unit,
    navController: NavController
) {
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
                    AddContactDialog(state = state, onEvent = onEvent)
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
fun ContactCard(contact: Contacts, onEvent: (ContactEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display contact image or default avatar
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
