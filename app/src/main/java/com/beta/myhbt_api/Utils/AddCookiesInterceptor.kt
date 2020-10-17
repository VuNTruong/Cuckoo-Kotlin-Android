package com.beta.myhbt_api.Utils

import android.content.Context
import android.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AddCookiesInterceptor(context: Context) : Interceptor {
    // Context of the parent activity
    private val context = context

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

        // Return the request with cookies
        return chain.proceed(builder.build())
    }
}