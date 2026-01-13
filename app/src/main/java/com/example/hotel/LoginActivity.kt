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
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var etLoginEmail: TextInputEditText
    private lateinit var etLoginPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvGoToSignup: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // ✅ Check if user already logged in
        if (auth.currentUser != null) {
            val userId = auth.currentUser!!.uid
            database = FirebaseDatabase
                .getInstance("https://hotel-80d8d-default-rtdb.firebaseio.com/")
                .reference.child("users")

            // Fetch user data and go directly to FindRoomActivity
            database.child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val fullName = snapshot.child("fullName").value.toString()
                    val email = snapshot.child("email").value.toString()
                    navigateToFindRoom(fullName, email)
                } else {
                    startActivity(Intent(this, Signup::class.java))
                    finish()
                }
            }.addOnFailureListener {
                startActivity(Intent(this, Signup::class.java))
                finish()
            }
            return
        }

        setContentView(R.layout.activity_login)

        database = FirebaseDatabase
            .getInstance("https://hotel-80d8d-default-rtdb.firebaseio.com/")
            .reference.child("users")

        initViews()
        btnLogin.setOnClickListener { validateAndLogin() }
        tvGoToSignup.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
        }
    }

    private fun initViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToSignup = findViewById(R.id.tvGoToSignup)
    }

    private fun validateAndLogin() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                etLoginEmail.error = "Please enter your email"
                etLoginEmail.requestFocus()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etLoginEmail.error = "Please enter a valid email"
                etLoginEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                etLoginPassword.error = "Please enter your password"
                etLoginPassword.requestFocus()
                return
            }
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        fetchUserData(userId)
                    }
                } else {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun fetchUserData(userId: String) {
        database.child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val fullName = snapshot.child("fullName").value.toString()
                    val email = snapshot.child("email").value.toString()

                    Toast.makeText(this, "Welcome back, $fullName!", Toast.LENGTH_SHORT).show()
                    navigateToFindRoom(fullName, email)
                } else {
                    Toast.makeText(this, "User not found in database!", Toast.LENGTH_SHORT).show()
                }
                btnLogin.isEnabled = true
                btnLogin.text = "Login"
            }
            .addOnFailureListener {
                btnLogin.isEnabled = true
                btnLogin.text = "Login"
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
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
}
