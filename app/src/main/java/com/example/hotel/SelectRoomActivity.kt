package com.example.hotel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip

data class Room(
    val name: String,
    val price: Int,
    val imageResId: Int,
    var isSelected: Boolean = false
)

class SelectRoomActivity : AppCompatActivity() {

    private lateinit var layoutRooms: LinearLayout
    private lateinit var tvHotelName: TextView
    private lateinit var imgHotel: ShapeableImageView
    private lateinit var tvRating: TextView
    private lateinit var tvAbout: TextView
    private lateinit var amenitiesContainer: FlexboxLayout
    private lateinit var layoutBottomBar: LinearLayout
    private lateinit var tvSummary: TextView
    private lateinit var btnCheckout: Button

    private lateinit var navHome: LinearLayout
    private lateinit var navWhere2Go: LinearLayout

    private var selectedRooms = 0
    private var totalPrice = 0
    private var totalDays = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_room)

        // Initialize views
        layoutRooms = findViewById(R.id.layoutRooms)
        tvHotelName = findViewById(R.id.tvHotelName)
        imgHotel = findViewById(R.id.imgHotel)
        tvRating = findViewById(R.id.tvRating)
        tvAbout = findViewById(R.id.tvAbout)
        amenitiesContainer = findViewById(R.id.amenitiesContainer)
        layoutBottomBar = findViewById(R.id.layoutBottomBar)
        tvSummary = findViewById(R.id.tvSummary)
        btnCheckout = findViewById(R.id.btnCheckout)

        // Receive hotel info from previous screen
        val hotelName = intent.getStringExtra("hotel_name") ?: "Hotel"
        val hotelImage = intent.getIntExtra("hotel_image", R.drawable.placeholder)
        val city = intent.getStringExtra("city_name") ?: ""
        val checkIn = intent.getStringExtra("check_in") ?: ""
        val checkOut = intent.getStringExtra("check_out") ?: ""

        // 🔹 Receive dynamic rating, about, and amenities
        val rating = intent.getStringExtra("hotel_rating") ?: "⭐⭐⭐⭐ 4.3"
        val about = intent.getStringExtra("hotel_about") ?: "Our hotel is located in Ashok Cosmos Mall in Sanjay Place, one of the largest shopping malls in the city. Many sites like the Taj Mahal are minutes away while fun outings to Agra Golf Course are just around the corner."
        val amenities = intent.getStringArrayListExtra("hotel_amenities")?.toTypedArray() ?: arrayOf(
            "WiFi", "Pool", "Spa", "Room Service", "Laundry", "24/7 Security"
        )

        tvHotelName.text = "$hotelName, $city"
        imgHotel.setImageResource(hotelImage)
        tvRating.text = rating
        tvAbout.text = about

        // 🔹 Populate amenities dynamically
        populateAmenities(amenities)

        // Calculate number of days
        totalDays = calculateDays(checkIn, checkOut)

        // Sample rooms list
        val rooms = listOf(
            Room("Deluxe Room AC", 4999, R.drawable.deluxe),
            Room("Double Room", 7999, R.drawable.double_room),
            Room("Executive Room", 9999, R.drawable.executive_room)
        )

        // Inflate rooms dynamically
        for (room in rooms) {
            val view = layoutInflater.inflate(R.layout.item_room_card, layoutRooms, false)
            val image = view.findViewById<ShapeableImageView>(R.id.roomImage)
            val name = view.findViewById<TextView>(R.id.roomName)
            val price = view.findViewById<TextView>(R.id.roomPrice)
            val btnSelect = view.findViewById<Button>(R.id.btnSelect)

            image.setImageResource(room.imageResId)
            name.text = room.name
            price.text = "Rs. ${formatPrice(room.price)}"

            btnSelect.setOnClickListener {
                room.isSelected = !room.isSelected
                if (room.isSelected) {
                    selectedRooms++
                    totalPrice += room.price
                    btnSelect.text = "SELECTED"
                    btnSelect.backgroundTintList = getColorStateList(android.R.color.holo_green_dark)
                    btnSelect.setTextColor(getColor(android.R.color.white))
                } else {
                    selectedRooms--
                    totalPrice -= room.price
                    btnSelect.text = "SELECT"
                    btnSelect.backgroundTintList = getColorStateList(android.R.color.holo_blue_dark)
                    btnSelect.setTextColor(getColor(android.R.color.white))
                }

                updateBottomBar()
            }

            layoutRooms.addView(view)
        }

        btnCheckout.setOnClickListener {
            // Create list of selected rooms
            val selectedRoomsList = rooms.filter { it.isSelected }

            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("hotel_name", hotelName)
            intent.putExtra("hotel_image", hotelImage)
            intent.putExtra("check_in", checkIn)
            intent.putExtra("check_out", checkOut)
            intent.putExtra("total_days", totalDays)
            intent.putExtra("selected_rooms", selectedRooms)
            intent.putExtra("total_price", totalPrice)

            // Pass selected room details as arrays
            val roomNames = selectedRoomsList.map { it.name }.toTypedArray()
            val roomPrices = selectedRoomsList.map { it.price }.toIntArray()
            val roomImages = selectedRoomsList.map { it.imageResId }.toIntArray()

            intent.putExtra("room_names", roomNames)
            intent.putExtra("room_prices", roomPrices)
            intent.putExtra("room_images", roomImages)

            startActivity(intent)
        }

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

    // 🔹 Function to populate amenities dynamically
    private fun populateAmenities(amenities: Array<String>) {
        amenitiesContainer.removeAllViews()

        for (amenity in amenities) {
            val chip = Chip(this).apply {
                text = amenity
                isClickable = false
                isCheckable = false
                chipBackgroundColor = getColorStateList(android.R.color.holo_blue_light)
                setTextColor(getColor(android.R.color.white))
                chipCornerRadius = 24f
                chipStrokeWidth = 0f
                textSize = 13f
                setPadding(16, 8, 16, 8)

                // 🔹 Add icon based on amenity name
                chipIcon = getDrawable(getAmenityIcon(amenity))
                setChipIconTintResource(android.R.color.white)
                chipIconSize = 48f
            }

            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            chip.layoutParams = params

            amenitiesContainer.addView(chip)
        }
    }

    // 🔹 Helper function to get icon resource based on amenity name
    private fun getAmenityIcon(amenity: String): Int {
        return when (amenity.lowercase()) {
            "gym" -> android.R.drawable.ic_menu_compass
            "free parking" -> android.R.drawable.ic_menu_mylocation
            "restaurant" -> android.R.drawable.ic_menu_sort_by_size
            "pool" -> android.R.drawable.ic_menu_gallery
            "wi-fi", "wifi" -> android.R.drawable.stat_sys_data_bluetooth
            "spa" -> android.R.drawable.ic_menu_view
            "bar" -> android.R.drawable.ic_menu_preferences
            "room service" -> android.R.drawable.ic_menu_manage
            "laundry" -> android.R.drawable.ic_menu_rotate
            "24/7 security" -> android.R.drawable.ic_lock_lock
            else -> android.R.drawable.ic_menu_info_details
        }
    }

    private fun calculateDays(checkIn: String, checkOut: String): Int {
        return try {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val start = sdf.parse(checkIn)
            val end = sdf.parse(checkOut)
            if (start != null && end != null) {
                val diff = (end.time - start.time) / (1000 * 60 * 60 * 24)
                if (diff <= 0) 1 else diff.toInt()
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d", price)
    }

    private fun updateBottomBar() {
        layoutBottomBar.isVisible = selectedRooms > 0
        if (selectedRooms > 0) {
            val formattedPrice = formatPrice(totalPrice)
            tvSummary.text = "$selectedRooms Room(s) | $totalDays Days | Rs. $formattedPrice"
        }
    }
}