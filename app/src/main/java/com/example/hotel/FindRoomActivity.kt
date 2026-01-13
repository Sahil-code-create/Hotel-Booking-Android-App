package com.example.hotel

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class FindRoomActivity : AppCompatActivity() {

    private lateinit var dropdownCity: AutoCompleteTextView
    private lateinit var tvCheckIn: TextView
    private lateinit var tvCheckOut: TextView
    private lateinit var etRooms: EditText
    private lateinit var btnSearch: Button
    private lateinit var layoutPlaces: LinearLayout
    private lateinit var viewAll: TextView
    private lateinit var btnLogout: Button

    private lateinit var navHome: LinearLayout
    private lateinit var navWhere2Go: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_room)

        // Initialize views
        dropdownCity = findViewById(R.id.dropdownCity)
        tvCheckIn = findViewById(R.id.tvCheckIn)
        tvCheckOut = findViewById(R.id.tvCheckOut)
        etRooms = findViewById(R.id.etRooms)
        btnSearch = findViewById(R.id.btnSearch)
        layoutPlaces = findViewById(R.id.layoutPlaces)
        viewAll = findViewById(R.id.viewAll)
        btnLogout = findViewById(R.id.btnLogout)

        navHome = findViewById(R.id.navHome)
        navWhere2Go = findViewById(R.id.navWhere2Go)

        // 🔹 Logout button with confirmation
        btnLogout.setOnClickListener { showLogoutConfirmation() }

        // 🔹 Dropdown for cities
        val cities = listOf("Agra", "Bengaluru", "Chennai", "Delhi", "Mumbai")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        dropdownCity.setAdapter(adapter)
        dropdownCity.setOnClickListener { dropdownCity.showDropDown() }

        // 🔹 Date pickers
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvCheckIn.setOnClickListener { pickDate { date -> tvCheckIn.text = dateFormat.format(date.time) } }
        tvCheckOut.setOnClickListener { pickDate { date -> tvCheckOut.text = dateFormat.format(date.time) } }

        // 🔹 Search button
        btnSearch.setOnClickListener {
            val city = dropdownCity.text.toString().trim()
            val checkIn = tvCheckIn.text.toString().trim()
            val checkOut = tvCheckOut.text.toString().trim()
            val rooms = etRooms.text.toString().trim()

            if (city.isEmpty() || checkIn.isEmpty() || checkOut.isEmpty() || rooms.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, HotelListActivity::class.java)
                intent.putExtra("city_name", city)
                intent.putExtra("check_in", checkIn)
                intent.putExtra("check_out", checkOut)
                intent.putExtra("rooms", rooms)
                startActivity(intent)
            }
        }

        // 🔹 View All places
        viewAll.setOnClickListener {
            startActivity(Intent(this, PlacesActivity::class.java))
        }

        // 🔹 Populate "Best Places" with descriptions
        val places = listOf(
            Place("Agra", R.drawable.taj_mahal, getString(R.string.agra_info)),
            Place("Bengaluru", R.drawable.bengaluru, getString(R.string.bengaluru_info)),
            Place("Chennai", R.drawable.chennai, getString(R.string.chennai_info)),
            Place("Delhi", R.drawable.delhi, getString(R.string.delhi_info)),
            Place("Mumbai", R.drawable.mumbai, getString(R.string.mumbai_info))
        )

        for (place in places) {
            val placeView = layoutInflater.inflate(R.layout.item_place_card, layoutPlaces, false)
            val img = placeView.findViewById<ImageView>(R.id.placeImage)
            val name = placeView.findViewById<TextView>(R.id.placeName)
            img.setImageResource(place.imageRes)
            name.text = place.name

            // 🔹 UPDATED: Navigate to PlaceDetailsActivity instead of HotelListActivity
            placeView.setOnClickListener {
                val intent = Intent(this, PlaceDetailsActivity::class.java)
                intent.putExtra("placeName", place.name)
                intent.putExtra("placeImage", place.imageRes)
                intent.putExtra("placeDescription", place.description)
                startActivity(intent)
            }

            layoutPlaces.addView(placeView)
        }

        // 🔹 Bottom navigation actions
        navHome.setOnClickListener {
            // Stay on FindRoomActivity
            Toast.makeText(this, "You are already on Home", Toast.LENGTH_SHORT).show()
        }

        navWhere2Go.setOnClickListener {
            val intent = Intent(this, PlacesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun pickDate(onDatePicked: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                onDatePicked(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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