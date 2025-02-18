package com.example.composetutorial

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class SensorHandler(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastNotificationTime = 0L

    var onSensorValuesChanged: ((Float, Float, Float, Float) -> Unit)? = null

    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.let { values ->
            val x = values[0]
            val y = values[1]
            val z = values[2]

            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            onSensorValuesChanged?.invoke(x, y, z, acceleration)

            if (acceleration > 15) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastNotificationTime > 5000) {
                    lastNotificationTime = currentTime
                    sendNotification(context, x, y, z, acceleration)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}
