package com.example.hotel

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlaceDetailsActivity : AppCompatActivity() {

    private lateinit var navHome: LinearLayout
    private lateinit var navWhere2Go: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_details)

        // ✅ Get data from intent
        val name = intent.getStringExtra("placeName")
        val desc = intent.getStringExtra("placeDescription")
        val imgRes = intent.getIntExtra("placeImage", 0)

        // ✅ Set views
        val placeImage = findViewById<ImageView>(R.id.placeDetailImage)
        val placeName = findViewById<TextView>(R.id.placeDetailName)
        val placeDesc = findViewById<TextView>(R.id.placeDetailDescription)

        placeName.text = name
        placeDesc.text = desc
        placeImage.setImageResource(imgRes)

        // ✅ Bottom navigation
        navHome = findViewById(R.id.navHome)
        navWhere2Go = findViewById(R.id.navWhere2Go)

        // Home → Go to FindRoomActivity
        navHome.setOnClickListener {
            val intent = Intent(this, FindRoomActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Where2Go → Go back to main PlacesActivity
        navWhere2Go.setOnClickListener {
            val intent = Intent(this, PlacesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
