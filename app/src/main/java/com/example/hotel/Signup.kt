package com.example.hotel

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etMobile: TextInputEditText
    private lateinit var btnSignUp: MaterialButton
    private lateinit var tvGoToLogin: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase
            .getInstance("https://hotel-80d8d-default-rtdb.firebaseio.com/")
            .reference

        // Initialize views
        initViews()

        // Set click listener for Sign Up
        btnSignUp.setOnClickListener {
            validateAndSignUp()
        }

        // Navigate to Login screen
        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etpassword)
        etMobile = findViewById(R.id.etMobile)
        btnSignUp = findViewById(R.id.btnSendOtp)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)
    }

    private fun validateAndSignUp() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val mobile = etMobile.text.toString().trim()

        when {
            fullName.isEmpty() -> {
                etFullName.error = "Please enter your full name"
                etFullName.requestFocus()
                return
            }
            email.isEmpty() -> {
                etEmail.error = "Please enter your email"
                etEmail.requestFocus()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Please enter a valid email"
                etEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                etPassword.error = "Please enter a password"
                etPassword.requestFocus()
                return
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return
            }
            mobile.isEmpty() -> {
                etMobile.error = "Please enter your mobile number"
                etMobile.requestFocus()
                return
            }
            mobile.length < 10 -> {
                etMobile.error = "Please enter a valid mobile number"
                etMobile.requestFocus()
                return
            }
        }

        signUpUser(fullName, email, password, mobile)
    }

    private fun signUpUser(fullName: String, email: String, password: String, mobile: String) {
        btnSignUp.isEnabled = false
        btnSignUp.text = "Signing Up..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToDatabase(userId, fullName, email, mobile)
                    }
                } else {
                    btnSignUp.isEnabled = true
                    btnSignUp.text = "Sign Up"
                    Toast.makeText(
                        this,
                        "Sign up failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, fullName: String, email: String, mobile: String) {
        val user = User(
            userId = userId,
            fullName = fullName,
            email = email,
            mobile = mobile,
            timestamp = System.currentTimeMillis()
        )

        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                btnSignUp.isEnabled = true
                btnSignUp.text = "Sign Up"
                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()

                navigateToFindRoom(fullName, email)
            }
            .addOnFailureListener { exception ->
                btnSignUp.isEnabled = true
                btnSignUp.text = "Sign Up"
                Toast.makeText(
                    this,
                    "Failed to save user data: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun navigateToFindRoom(fullName: String, email: String) {
        val intent = Intent(this, FindRoomActivity::class.java)
        intent.putExtra("USER_NAME", fullName)
        intent.putExtra("USER_EMAIL", email)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    data class User(
        val userId: String = "",
        val fullName: String = "",
        val email: String = "",
        val mobile: String = "",
        val timestamp: Long = 0
    )
}
