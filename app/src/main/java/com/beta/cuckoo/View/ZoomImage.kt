package com.beta.cuckoo.View

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.beta.cuckoo.R
import com.beta.cuckoo.Utils.AdditionalAssets
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_zoom_image.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ZoomImage : AppCompatActivity() {
    // Image bitmap of the image to be saved
    private lateinit var imageBitmapToBeSaved: Bitmap

    // Image URL of the image to load at this activity
    private lateinit var imageURLToLoad: String

    // The variable which will keep track of if the image option layout is being shown or not
    private var isShowingImageOptionLayout = false

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

        // Call the function to request for permission to download image
        requestPermissionForExternalStorage()

        // Load image into zoomable image view
        Glide.with(applicationContext)
            .load(imageURLToLoad)
            .listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Update the image bitmap to be saved
                    imageBitmapToBeSaved = resource!!.toBitmap()
                    return false
                }

            })
            .into(largeImage)

        // Hide the image option layout initially
        //imageOptionLayout.visibility = View.INVISIBLE

        // Set on click listener for the image option layout
        imageOptionLayout.setOnClickListener {
            // Call the function to toggle the layout
            toggleShowAndHideImageOptionLayout()
        }

        // Set on click listener for the download button
        downloadImageButton.setOnClickListener {
            // Call the function to start downloading
            downloadImage()
        }
    }

    // The function to start downloading image into device's external storage
    private fun downloadImage () {
        // Check for permission
        if (!verifyPermission()) {
            return
        }

        // Generate file name
        val fileName = additionalAssets.generateRandomString(10)

        Toast.makeText(applicationContext, "Saving image...", Toast.LENGTH_SHORT).show()
        saveImage(imageBitmapToBeSaved, fileName)
    }

    private fun saveImage(image: Bitmap, imageName: String): String? {
        var savedImagePath: String? = null
        val imageFileName = "JPEG_$imageName.jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Cuckoo"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
                Toast.makeText(applicationContext, "Image Saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error while saving image!", Toast.LENGTH_SHORT).show()
            }

            // Add the image to the system gallery
            galleryAddPic(savedImagePath)
        }
        return savedImagePath
    }

    private fun galleryAddPic(imagePath: String?) {
        imagePath?.let { path ->
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f = File(path)
            val contentUri: Uri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            sendBroadcast(mediaScanIntent)
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

    // The function which will toggle show and hide image option layout
    private fun toggleShowAndHideImageOptionLayout () {
        // Toggle the boolean which keep track of show or hide image option layout
        isShowingImageOptionLayout = !isShowingImageOptionLayout

        // Based on the toggled boolean to determine if the layout should be shown or not
        if (isShowingImageOptionLayout) {
            downloadImageButton.visibility = View.VISIBLE
        } else {
            downloadImageButton.visibility = View.INVISIBLE
        }
    }
}