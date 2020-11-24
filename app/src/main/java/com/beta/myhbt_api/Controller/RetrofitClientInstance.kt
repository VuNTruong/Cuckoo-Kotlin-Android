package com.beta.myhbt_api.Controller

import android.content.Context
import com.beta.myhbt_api.Utils.AddCookiesInterceptor
import com.beta.myhbt_api.Utils.ReceivedCookiesInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientInstance {
    companion object {
        var retrofit: Retrofit? = null
        private const val apiURL = "https://myhbt-api.herokuapp.com"
        //private const val apiURL = "https://myhbt-api.azurewebsites.net"
        //private const val apiURL = "http://10.0.2.2:3000"

        fun getRetrofitInstance(applicationContext: Context): Retrofit? {
            // Create the cookie interceptor
            val cookiesInterceptor = ReceivedCookiesInterceptor(applicationContext)

            // Create the add cookies interceptor
            val addCookiesInterceptor = AddCookiesInterceptor(applicationContext)

            // Create the http client
            var client: OkHttpClient

            // Create the builder
            val builder = OkHttpClient.Builder()

            // Add the interceptors
            builder.addInterceptor(cookiesInterceptor).addInterceptor(addCookiesInterceptor)

            // Build the the http client
            client = builder.build()

            // Build the retrofit client instance
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .client(client)
                    .baseUrl(apiURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            // Return the retrofit client instance
            return retrofit
        }
    }
}