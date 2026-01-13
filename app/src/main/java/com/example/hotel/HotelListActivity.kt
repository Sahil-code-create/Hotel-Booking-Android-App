package com.example.hotel

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView

data class Hotel(
    val name: String,
    val imageResId: Int,
    val priceRange: String,
    val isAvailable: Boolean
)

class HotelListActivity : AppCompatActivity() {

    private lateinit var navHome: LinearLayout
    private lateinit var navWhere2Go: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_list)

        val cityName = intent.getStringExtra("city_name") ?: "Unknown City"
        title = "Hotels in $cityName"

        val layoutHotels = findViewById<LinearLayout>(R.id.layoutHotels)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // ✅ Find bottom nav views properly (was missing)
        navHome = findViewById(R.id.navHome)
        navWhere2Go = findViewById(R.id.navWhere2Go)

        val hotels = when (cityName) {
            "Agra" -> listOf(
                Hotel("Fairfield Hotel, Agra", R.drawable.fairfield, "₹4,999 to ₹9,999", true),
                Hotel("Oberoi Hotel, Agra", R.drawable.oberoi, "", false),
                Hotel("Taj Hotel, Agra", R.drawable.taj, "₹6,999 to ₹12,000", true)
            )
            "Bengaluru" -> listOf(
                Hotel("Taj MG Road", R.drawable.tajmgroad, "₹6,999 to ₹12,000", true),
                Hotel("ITC Gardenia", R.drawable.itcgardenia, "", false)
            )
            "Chennai" -> listOf(
                Hotel("The Leela Palace", R.drawable.leela_chennai, "₹7,500 to ₹13,500", true),
                Hotel("ITC Grand Chola", R.drawable.itc_chola, "₹4,999 to ₹9,999", true)
            )
            "Mumbai" -> listOf(
                Hotel("Trident Nariman Point", R.drawable.trident, "₹9,999 to ₹15,000", true),
                Hotel("Taj Lands End", R.drawable.taj_lands, "", false)
            )
            "Delhi" -> listOf(
                Hotel("The Oberoi, New Delhi", R.drawable.oberoi_delhi, "₹10,000 to ₹18,000", true),
                Hotel("The Leela Palace, Delhi", R.drawable.leela_delhi, "", false)
            )
            else -> emptyList()
        }

        val checkIn = intent.getStringExtra("check_in") ?: ""
        val checkOut = intent.getStringExtra("check_out") ?: ""

        // ✅ Logout Button Click
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // ✅ Inflate each hotel card
        for (hotel in hotels) {
            val view = layoutInflater.inflate(R.layout.item_hotel_card, layoutHotels, false)
            val image = view.findViewById<ShapeableImageView>(R.id.hotelImage)
            val name = view.findViewById<TextView>(R.id.hotelName)
            val price = view.findViewById<TextView>(R.id.hotelPrice)

            image.setImageResource(hotel.imageResId)
            name.text = hotel.name

            if (hotel.isAvailable) {
                price.text = hotel.priceRange
                price.setBackgroundResource(R.drawable.price_available_bg)

                view.setOnClickListener {
                    val intent = Intent(this, SelectRoomActivity::class.java)
                    intent.putExtra("hotel_name", hotel.name)
                    intent.putExtra("hotel_image", hotel.imageResId)
                    intent.putExtra("hotel_price", hotel.priceRange)
                    intent.putExtra("check_in", checkIn)
                    intent.putExtra("check_out", checkOut)
                    intent.putExtra("city_name", cityName)

                    when (hotel.name) {
                        "Fairfield Hotel, Agra" -> {
                            intent.putExtra("hotel_rating", "⭐⭐⭐⭐ 4.3")
                            intent.putExtra("hotel_about", "Located near the Taj Mahal, this hotel offers elegant rooms, a pool, and fine dining.")
                            intent.putExtra("hotel_amenities", arrayListOf("Gym", "Free Parking", "Restaurant", "Pool", "Wi-Fi"))
                        }
                        "Taj Hotel, Agra" -> {
                            intent.putExtra("hotel_rating", "⭐⭐⭐⭐⭐ 4.7")
                            intent.putExtra("hotel_about", "Experience luxury and comfort with Taj’s signature hospitality and stunning views.")
                            intent.putExtra("hotel_amenities", arrayListOf("Wi-Fi", "Pool", "Restaurant", "Spa", "Bar"))
                        }
                        "Taj MG Road" -> {
                            intent.putExtra("hotel_rating", "⭐⭐⭐⭐⭐ 4.8")
                            intent.putExtra("hotel_about", "A premium stay in the heart of Bengaluru with rooftop dining and excellent service.")
                            intent.putExtra("hotel_amenities", arrayListOf("Gym", "Spa", "Wi-Fi", "Restaurant"))
                        }
                        "ITC Grand Chola" -> {
                            intent.putExtra("hotel_rating", "⭐⭐⭐⭐⭐ 4.6")
                            intent.putExtra("hotel_about", "Majestic architecture with royal interiors and world-class amenities.")
                            intent.putExtra("hotel_amenities", arrayListOf("Wi-Fi", "Pool", "Restaurant", "Bar", "Spa"))
                        }
                        "The Leela Palace" -> {
                            intent.putExtra("hotel_rating", "⭐⭐⭐⭐⭐ 4.9")
                            intent.putExtra("hotel_about", "A luxurious property offering royal suites, curated cuisines, and serene sea views.")
                            intent.putExtra("hotel_amenities", arrayListOf("Gym", "Free Parking", "Restaurant", "Spa"))
                        }
                        "Trident Nariman Point" -> {
                            intent.putExtra("hotel_rating", "⭐⭐⭐⭐ 4.5")
                            intent.putExtra("hotel_about", "Overlooking Marine Drive, Trident offers spacious rooms and fine sea-view dining.")
                            intent.putExtra("hotel_amenities", arrayListOf("Wi-Fi", "Pool", "Restaurant", "Bar"))
                        }
                        "The Oberoi, New Delhi" -> {
                            intent.putExtra("hotel_rating", "⭐⭐⭐⭐⭐ 4.9")
                            intent.putExtra("hotel_about", "A blend of modern luxury and classic elegance near India Gate.")
                            intent.putExtra("hotel_amenities", arrayListOf("Wi-Fi", "Pool", "Restaurant", "Gym", "Spa"))
                        }
                    }

                    startActivity(intent)
                }

            } else {
                price.text = "Sold Out"
                price.setBackgroundResource(R.drawable.price_soldout_bg)
                view.alpha = 0.6f
                view.isClickable = false
            }

            layoutHotels.addView(view)
        }

        // ✅ Bottom Navigation Clicks
        navHome.setOnClickListener {
            val intent = Intent(this, FindRoomActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        navWhere2Go.setOnClickListener {
            val intent = Intent(this, PlacesActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    // ✅ Logout Confirmation Dialog
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

}
