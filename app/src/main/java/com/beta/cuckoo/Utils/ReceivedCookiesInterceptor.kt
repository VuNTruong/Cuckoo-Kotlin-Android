package com.beta.cuckoo.Utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response

class ReceivedCookiesInterceptor (context: Context): Interceptor {
    // Context of the parent activity
    private val context = context

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get access to the shared preference
        val memory : SharedPreferences.Editor = PreferenceManager.getDefaultSharedPreferences(context).edit()

        // The original response from server
        val originalResponse = chain.proceed((chain.request()))

        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            for (header in originalResponse.headers("Set-Cookie")) {
                // Read cookie info from the response header
                val cookie = header.toString()

                // Split the cookie string into parts in order to get name of the cookie
                // Also get name of the cookie
                val cookieName = cookie.split("=")[0]

                // Put that cookie info into the memory
                memory.putString(cookieName, cookie).apply()
                memory.commit()
            }
        }

        // Return the response
        return originalResponse
    }
}