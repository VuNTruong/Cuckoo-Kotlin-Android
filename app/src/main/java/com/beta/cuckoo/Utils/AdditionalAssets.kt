package com.beta.cuckoo.Utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.firebase.storage.FirebaseStorage
import kotlin.math.floor

class AdditionalAssets  (context: Context) {
    // Storage reference
    private val storage = FirebaseStorage.getInstance()

    // Context of the parent activity
    private val context = context

    // The function to upload image to storage
    fun uploadImageToStorage (imageURI: Uri, referencePath: String, callback: (imageUploaded: Boolean, imageURL: String) -> Unit) {
        // Generate name for the image
        val imageName = generateRandomString(20)

        // Create the storage reference
        val storageReference = storage.getReference(referencePath)

        // Put name for the image
        val reference = storageReference.child("${imageName}.${getExtension(imageURI)}")

        // Start the upload task. This is the uploadTask which will be used to keep track of the upload process
        val uploadTask = reference.putFile(imageURI)

        // When uploading is done, get URL of that image
        uploadTask.addOnSuccessListener {
            // Get URL of the image that has just been uploaded to the storage
            reference.downloadUrl.addOnSuccessListener { uri ->
                // Let the view know that image was uploaded via callback function
                callback(true, uri.toString())
            }.addOnFailureListener {
                // Report error via callback function
                callback(false, "")
            }
        }
    }

    // The function to create a random string of 20 characters
    fun generateRandomString (length: Int): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var randomString = ""
        for (i in 0..length) {
            randomString += chars[floor(Math.random() * chars.length).toInt()]
        }
        return randomString
    }

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}