package com.beta.myhbt_api.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.beta.myhbt_api.Controller.GetSignUpTokenService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import kotlinx.android.synthetic.main.activity_sign_up_enter_student_id.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpEnterStudentId : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_enter_student_id)

        // Set on click listener for the next button
        gotoStep2SignUpButton.setOnClickListener {
            // Call the function to verify student id
            verifyStudentId(studentIdEditText.text.toString())
        }
    }

    // The function to perform the verify student id and get sign up token procedure
    private fun verifyStudentId (studentId: String) {
        // Create the get sign up token service
        val getSignUpTokenService: GetSignUpTokenService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetSignUpTokenService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = getSignUpTokenService.getSignUpToken(studentId)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that the user may didn't enter the correct email or password
                if (response.body() == null) {
                    // Show the user that the login was not successful
                    Toast.makeText(applicationContext, "Student id is not right", Toast.LENGTH_SHORT).show()
                } else {
                    // Get body of the response
                    val body = response.body() as Map<String, Any>

                    // Get token from the response body
                    val signUpToken = body["token"] as String

                    // Go to the second step of registration
                    val intent = Intent(applicationContext, SignUpVerifyInfo::class.java)

                    // Put the sign up token inside the intent so that next activity will have the token in order to continue
                    intent.putExtra("signUpToken", signUpToken)

                    // Also put student id inside the intent so that next activity will know which user to show info of
                    intent.putExtra("studentId", studentId)

                    // Start the second step
                    startActivity(intent)
                }
            }
        })
    }
}
