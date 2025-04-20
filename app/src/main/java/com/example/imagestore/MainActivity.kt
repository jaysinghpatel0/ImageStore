package com.example.imagestore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkPermissions()) {
            captureImage()
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION), 100)
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, 101)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir("Images")
        val file = File.createTempFile("IMG_$timeStamp", ".jpg", storageDir)
        currentPhotoPath = file.absolutePath
        return file
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            val locationHelper = LocationHelper(this)
            locationHelper.getLocation { lat, lon ->

                Log.d("Location", "Lat:$lat Lon:$lon")

                // Upload Image to FTP
                Thread {
                    FtpUploader.uploadImageToFtp(currentPhotoPath)

                    // Device Info
                    val deviceType = "Tab"
                    val deviceID = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
                    val ipAddress = NetworkUtil.getIPAddress(this)

                    // Send Data to API
                    ApiService.updateDatabase(
                        currentPhotoPath.substringAfterLast("/"),
                        lat,
                        lon,
                        deviceType,
                        deviceID,
                        ipAddress
                    )
                }.start()
            }
        }
    }
}
