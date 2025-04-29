# Next-Gen IoT Fire Alert System for Real-Time Monitoring

## Overview
The **Next-Gen IoT Fire Alert System** is an advanced IoT-based system designed for real-time fire, smoke, and gas detection. The system uses various sensors, including fire, smoke, and gas sensors, along with a temperature sensor, to detect hazardous conditions. In case of any anomalies, the system sends alerts to a connected mobile app and provides real-time monitoring data.

This project integrates IoT hardware with a mobile app built using Android Studio and Firebase for cloud data storage. It aims to provide a reliable and real-time fire detection solution.

---

## Features

- **Real-time Monitoring**: Continuously monitors fire, smoke, gas levels, and temperature data.
- **Real-time Alerts**: Sends push notifications and alerts when fire, smoke, or dangerous gas levels are detected.
- **Data Logging**: Stores all sensor data in Firebase for historical analysis.
- **Temperature Data**: Tracks the temperature readings and sends alerts if temperatures exceed preset thresholds.
- **Camera Integration**: Set up a camera for visual monitoring alongside the IoT sensors.
  
---

## Components

### Hardware
- **ESP32**: A powerful microcontroller for handling sensor data and communicating with Firebase.
- **Fire Sensor**: Detects the presence of fire or flame.
- **Smoke Sensor**: Detects smoke levels in the environment.
- **Gas Sensor**: Detects hazardous gases.
- **Temperature Sensor**: Monitors temperature in real time.
- **Camera Module**: Provides visual monitoring.

### Software
- **Android Studio**: For building the mobile app to display real-time sensor data.
- **Firebase**: Cloud platform for storing and managing sensor data and sending alerts.
- **Arduino IDE**: For programming the ESP32 board.
- **Firebase Cloud Messaging (FCM)**: To send push notifications.

  ![image](https://github.com/user-attachments/assets/a31fe9dd-b475-4bae-98eb-3b0c9bac1f18)


---

## Prerequisites

- **Hardware**:
  - ESP32 Board
  - Fire, Smoke, Gas, and Temperature Sensors
  - Camera Module (Optional)

- **Software**:
  - Android Studio (for mobile app development)
  - Arduino IDE (for programming ESP32)
  - Firebase account (for real-time data storage and alerts)
  - Firebase Cloud Messaging (for push notifications)
  
---

## Setup Instructions

### 1. Hardware Connections

- **Fire Sensor**: Connect the fire sensor to one of the analog input pins of the ESP32.
- **Smoke Sensor**: Connect the smoke sensor to another analog input pin of the ESP32.
- **Gas Sensor**: Connect the gas sensor to an analog input pin of the ESP32.
- **Temperature Sensor**: Connect the temperature sensor (e.g., DHT11) to a digital pin of the ESP32.
![image](https://github.com/user-attachments/assets/f18d1ed2-48f6-4010-bb31-a7dcdb4f5e1d)


Refer to the datasheets of each component for pinout and wiring details.

### 2. Setting up Firebase
- Create a Firebase project via the [Firebase Console](https://console.firebase.google.com/).
- Add Firebase to your Android project.
- Enable Firebase Realtime Database or Firestore to store sensor data.
- Set up Firebase Cloud Messaging (FCM) for push notifications.
- Obtain your Firebase credentials and configure them in your Android app.
  

### 3. Programming the ESP32

- Install the necessary libraries in Arduino IDE for ESP32 and sensors.
- Write the Arduino code to read data from the sensors and send it to Firebase.
  
Sample code for connecting the ESP32 to Firebase:

```cpp
#include <WiFi.h>
#include <FirebaseESP32.h>

// Wi-Fi credentials
const char* ssid = "your-SSID";
const char* password = "your-PASSWORD";

// Firebase credentials
#define FIREBASE_HOST "your-project-id.firebaseio.com"
#define FIREBASE_AUTH "your-database-secret"

// Initialize Firebase
FirebaseData firebaseData;

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

void loop() {
  // Get sensor data
  float temperature = analogRead(A0); // Example for temperature sensor
  float smokeLevel = analogRead(A1); // Example for smoke sensor
  // Send data to Firebase
  Firebase.setFloat(firebaseData, "/temperature", temperature);
  Firebase.setFloat(firebaseData, "/smoke", smokeLevel);
  delay(1000); // Delay before next reading
}
FirebaseDatabase database = FirebaseDatabase.getInstance();
DatabaseReference myRef = database.getReference("sensorData");

myRef.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        // Get sensor data from Firebase
        String fireStatus = dataSnapshot.child("fire").getValue(String.class);
        String smokeStatus = dataSnapshot.child("smoke").getValue(String.class);
        // Update UI
        fireTextView.setText(fireStatus);
        smokeTextView.setText(smokeStatus);
    }

    @Override
    public void onCancelled(DatabaseError error) {
        // Failed to read value
        Log.w(TAG, "Failed to read value.", error.toException());
    }
});
