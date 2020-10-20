package com.beta.myhbt_api.View

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import retrofit2.Call
import com.beta.myhbt_api.Controller.GetAllowedUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import kotlinx.android.synthetic.main.activity_sign_up_enter_student_id.*
import kotlinx.android.synthetic.main.activity_sign_up_verify_info.*
import retrofit2.Callback
import retrofit2.Response

class SignUpVerifyInfo : AppCompatActivity() {
    // Sign up token from the previous activity
    private var signUpToken : String ?= null

    // Student id from the previous activity
    private var studentId : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_verify_info)

        // Get sign up token from the previous activity
        signUpToken = intent.getStringExtra("signUpToken")

        // Get student id from the previous activity
        studentId = intent.getStringExtra("studentId")

        // Execute the AsyncTask to get info of the allowed user based on student id
        GetInfoOfAllowedUser().execute(hashMapOf(
            "studentId" to studentId!!,
            "fullNameTextView" to userFullNameVerify,
            "classCodeTextView" to userClassCodeVerify,
            "signUpToken" to signUpToken!!
        ))

        // Set on click listener for the go to step 3 registration button
        gotoStep3SignUpButton.setOnClickListener {
            // Create intent to go to the next step
            val intent = Intent(applicationContext, SignUpCreateEmailAndPassword::class.java)

            // Put the sign up token inside the intent so that next activity will have the token in order to continue
            intent.putExtra("signUpToken", signUpToken)

            // Put student id inside the intent as well
            intent.putExtra("studentId", studentId)

            // Go to the third step of registration
            startActivity(intent)
        }
    }

    // AsyncTask to get info of the allowed user based on student id
    inner class GetInfoOfAllowedUser : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the student id
            val studentId = params[0]!!["studentId"] as String

            // Get the full name text view
            val fullNameTextView = params[0]!!["fullNameTextView"] as TextView

            // Get the class code text view
            val classCodeTextView = params[0]!!["classCodeTextView"] as TextView

            // Create the get sign up token service
            val getAllowedUserInfoBasedOnIdService: GetAllowedUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
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
                    // Get first name
                    val firstName = userInfo["firstName"] as String
                    val middleName = userInfo["middleName"] as String
                    val lastName = userInfo["lastName"] as String
                    // Combine them all to get the full name
                    val fullName = "$lastName $middleName $firstName"

                    // Get class code of the user
                    val classCode = userInfo["classCode"] as String

                    // Load full name into the TextView
                    fullNameTextView.text = fullName

                    // Load class code into the TextView
                    classCodeTextView.text = classCode
                }
            })

            return null
        }
    }
}
