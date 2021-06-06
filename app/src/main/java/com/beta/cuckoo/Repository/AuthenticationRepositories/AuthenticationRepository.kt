package com.beta.cuckoo.Repository.AuthenticationRepositories

import android.content.Context
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class AuthenticationRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // Instance of FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    // The function to update password of the user
    fun updatePassword (emailConfirmChangePassword: String, passwordConfirmChangePassword: String, newPassword: String, passwordConfirm: String, callback: (isUpdated: Boolean, errorMessage: String) -> Unit) {
        // Check to see if password and password confirm matches or not
        if (newPassword != passwordConfirm) {
            // If they are different, let the view know that password was not updated via callback function
            // and also notify the view of the error
            callback(false, "Password does not match")

            // Get out of the function
            return
        }

        executor.execute {
            // Call the function to re authenticate user
            mAuth.currentUser!!.reauthenticate(EmailAuthProvider.getCredential(emailConfirmChangePassword, passwordConfirmChangePassword))
                .addOnCompleteListener {
                    // Call the function to update password
                    mAuth.currentUser!!.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Let the view know that password was updated via callback function
                                callback(true, "")
                            } else {
                                // Let the view know that password was not updated
                                callback(false, "Something wrong, please try again")
                            }
                        }.addOnFailureListener {exception ->
                            // Let the view know that password was not updated via callback function
                            callback(false, exception.localizedMessage!!)
                        }
                }
                .addOnFailureListener {
                    // Let the view know that password was not updated via callback function
                    callback(false, "Wrong login credentials entered")
                }
        }
    }

    // The function to send password reset email to the user
    fun sendPasswordResetEmail (email: String, callback: (isSent: Boolean, errorMessage: String) -> Unit) {
        executor.execute {
            // Call the function to send password reset email to the user
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Let the view know that password reset email was sent via callback function
                        callback(true, "")
                    } else {
                        // Let the view know that password reset email was not sent via callback function
                        callback(false, "Something wrong, please try again")
                    }
                }
                .addOnFailureListener {exception ->
                    // Let the view know that password reset email was not sent via callback function
                    callback(false, exception.localizedMessage!!)
                }
        }
    }

    // The function to update user's email
    fun updateEmail (emailConfirmChangeEmail: String, passwordConfirmChangeEmail: String, email: String, callback: (isUpdated: Boolean, errorMessage: String) -> Unit) {
        executor.execute {
            // Call the function to re authenticate user
            mAuth.currentUser!!.reauthenticate(EmailAuthProvider.getCredential(emailConfirmChangeEmail, passwordConfirmChangeEmail))
                .addOnCompleteListener {
                    // Call the function to update email for the user
                    mAuth.currentUser!!.updateEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Let the view know that email was updated via callback function
                                callback(true, "")
                            } else {
                                // Let the view know that email was not updated via callback function
                                callback(false, "Something wrong, please try again")
                            }
                        }
                        .addOnFailureListener {exception ->
                            // Let the view know that email was not updated via callback function
                            callback(false, exception.localizedMessage!!)
                        }
                }
                .addOnFailureListener {
                    // Let the view know that email was not updated via callback function
                    callback(false, "Wrong login credentials entered")
                }
        }
    }
}