package com.example.hotel

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlacesActivity : AppCompatActivity() {

    private lateinit var navHome: LinearLayout
    private lateinit var navWhere2Go: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        val layoutPlaces = findViewById<LinearLayout>(R.id.layoutPlacesList)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // ✅ Initialize bottom navigation views
        navHome = findViewById(R.id.navHome)
        navWhere2Go = findViewById(R.id.navWhere2Go)

        // ✅ Logout confirmation dialog
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // ✅ List of places
        val places = listOf(
            Place("Agra", R.drawable.taj_mahal, getString(R.string.agra_info)),
            Place("Bengaluru", R.drawable.bengaluru, getString(R.string.bengaluru_info)),
            Place("Chennai", R.drawable.chennai, getString(R.string.chennai_info)),
            Place("Delhi", R.drawable.delhi, getString(R.string.delhi_info)),
            Place("Mumbai", R.drawable.mumbai, getString(R.string.mumbai_info))
        )

        // ✅ Inflate each card dynamically
        for (place in places) {
            val card = layoutInflater.inflate(R.layout.item_place_card_vertical, layoutPlaces, false)
            val img = card.findViewById<ImageView>(R.id.placeImage)
            val name = card.findViewById<TextView>(R.id.placeName)

            img.setImageResource(place.imageRes)
            name.text = place.name

            card.setOnClickListener {
                val intent = Intent(this, PlaceDetailsActivity::class.java)
                intent.putExtra("placeName", place.name)
                intent.putExtra("placeImage", place.imageRes)
                intent.putExtra("placeDescription", place.description)
                startActivity(intent)
            }

            layoutPlaces.addView(card)
        }

        // ✅ Bottom navigation actions
        navHome.setOnClickListener {
            val intent = Intent(this, FindRoomActivity::class.java)
            startActivity(intent)
        }

        navWhere2Go.setOnClickListener {
            Toast.makeText(this, "You are already on Where2Go Page", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Logout confirmation dialog method
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // ✅ Properly sign out the user
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

                // ✅ Redirect to LoginActivity (not Signup)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }


    data class Place(val name: String, val imageRes: Int, val description: String)
}
