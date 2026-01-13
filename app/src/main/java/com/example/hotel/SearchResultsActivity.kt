package com.example.hotel

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView

class SearchResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        val city = intent.getStringExtra("city_name") ?: "Unknown"
        val checkIn = intent.getStringExtra("check_in")
        val checkOut = intent.getStringExtra("check_out")
        val rooms = intent.getStringExtra("rooms")

        title = "Hotels in $city"

        val layoutHotels = findViewById<LinearLayout>(R.id.layoutHotels)
        val tvSearchInfo = findViewById<TextView>(R.id.tvSearchInfo)

        tvSearchInfo.text = "Showing hotels in $city\nCheck-in: $checkIn\nCheck-out: $checkOut\nRooms: $rooms"

        // Sample hotels by city (you can later fetch from API or DB)
        val hotels = when (city) {
            "Agra" -> listOf(
                Hotel("Fairfield Hotel, Agra", R.drawable.fairfield, "₹4,999 to ₹9,999", true),
                Hotel("Oberoi Hotel, Agra", R.drawable.oberoi, "₹10,000 to ₹15,000", true)
            )
            "Bengaluru" -> listOf(
                Hotel("ITC Gardenia", R.drawable.itcgardenia, "₹7,500 to ₹12,000", true),
                Hotel("Taj MG Road", R.drawable.tajmgroad, "₹8,000 to ₹13,000", false)
            )
            else -> listOf(
                Hotel("No hotels found", R.drawable.placeholder, "", false)
            )
        }

        for (hotel in hotels) {
            val view = layoutInflater.inflate(R.layout.item_hotel_card, layoutHotels, false)

            val image = view.findViewById<ShapeableImageView>(R.id.hotelImage)
            val name = view.findViewById<TextView>(R.id.hotelName)
            val price = view.findViewById<TextView>(R.id.hotelPrice)

            image.setImageResource(hotel.imageResId)
            name.text = hotel.name
            price.text = if (hotel.isAvailable) hotel.priceRange else "Sold Out"
            layoutHotels.addView(view)
        }
    }
}
