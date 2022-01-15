package com.example.foregroundservice

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class ForeGroundService : Service() {
   private lateinit var scope : Job


    // fused location
    private lateinit var fusedLocation : FusedLocationProviderClient
    // locationCallBack
    private lateinit var locationCallBack : LocationCallback

    override fun onBind(intent: Intent): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val check : Boolean = intent?.getStringExtra("settingCheck").toBoolean()

            Log.i("log", "onStartCommand")
            scope = GlobalScope.launch {

                val locationRequest = LocationRequest.create().apply {
                    interval = 4000
                    fastestInterval = 2000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                fusedLocation =
                    LocationServices.getFusedLocationProviderClient(this@ForeGroundService)

                locationCallBack = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        super.onLocationResult(p0)
                        for (location in p0.locations) {
                            Log.i("log", "location ................... $location")
                            Toast.makeText(
                                applicationContext,
                                "location ............ $location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                if ((ActivityCompat.checkSelfPermission(
                        this@ForeGroundService,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                        this@ForeGroundService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED)
                ) {
                    Log.i("log", "request location")
                     fusedLocation.requestLocationUpdates(
                        locationRequest,
                        locationCallBack,
                        Looper.getMainLooper()
                    )
                }
            }
            val notificationChannel =
                NotificationChannel("channelId", "channelId", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            val notificationBuilder = NotificationCompat.Builder(this, "channelId")
                .setContentTitle("ForeGround service ")
                .setContentText("foreGround service notification")
                .setSmallIcon(R.drawable.ic_launcher_background)

            startForeground(101, notificationBuilder.build())

//        Toast.makeText(applicationContext,"cant get the location Request",Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    private fun stopRequestLocation(){
        val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        fusedLocation.removeLocationUpdates(locationCallBack)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRequestLocation()
        scope.cancel()
        Toast.makeText(applicationContext,"service is Stopped ",Toast.LENGTH_SHORT).show()
    }
}