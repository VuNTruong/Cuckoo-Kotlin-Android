package com.beta.myhbt_api.View

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beta.myhbt_api.Controller.GetAllowedUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.SignUpService
import com.beta.myhbt_api.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up_create_email_and_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpCreateEmailAndPassword : AppCompatActivity() {
    // Sign up token from the previous activity
    var signUpToken = ""

    // Student id from the previous activity
    var studentId = ""

    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_create_email_and_password)

        // Get sign up token from the previous activity
        signUpToken = intent.getStringExtra("signUpToken")!!

        // Get student id from the previous activity
        studentId = intent.getStringExtra("studentId")!!

        // Set on click listener for done sign up button
        doneRegistrationButton.setOnClickListener {
            // Execute the AsyncTask to submit the registration form
            GetInfoOfAllowedUserAndRegister().execute(hashMapOf(
                "studentId" to studentId,
                "signUpToken" to signUpToken,
                "email" to enterEmailRegistration.text.toString(),
                "password" to enterPasswordRegistration.text.toString(),
                "passwordConfirm" to confirmPasswordRegistration.text.toString()
            ))
        }
    }

    // AsyncTask to get info of the allowed user based on student id
    inner class GetInfoOfAllowedUserAndRegister : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the student id
            val studentId = params[0]!!["studentId"] as String

            // Get the sign up token
            val signUpToken = params[0]!!["signUpToken"] as String

            // Get email of the user
            val email = params[0]!!["email"] as String

            // Get password of the user
            val password = params[0]!!["password"] as String

            // Get password confirm of the user
            val passwordConfirm = params[0]!!["passwordConfirm"] as String

            // Create the get allowed user info
            val getAllowedUserInfoBasedOnIdService : GetAllowedUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                GetAllowedUserInfoBasedOnIdService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = getAllowedUserInfoBasedOnIdService.getAllowedUserInfoBasedOnId(studentId)

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Get body of the response
                    val responseBody = response.body()
                    print(responseBody)
                    val body = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = body["data"] as Map<String, Any>

                    // Get user info from the data
                    val userInfo = (data["documents"] as List<Map<String, Any>>)[0]

                    // Get name of the user
                    val firstName = userInfo["firstName"] as String
                    val middleName = userInfo["middleName"] as String
                    val lastName = userInfo["lastName"] as String

                    // Get class code of the user
                    val classCode = userInfo["classCode"] as String

                    // Get role of the user
                    val role = userInfo["role"] as String

                    // Execute the AsyncTask to perform the sign up operation
                    SignUpTask().execute(hashMapOf(
                        "signUpToken" to signUpToken,
                        "email" to email,
                        "password" to password,
                        "passwordConfirm" to passwordConfirm,
                        "firstName" to firstName,
                        "middleName" to middleName,
                        "lastName" to lastName,
                        "classCode" to classCode,
                        "role" to role,
                        "studentId" to studentId
                    ))
                }
            })

            return null
        }
    }

    // AsyncTask to perform the sign up operation
    inner class SignUpTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the sign up token
            val signUpToken = params[0]!!["signUpToken"] as String

            // Get student id
            val studentId = params[0]!!["studentId"] as String

            // Get email of the user
            val email = params[0]!!["email"] as String

            // Get password of the user
            val password = params[0]!!["password"] as String

            // Get password confirm of the user
            val passwordConfirm = params[0]!!["passwordConfirm"] as String

            // Get name of the user
            val firstName = params[0]!!["firstName"] as String
            val lastName = params[0]!!["lastName"] as String
            val middleName = params[0]!!["middleName"] as String

            // Get class code of the user
            val classCode = params[0]!!["classCode"] as String

            // Get role of the user
            val role = params[0]!!["role"] as String

            // Create the sign up service
            val signUpService : SignUpService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                SignUpService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = signUpService.signUp(email, password, passwordConfirm, firstName, middleName, lastName, role, classCode, "avatar", "cover", studentId, signUpToken)

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Get body of the response
                    val body = response.body()

                    if (body != null) {
                        // Call the function to sign the user in using FirebaseAuth
                        SignInWithFirebaseTask().execute()

                        // Go to the Welcome activity
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)

                        // Finish this activity
                        finish()
                    }
                }
            })

            return null
        }
    }

    // The AsyncTask to sign user in with FirebaseAuth and provide FirebaseAuth token in order to have access to the storage
    inner class SignInWithFirebaseTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            // Sign in with Firebase
            mAuth.signInWithEmailAndPassword("allowedusers@email.com", "AllowedUser")

            return null
        }
    }
}
