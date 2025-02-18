package com.example.composetutorial
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.content.res.Configuration

import com.example.composetutorial.ui.theme.ComposeTutorialTheme
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay


class EkaSivu : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            ContactDatabase::class.java,
            "contacts.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    private lateinit var sensorHandler: SensorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        requestNotificationPermission(this)
        sensorHandler = SensorHandler(this)

        val x = intent.getFloatExtra("xValue", Float.NaN)
        val y = intent.getFloatExtra("yValue", Float.NaN)
        val z = intent.getFloatExtra("zValue", Float.NaN)
        val acceleration = intent.getFloatExtra("acceleration", Float.NaN)

        setContent {
            val navController = rememberNavController()
            val viewModel: ContactViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ContactViewModel(db.dao) as T
                    }
                }
            )

            var showDialog by remember { mutableStateOf(!x.isNaN()) }

            var sensorData by remember {
                mutableStateOf(SensorData(x, y, z, acceleration))
            }

            LaunchedEffect(Unit) {
                sensorHandler.onSensorValuesChanged = { x, y, z, acceleration ->
                    sensorData = SensorData(x, y, z, acceleration)
                }
            }

            ComposeTutorialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        NotificationButton(
                            context = this@EkaSivu,
                            x = sensorData.x,
                            y = sensorData.y,
                            z = sensorData.z,
                            acceleration = sensorData.acceleration
                        )

                        PreviewConversation(navController)
                    }

                    if (showDialog) {
                        SensorDataDialog(
                            x = sensorData.x,
                            y = sensorData.y,
                            z = sensorData.z,
                            acceleration = sensorData.acceleration,
                            onDismiss = { showDialog = false } // Sulje dialogi
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sensorHandler.isInitialized) {
            sensorHandler.stopListening()
        }
    }
}

data class SensorData(val x: Float, val y: Float, val z: Float, val acceleration: Float)


fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "sensor_channel",
            "Sensor Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

fun sendNotification(context: Context, x: Float, y: Float, z: Float, acceleration: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    val intent = Intent(context, EkaSivu::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("xValue", x)
        putExtra("yValue", y)
        putExtra("zValue", z)
        putExtra("acceleration", acceleration)
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, "sensor_channel")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Sensor Triggered!")
        .setContentText("Tap to view sensor data.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(1, notification)
}

@Composable
fun SensorDataDialog(
    x: Float,
    y: Float,
    z: Float,
    acceleration: Float,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Sensor Data") },
        text = {
            Column {
                Text(text = "X: ${"%.2f".format(x)}")
                Text(text = "Y: ${"%.2f".format(y)}")
                Text(text = "Z: ${"%.2f".format(z)}")
                Text(text = "Acceleration: ${"%.2f".format(acceleration)}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


fun requestNotificationPermission(activity: ComponentActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101 // Request code
            )
        }
    }
}

@Composable
fun NotificationButton(
    context: Context,
    x: Float,
    y: Float,
    z: Float,
    acceleration: Float
) {
    Button(
        onClick = {
            sendNotification(context, x, y, z, acceleration)
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Send Notification")
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Composable
fun PreviewConversation(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Viestit", modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(SampleData.conversationSample) { message ->
                MessageCard(message)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("contacts") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("To contacts")
        }
    }
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }

        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)

@Composable
fun PreviewMessageCard() {
    ComposeTutorialTheme {
        Surface {
            MessageCard(
                msg = Message("Lexi", "Hey, take a look at Jetpack Compose, it's great!")
            )
        }
    }
}





