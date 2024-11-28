/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.facelandmarker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.facelandmarker.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorEventListener: SensorEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }

    }

    override fun onBackPressed() {
        finish()
    }



    override fun onResume() {
        super.onResume()

        // Initialize sensorManager if it's not already initialized
        if (!this::sensorManager.isInitialized) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }

        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (!this::sensorEventListener.isInitialized) {
            sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    // this is just a magnitude of g force, negative g-force more deadly than positive
                    val acceleration = sqrt(x * x + y * y + z * z)
                    val accelerationThreshold = 3.0f

                    val textViewSensor = activityMainBinding.textViewSensor
                    val textViewAcceleration = activityMainBinding.textViewAcceleration
                    textViewSensor.setText("X: %.1f, Y: %.1f, Z: %.1f".format(x,y,z))
                    textViewAcceleration.setText("a = : %.1f".format(acceleration))


                    //TODO: Compare with baseline and threshold
                    // -20 to -30 m/s2
                    // maybe avg_acceleration
                    //if (abs(acceleration - baselineAcceleration) > crashThreshold) {
                        // Potential crash detected!
                        // TODO: Implement crash handling logic here
                        // TODO: Sending a crash report to a server
                    //}
                    if (acceleration > accelerationThreshold) {
                        Toast.makeText(getApplicationContext(), "acceleration exceeds threshold!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    // Handle accuracy changes if needed
                    // Toast.makeText(getApplicationContext(), "Sensor accuracy changed: $accuracy", Toast.LENGTH_SHORT).show()
                }
            }
        }


        sensorManager.registerListener(
            sensorEventListener,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }


    // Unregister the listener when your activity pauses
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }


}
