package com.firealertsystemapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference()

        // Create Notification Channel
        createNotificationChannel()

        setContent {
            FireAlertApp(database, this)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fire_alert_channel",
                "Fire Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for Fire, Smoke, and Temperature"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireAlertApp(database: DatabaseReference, context: Context) {
    var fireStatus by remember { mutableStateOf("Loading...") }
    var smokeStatus by remember { mutableStateOf("Loading...") }
    var tempStatus by remember { mutableStateOf("Loading...") }
    var history by remember { mutableStateOf<List<String>>(emptyList()) }
    var showHistory by remember { mutableStateOf(false) }

    // Previous alert states
    var prevFire by remember { mutableStateOf(1) } // Assuming 1 means no fire
    var prevSmoke by remember { mutableStateOf(0) }
    var prevTemp by remember { mutableStateOf(0) }

    // Function to fetch real-time data from Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val fire = snapshot.child("FireSensor").getValue(Int::class.java) ?: 1
                    val smoke = snapshot.child("SmokeSensor").getValue(Int::class.java) ?: 0
                    val temperature = snapshot.child("Temperature").getValue(Int::class.java) ?: -1

                    // Update UI statuses
                    fireStatus = if (fire == 0) "🔥 Fire Detected!" else "✅ No Fire"
                    smokeStatus = if (smoke > 1000) "💨  Smoke Level: $smoke" else "✅ high Smoke: $smoke"
                    tempStatus = "🌡 Temperature: 35 °C"

                    // Prepare notification messages
                    val alerts = mutableListOf<String>()

                    // Trigger notifications only when new danger is detected
                    if (fire == 0 && prevFire != 0) {
                        alerts.add("🔥 Fire Detected!")
                    }
                    if (smoke > 1000 && prevSmoke <= 1000) {
                        alerts.add("💨 High Smoke Level: $smoke")
                    }
                    if (temperature > 50 && prevTemp <= 50) {
                        alerts.add("🌡 High Temperature: $temperature°C")
                    }

                    // Send alert if there are any new detections
                    if (alerts.isNotEmpty()) {
                        val alertMessage = alerts.joinToString("\n")
                        showNotification(context, "🚨 ALERT!", alertMessage)
                    }

                    // Update previous states to prevent duplicate alerts
                    prevFire = fire
                    prevSmoke = smoke
                    prevTemp = temperature

                    // Get current time
                    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                    // Update history log (keep only the last 10 readings)
                    val newHistory = history.toMutableList()
                    newHistory.add(0, "[$time] 🔥 Fire: ${if (fire == 0) "YES" else "NO"} | 💨 Smoke: $smoke | 🌡 Temp: $temperature°C")
                    if (newHistory.size > 10) {
                        newHistory.removeAt(newHistory.size - 1)
                    }
                    history = newHistory
                } else {
                    Toast.makeText(context, "⚠ No data found in Firebase!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "❌ Firebase Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🔥 Fire Alert System") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = fireStatus, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = smokeStatus, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = tempStatus, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            // Refresh Button
            Button(
                onClick = { /* No need to refresh, real-time updates enabled */ },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("✅ Real-Time Enabled")
            }

            // Call Fire Station Button
            Button(
                onClick = {
                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:101"))
                    context.startActivity(callIntent)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text("🚒 Call Fire Station")
            }

            // Show History Button
            Button(
                onClick = { showHistory = !showHistory },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                Text(if (showHistory) "📜 Hide History" else "📜 Show History")
            }

            // Display History if visible
            if (showHistory) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(10.dp)
                ) {
                    items(history) { entry ->
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}

// Function to Show Notifications
fun showNotification(context: Context, title: String, message: String) {
    val notification = NotificationCompat.Builder(context, "fire_alert_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    with(NotificationManagerCompat.from(context)) {
        notify(System.currentTimeMillis().toInt(), notification)
    }
}

@Preview(showBackground = true)
@Composable
fun FireAlertPreview() {
    FireAlertApp(FirebaseDatabase.getInstance().getReference(), LocalContext.current)
}