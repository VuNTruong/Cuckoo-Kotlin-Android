package com.beta.cuckoo.Utils

import android.content.Context
import android.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AddCookiesInterceptor(context: Context) : Interceptor {
    // Context of the parent activity
    private val context = context

    // Instance of Firebase Authentication
    private val mAuth = FirebaseAuth.getInstance()

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the request
        val builder : Request.Builder = chain.request().newBuilder()

        // Get the shared preference (memory) instance
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        /*
        // Loop through all data in the shared preference
        for (item in preferences.all) {
            // Add cookie to the request header
            builder.addHeader("Cookie", item.value as String)
        }
         */

        builder.addHeader("Cookie", preferences.getString("jwt", "") as String)
        builder.addHeader("Cookie", preferences.getString("sign_up_jwt", "") as String)
        builder.addHeader("Cookie", "idToken=${preferences.getString("idToken", "") as String}")

        // Add user' Firebase ID token to device's memory
        if (mAuth.currentUser != null) {
            builder.addHeader("Cookie", "firebaseUID=${mAuth.currentUser!!.uid}")
        } else {
            builder.addHeader("Cookie", "firebaseUID=noToken")
        }

        // Return the request with cookies
        return chain.proceed(builder.build())
    }
}