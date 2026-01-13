package com.example.hotel

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

data class SelectedRoom(
    val name: String,
    val price: Int,
    val imageResId: Int
)

class CheckoutActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var imgHotelCheckout: ShapeableImageView
    private lateinit var tvCheckoutHotelName: TextView
    private lateinit var tvCheckoutDates: TextView
    private lateinit var tvCheckoutRooms: TextView
    private lateinit var tvGuestName: TextView
    private lateinit var tvGuestEmail: TextView
    private lateinit var tvGuestPhone: TextView
    private lateinit var layoutSelectedRooms: LinearLayout
    private lateinit var tvRoomCharges: TextView
    private lateinit var tvTaxes: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvBottomTotal: TextView
    private lateinit var radioGroupPayment: RadioGroup
    private lateinit var btnConfirmBooking: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var navHome: LinearLayout
    private lateinit var navWhere2Go: LinearLayout

    private val selectedRoomsList = mutableListOf<SelectedRoom>()
    private var currentTotalPrice = 0
    private var totalDays = 1
    private var hotelName = ""
    private var checkIn = ""
    private var checkOut = ""
    private var grandTotal = 0
    private var paymentMethod = "Pay at Hotel"

    companion object {
        const val SMS_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        Checkout.preload(applicationContext)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase
            .getInstance("https://hotel-80d8d-default-rtdb.firebaseio.com/")
            .reference

        imgHotelCheckout = findViewById(R.id.imgHotelCheckout)
        tvCheckoutHotelName = findViewById(R.id.tvCheckoutHotelName)
        tvCheckoutDates = findViewById(R.id.tvCheckoutDates)
        tvCheckoutRooms = findViewById(R.id.tvCheckoutRooms)
        tvGuestName = findViewById(R.id.tvGuestName)
        tvGuestEmail = findViewById(R.id.tvGuestEmail)
        tvGuestPhone = findViewById(R.id.tvGuestPhone)
        layoutSelectedRooms = findViewById(R.id.layoutSelectedRooms)
        tvRoomCharges = findViewById(R.id.tvRoomCharges)
        tvTaxes = findViewById(R.id.tvTaxes)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvBottomTotal = findViewById(R.id.tvBottomTotal)
        radioGroupPayment = findViewById(R.id.radioGroupPayment)
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking)

        // Receive intent data
        hotelName = intent.getStringExtra("hotel_name") ?: "Hotel"
        val hotelImage = intent.getIntExtra("hotel_image", R.drawable.placeholder)
        checkIn = intent.getStringExtra("check_in") ?: ""
        checkOut = intent.getStringExtra("check_out") ?: ""
        totalDays = intent.getIntExtra("total_days", 1)
        val totalPrice = intent.getIntExtra("total_price", 0)

        val roomNames = intent.getStringArrayExtra("room_names") ?: arrayOf()
        val roomPrices = intent.getIntArrayExtra("room_prices") ?: intArrayOf()
        val roomImages = intent.getIntArrayExtra("room_images") ?: intArrayOf()

        for (i in roomNames.indices) {
            selectedRoomsList.add(
                SelectedRoom(roomNames[i], roomPrices[i], roomImages[i])
            )
        }

        currentTotalPrice = totalPrice
        imgHotelCheckout.setImageResource(hotelImage)
        tvCheckoutHotelName.text = hotelName
        tvCheckoutDates.text = "$checkIn - $checkOut | $totalDays Days"
        tvCheckoutRooms.text = "${selectedRoomsList.size} Room(s)"

        loadSelectedRooms()
        updatePrices()
        loadUserDetails()

        btnConfirmBooking.setOnClickListener {
            val selectedId = radioGroupPayment.checkedRadioButtonId
            paymentMethod = if (selectedId == R.id.rbOnline) "Pay Online" else "Pay at Hotel"

            if (paymentMethod == "Pay Online") {
                startPayment()
            } else {
                saveBooking()
            }
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

    private fun loadSelectedRooms() {
        layoutSelectedRooms.removeAllViews()
        for ((index, room) in selectedRoomsList.withIndex()) {
            val view = layoutInflater.inflate(R.layout.item_checkout_room_card, layoutSelectedRooms, false)
            val image = view.findViewById<ShapeableImageView>(R.id.roomImageCheckout)
            val name = view.findViewById<TextView>(R.id.roomNameCheckout)
            val price = view.findViewById<TextView>(R.id.roomPriceCheckout)
            val btnRemove = view.findViewById<Button>(R.id.btnRemove)

            image.setImageResource(room.imageResId)
            name.text = room.name
            price.text = "₹${formatPrice(room.price)}"

            btnRemove.setOnClickListener {
                selectedRoomsList.removeAt(index)
                currentTotalPrice -= room.price
                loadSelectedRooms()
                updatePrices()
                tvCheckoutRooms.text = "${selectedRoomsList.size} Room(s)"
            }

            layoutSelectedRooms.addView(view)
        }
    }

    private fun updatePrices() {
        val taxes = (currentTotalPrice * 0.12).toInt()
        grandTotal = currentTotalPrice + taxes

        tvRoomCharges.text = "₹${formatPrice(currentTotalPrice)}"
        tvTaxes.text = "₹${formatPrice(taxes)}"
        tvTotalAmount.text = "₹${formatPrice(grandTotal)}"
        tvBottomTotal.text = "₹${formatPrice(grandTotal)}"
    }

    private fun loadUserDetails() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tvGuestName.text = snapshot.child("fullName").getValue(String::class.java) ?: ""
                    tvGuestEmail.text = snapshot.child("email").getValue(String::class.java) ?: ""
                    tvGuestPhone.text = snapshot.child("mobile").getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CheckoutActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ---------------- Razorpay Integration ----------------
    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_nntG9kbPd8pRPy") // Replace with your Razorpay Key ID

        try {
            val options = JSONObject()
            options.put("name", "Hotel Booking")
            options.put("description", "Payment for $hotelName")
            options.put("currency", "INR")
            options.put("amount", grandTotal * 100) // Amount in paise

            val prefill = JSONObject()
            prefill.put("email", tvGuestEmail.text)
            prefill.put("contact", tvGuestPhone.text)
            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Payment Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Toast.makeText(this, "Payment Successful! ID: $razorpayPaymentId", Toast.LENGTH_LONG).show()
        saveBooking(razorpayPaymentId)
    }

    override fun onPaymentError(code: Int, description: String?) {
        Toast.makeText(this, "Payment Failed: $description", Toast.LENGTH_LONG).show()
    }
    // -------------------------------------------------------

    private fun saveBooking(paymentId: String? = null) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val bookingId = database.child("bookings").push().key ?: return

        val roomDetails = selectedRoomsList.map {
            mapOf("name" to it.name, "price" to it.price)
        }

        val booking = mapOf(
            "bookingId" to bookingId,
            "userId" to uid,
            "hotelName" to hotelName,
            "checkIn" to checkIn,
            "checkOut" to checkOut,
            "totalDays" to totalDays,
            "rooms" to selectedRoomsList.size,
            "roomDetails" to roomDetails,
            "totalAmount" to grandTotal,
            "paymentMethod" to paymentMethod,
            "paymentId" to paymentId,
            "guestName" to tvGuestName.text.toString(),
            "guestEmail" to tvGuestEmail.text.toString(),
            "guestPhone" to tvGuestPhone.text.toString(),
            "timestamp" to System.currentTimeMillis(),
            "status" to if (paymentMethod == "Pay Online") "Paid" else "Confirmed"
        )

        database.child("bookings").child(bookingId).setValue(booking)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_LONG).show()

                // 🔹 Send SMS confirmation
                sendConfirmationSMS(
                    tvGuestPhone.text.toString(),
                    checkIn,
                    checkOut,
                    paymentMethod,
                    if (paymentMethod == "Pay Online") "Paid" else "Pending"
                )

                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ---------------- SMS Confirmation using SmsManager ----------------
    private fun sendConfirmationSMS(
        phone: String,
        checkIn: String,
        checkOut: String,
        paymentMethod: String,
        paymentStatus: String
    ) {
        val message = """
            Your booking at $hotelName is confirmed!
            Check-in: $checkIn
            Check-out: $checkOut
            Payment Method: $paymentMethod
            Status: $paymentStatus

            Thank you for booking with Where2Go!
        """.trimIndent()

        // Check if permission granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
            Toast.makeText(this, "Please allow SMS permission to send booking confirmation.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            Toast.makeText(this, "Booking confirmation SMS sent successfully!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "SMS sending failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted. Booking confirmations will be sent automatically.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SMS permission denied. Cannot send booking confirmations.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d", price)
    }
}
