package com.beta.myhbt_api.View

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.beta.myhbt_api.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_label_image.*

class LabelImage : AppCompatActivity() {
    // Image Uri of the selected image
    private var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_image)

        // Select photo button
        selectImageToLabelButton.setOnClickListener {
            fileChooser()
        }

        // Label photo button
        labelSelectedImage.setOnClickListener {
            labelImage()
        }
    }

    // The function to label image
    fun labelImage () {
        // Image labeler (powered by FirebaseVision)
        val labeler = FirebaseVision.getInstance().visionCloudLabelDetector

        // The function to convert image uri into bitmap
        fun convertToBitmap (imageURI: Uri) : Bitmap {
            return MediaStore.Images.Media.getBitmap(this.contentResolver, imageURI)
        }

        // Convert the image to bitmap
        val bitmap = convertToBitmap(imageURI!!)

        // Create the firebase vision image object which can be used to label by the labeler
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // Start labeling the image
        labeler.detectInImage(image)
            .addOnSuccessListener {labels ->
                val labels = labels
                print(labels)

                // Loop through list of labels and add them to the database
                for (label in labels) {
                    // Get info of the labeled image
                    val text = label.label
                }
            }.addOnFailureListener {
                print("Task failed")
            }
    }

    // The function to open file chooser to get the image
    private fun fileChooser() {
        // Create the new intent in order to come to the file chooser
        val intent = Intent()

        // Set the intent to just pick the image
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        // Star the activity to get the image
        startActivityForResult(intent, 0)
    }

    // The function to load chosen image into the image view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageURI = data.data
            imageViewToLabel.setImageURI(imageURI)
        }
    }
}
