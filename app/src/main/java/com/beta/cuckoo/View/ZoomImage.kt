package com.beta.cuckoo.View

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.beta.cuckoo.R
import com.beta.cuckoo.Utils.AdditionalAssets
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_zoom_image.*
import java.io.File
import java.io.FileOutputStream


class ZoomImage : AppCompatActivity() {
    // Image URL of the image to load at this activity
    private lateinit var imageURLToLoad: String

    // Additional assets
    private lateinit var additionalAssets: AdditionalAssets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom_image)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate additional assets
        additionalAssets = AdditionalAssets(applicationContext)

        // Get image URL to load from the previous activity
        imageURLToLoad = intent.getStringExtra("imageURLToLoad")!!

        // Load image into zoomable image view
        Glide.with(applicationContext)
            .load(imageURLToLoad)
            .into(largeImage)
    }

    // The function to start downloading image into device's external storage
    fun downloadImage (imageURL: String) {
        // Check for permission
        if (!verifyPermission()) {
            return
        }

        // Get the directory path
        val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/" + getString(R.string.app_name) + "/"

        // Create the file object based on file path
        val dir = File(dirPath)

        // Generate file name
        val fileName = additionalAssets.generateRandomString(10)

        // Load image into zoomable image view
        Glide.with(applicationContext)
            .load(imageURLToLoad)
            .into(object: Target<Drawable> {
                override fun onLoadStarted(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    TODO("Not yet implemented")
                }

                override fun getSize(cb: SizeReadyCallback) {
                    TODO("Not yet implemented")
                }

                override fun getRequest(): Request? {
                    TODO("Not yet implemented")
                }

                override fun onStop() {
                    TODO("Not yet implemented")
                }

                override fun setRequest(request: Request?) {
                    TODO("Not yet implemented")
                }

                override fun removeCallback(cb: SizeReadyCallback) {
                    TODO("Not yet implemented")
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }

                override fun onStart() {
                    TODO("Not yet implemented")
                }

                override fun onDestroy() {
                    TODO("Not yet implemented")
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    val bitmap = resource.toBitmap()
                    Toast.makeText(applicationContext, "Saving image...", Toast.LENGTH_SHORT).show()
                    saveImage(bitmap, dir, fileName)
                }

            })
    }

    fun saveImage (image: Bitmap, storageDir: File, imageFileName: String) {
        var successDirCreated = false
        if (!storageDir.exists()) {
            successDirCreated = storageDir.mkdir()
        }
        if (successDirCreated) {
            val imageFile = File(storageDir, imageFileName)
            val savedImagePath = imageFile.absolutePath
            try {
                val fOut = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
                Toast.makeText(applicationContext, "Image Saved!", Toast.LENGTH_SHORT).show()
            } catch (exception: Exception) {
                print(exception.stackTrace)
                Toast.makeText(applicationContext, "Error while saving image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //************************************************** REQUEST PERMISSION **************************************************
    // The function to request for permission to use external storage
    private fun requestPermissionForExternalStorage() {
        // If user need explanation on why the app need to get access to external storage
        // explain it to the user
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "We need your permission to use microphone and camera", Toast.LENGTH_LONG).show()
        } // If not, start asking for permission
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    // Handle user response to permission
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == 1) {
            var externalFileAccessPermissionGranted = true

            for (grantResult in grantResults) {
                externalFileAccessPermissionGranted = externalFileAccessPermissionGranted and
                        (grantResult == PackageManager.PERMISSION_GRANTED)
            }

            if (!externalFileAccessPermissionGranted) {
                Toast.makeText(this, "We need your permission to access external storage", Toast.LENGTH_LONG).show()
            }
        }
    }

    // The function to check if permission is granted
    private fun verifyPermission () : Boolean {
        // This will return the current status
        val permissionExternalMemory = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // Check if permission is granted or not
        return if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            // Call the function to request for permission to use the storage
            requestPermissionForExternalStorage()

            // Return false
            false
        } else {
            // Return true
            true
        }
    }
    //************************************************** END REQUEST PERMISSION **************************************************
}