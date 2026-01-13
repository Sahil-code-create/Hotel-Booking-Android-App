package com.example.hotel

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        // Delay for 3 seconds, then check login state
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // ✅ User already logged in → Go to FindRoomActivity
                val intent = Intent(this, FindRoomActivity::class.java)
                startActivity(intent)
            } else {
                // 🚪 User not logged in → Go to SignupActivity
                val intent = Intent(this, Signup::class.java)
                startActivity(intent)
            }

            finish() // Close splash so user can’t return to it
        }, 3000)
    }
}
