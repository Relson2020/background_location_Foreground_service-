package com.example.foregroundservice

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.foregroundservice.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class MainActivity : AppCompatActivity() {

    private val locationRequest = LocationRequest.create().apply {
        interval = 4000
        fastestInterval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var check = false

    private lateinit var binding : ActivityMainBinding

    private val permissionId  = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)


//        locationRequestSetting()
        //Toast.makeText(this,"$check",Toast.LENGTH_SHORT).show()

        // service intent
        val intent  =  Intent(this,ForeGroundService::class.java)
        intent.putExtra("settingCheck",check)
        binding.startForeGround.setOnClickListener {

            startForegroundService(intent)
        }

        // stop service
        binding.stopForeGround.setOnClickListener {
            stopService(intent)
        }
        // permission
        binding.permissionButton.setOnClickListener{
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
               Toast.makeText(this,"permission granted",Toast.LENGTH_SHORT).show()
            } else {
                getLocationPermission()
            }
        }
    }

    // location permission request
    private fun getLocationPermission() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION,
            )) && (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        ) {

            AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("hey accept the permission to access location")
                .setPositiveButton("ok", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity, arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), permissionId
                        )
                    }
                })
                .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), permissionId
            )
        }
    }

    private fun locationRequestSetting() {
        val locationSettingRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val clientSetting = LocationServices.getSettingsClient(this)
        val task = clientSetting.checkLocationSettings(locationSettingRequest.build())

        Log.i("log", "location setting")

        task.addOnSuccessListener {
           check = true
        }

        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                Log.i("log", "exception")
                try {
                    it.startResolutionForResult(this@MainActivity, 101)
                } catch (e: IntentSender.SendIntentException) {
                }
            }
        }
    }
}