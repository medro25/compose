package com.example.composetutorial

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.net.Uri
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.core.app.NotificationCompat
import com.example.composetutorial.ui.theme.ComposeTutorialTheme
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager





data class Message(val author: String, val body: String)

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_IMAGE_URI = stringPreferencesKey("image_uri")
    }

    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USERNAME]
    }

    val imageUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_IMAGE_URI]
    }

    suspend fun saveUserData(username: String, imageUri: String?) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
            preferences[KEY_IMAGE_URI] = imageUri ?: ""
        }
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "profile_picture.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return Uri.fromFile(file)
}
@OptIn(ExperimentalMaterial3Api::class)

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Register the sensor listener
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        setContent {
            ComposeTutorialTheme {
                MainApp()
            }
        }
    }

    // Correctly placed SensorEventListener methods
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val brightness = it.values[0] // Get brightness level in lux
            showNotification(this, "Light Sensor", "Brightness: $brightness lux")

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}


@Composable
fun MainApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_view") {
        composable("main_view") { MainView(navController) }
        composable("second_view") { SecondView(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("User") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Load stored data (username)
    LaunchedEffect(Unit) {
        userPreferences.username.collect { storedUsername ->
            storedUsername?.let { username = it }
        }
    }

    // Load stored data (image URI)
    LaunchedEffect(Unit) {
        userPreferences.imageUri.collect { storedImageUri ->
            storedImageUri?.let { imageUri = Uri.parse(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compose Tutorial") },
                actions = {
                    IconButton(onClick = { navController.navigate("second_view") }) {
                        Icon(
                            painter = rememberAsyncImagePainter(model = R.drawable.ic_button_icon),
                            contentDescription = "Go to Second View"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn {
                items(SampleData.conversationSample) { message ->
                    MessageCard(message.copy(author = username), imageUri)
                }
            }
        }
    }
}

@Composable
fun MessageCard(msg: Message, imageUri: Uri?) {
    Row(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("?", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        var isDarkMode by remember { mutableStateOf(false) }

        val surfaceColor by animateColorAsState(
            if (isDarkMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )

        Column(
            modifier = Modifier
                .clickable {
                    isExpanded = !isExpanded
                    isDarkMode = !isDarkMode
                }
                .padding(4.dp)
        ) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondView(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences(context) }

    var userInput by remember { mutableStateOf("") }

    // Load stored username
    LaunchedEffect(Unit) {
        userPreferences.username.collect { storedUsername ->
            storedUsername?.let { userInput = it }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showNotification(context, "Compose Tutorial", "Notification Enabled")
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Enable Notifications") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                coroutineScope.launch {
                    userPreferences.saveUserData(userInput.trim(), null)
                }
                navController.navigate("main_view")
            }) {
                Text("Save and Go Back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }) {
                Text("Enable Notification")
            }
        }
    }
}

// Function to Show Notification
fun showNotification(context: Context, title: String, message: String) {
    val channelId = "compose_tutorial_channel"
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Compose Tutorial Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Intent to open the app when notification is clicked
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)  // <-- Clickable action added here
        .build()

    notificationManager.notify(1, notification)
}