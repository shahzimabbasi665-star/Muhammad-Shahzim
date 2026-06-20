package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LensoraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.appDao())

    // UI Configuration States
    var currentLanguage by mutableStateOf("en") // "en", "ur"
    var isDarkMode by mutableStateOf(false)
    var currentUserRole by mutableStateOf("Admin") // "Admin", "Manager", "Staff"

    // Multi-Language Key-Value Map
    private val urStrings = mapOf(
        "app_name" to "لینسورا",
        "dashboard" to "ڈیش بورڈ",
        "calendar_bookings" to "کیلنڈر بکنگ",
        "clients" to "کلائنٹس",
        "photographers" to "فوٹوگرافرز",
        "videographers" to "ویڈیو گرافرز",
        "payments" to "ادائیگیاں",
        "reports" to "رپورٹس",
        "data_delivery" to "ڈیٹا ڈیلیوری",
        "notifications" to "اطلاعات",
        "settings" to "ترتیبات",
        
        "total_bookings" to "کل بکنگ",
        "upcoming_events" to "آنے والے واقعات",
        "todays_events" to "آج کے واقعات",
        "pending_payments" to "بقیہ ادائیگیاں",
        "completed_events" to "مکمل شدہ واقعات",
        "delivery_pending" to "ڈیلیوری باقی",
        "delivered" to "ڈیلیور شدہ",
        "total_revenue" to "کل آمدنی",
        "total_expenses" to "کل اخراجات",
        "monthly_profit" to "ماہانہ منافع",
        
        "event_title" to "ایونٹ کا عنوان",
        "client_name" to "کلائنٹ کا نام",
        "event_type" to "ایونٹ کی قسم",
        "wedding" to "شادی",
        "mehndi" to "مہندی",
        "barat" to "بارات",
        "walima" to "ولیمہ",
        "birthday" to "سالگرہ",
        "corporate" to "کارپوریٹ ایونٹ",
        "other" to "دیگر",
        "date" to "تاریخ",
        "time" to "وقت",
        "location" to "مقام",
        "notes" to "نوٹس / تفصیل",
        "assign_team" to "ٹیم تفویض کریں",
        "photographer" to "فوٹوگرافر",
        "videographer" to "ویڈیو گرافر",
        "assistant" to "اسسٹنٹ",
        "drone_operator" to "ڈرون آپریٹر",
        "photographers_count" to "فوٹوگرافرز کی تعداد",
        "videographers_count" to "ویڈیوگرافرز کی تعداد",
        "status" to "حیثیت",
        "confirmed" to "تصدیق شدہ",
        "tentative" to "غیر حتمی",
        "completed" to "مکمل",
        "cancelled" to "منسوخ",
        "submit" to "محفوظ کریں",
        "cancel" to "منسوخ کریں",
        
        "phone" to "فون نمبر",
        "whatsapp" to "واٹس ایپ",
        "email" to "ای میل",
        "address" to "پتہ",
        "cnic" to "شناختی کارڈ نمبر",
        "package_name" to "پیکیج کا نام",
        "total_amount" to "ٹوٹل رقم",
        "advance_received" to "ایڈوانس وصول",
        "remaining_amount" to "بقیہ رقم",
        
        "paid" to "ادا شدہ",
        "partial" to "جزوی ادا",
        "unpaid" to "غیر ادا شدہ",
        "agreed_amount" to "طے شدہ رقم",
        "paid_amount" to "ادا شدہ رقم",
        "remaining_balance" to "بقیہ بیلنس",
        "fuel" to "فیول",
        "food" to "کھانا",
        "travel" to "سفر",
        "rent" to "سامان کا کرایہ",
        "other_exp" to "دیگر اخراجات",
        
        "name" to "نام",
        "experience" to "تجربہ",
        "specialty" to "مہارت",
        "availability" to "دستیابی کی حیثيت",
        "events_worked" to "کام کیے گئے ایونٹس",
        "total_earnings" to "کل کمائی",
        
        "delivery_status" to "ڈیلیوری کی حیثیت",
        "not_started" to "شروع نہیں ہوا",
        "editing" to "ایڈیٹنگ",
        "ready" to "تیار ہے",
        "client_received_data" to "کلائنٹ کو ڈیٹا مل گیا",
        "drive_link" to "گوگل ڈرائیو لنک",
        "dropbox_link" to "ڈراپ باکس لنک",
        "download_link" to "ڈاؤن لوڈ لنک",
        
        "alerts" to "انتباہات",
        "event_tomorrow" to "کل کا ایونٹ",
        "event_today" to "آج کا ایونٹ",
        "payment_pending" to "ادائیگی باقی ہے",
        "delivery_due" to "ڈیلیوری باقی ہے",
        "duplicate_warning" to "انتباہ: اس ٹیم ممبر کی اس تاریخ/وقت پر پہلے ہی بکنگ ہے!"
    )

    private val enStrings = mapOf(
        "app_name" to "Lensora",
        "dashboard" to "Dashboard",
        "calendar_bookings" to "Calendar Bookings",
        "clients" to "Clients",
        "photographers" to "Photographers",
        "videographers" to "Videographers",
        "payments" to "Payments",
        "reports" to "Reports",
        "data_delivery" to "Data Delivery",
        "notifications" to "Notifications",
        "settings" to "Settings",
        
        "total_bookings" to "Total Bookings",
        "upcoming_events" to "Upcoming Events",
        "todays_events" to "Today's Events",
        "pending_payments" to "Pending Payments",
        "completed_events" to "Completed Events",
        "delivery_pending" to "Delivery Pending",
        "delivered" to "Delivered",
        "total_revenue" to "Total Revenue",
        "total_expenses" to "Total Expenses",
        "monthly_profit" to "Monthly Profit",
        
        "event_title" to "Event Title",
        "client_name" to "Client Name",
        "event_type" to "Event Type",
        "wedding" to "Wedding",
        "mehndi" to "Mehndi",
        "barat" to "Barat",
        "walima" to "Walima",
        "birthday" to "Birthday",
        "corporate" to "Corporate Event",
        "other" to "Other",
        "date" to "Date",
        "time" to "Time",
        "location" to "Location",
        "notes" to "Notes / Description",
        "assign_team" to "Assign Team",
        "photographer" to "Photographer",
        "videographer" to "Videographer",
        "assistant" to "Assistant",
        "drone_operator" to "Drone Operator",
        "photographers_count" to "No. of Photographers",
        "videographers_count" to "No. of Videographers",
        "status" to "Status",
        "confirmed" to "Confirmed",
        "tentative" to "Tentative",
        "completed" to "Completed",
        "cancelled" to "Cancelled",
        "submit" to "Save",
        "cancel" to "Cancel",
        
        "phone" to "Phone Number",
        "whatsapp" to "WhatsApp",
        "email" to "Email",
        "address" to "Address",
        "cnic" to "CNIC (Optional)",
        "package_name" to "Package Name",
        "total_amount" to "Total Amount",
        "advance_received" to "Advance Received",
        "remaining_amount" to "Remaining Amount",
        
        "paid" to "Paid",
        "partial" to "Partial",
        "unpaid" to "Unpaid",
        "agreed_amount" to "Agreed Amount",
        "paid_amount" to "Paid Amount",
        "remaining_balance" to "Remaining Balance",
        "fuel" to "Fuel",
        "food" to "Food",
        "travel" to "Travel",
        "rent" to "Equipment Rental",
        "other_exp" to "Other Expenses",
        
        "name" to "Name",
        "experience" to "Experience",
        "specialty" to "Specialty",
        "availability" to "Availability Status",
        "events_worked" to "Events Worked",
        "total_earnings" to "Total Earnings",
        
        "delivery_status" to "Delivery Status",
        "not_started" to "Not Started",
        "editing" to "Editing",
        "ready" to "Ready",
        "client_received_data" to "Client Received Data",
        "drive_link" to "Google Drive Link",
        "dropbox_link" to "Dropbox Link",
        "download_link" to "Download Link",
        
        "alerts" to "Alerts",
        "event_tomorrow" to "Event Tomorrow",
        "event_today" to "Event Today",
        "payment_pending" to "Payment Pending",
        "delivery_due" to "Delivery Pending",
        "duplicate_warning" to "Warning: Assigned team member has another booking at this date/time!"
    )

    fun getString(key: String): String {
        return if (currentLanguage == "ur") {
            urStrings[key] ?: enStrings[key] ?: key
        } else {
            enStrings[key] ?: key
        }
    }

    // Reactive StateFlow sources from Room
    val clients: StateFlow<List<Client>> = repository.allClients.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val teamMembers: StateFlow<List<TeamMember>> = repository.allTeamMembers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookings: StateFlow<List<Booking>> = repository.allBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val expenses: StateFlow<List<Expense>> = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val teamPayments: StateFlow<List<TeamPayment>> = repository.allTeamPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dataDeliveries: StateFlow<List<DataDelivery>> = repository.allDataDeliveries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Pre-populate with beautiful SaaS sample data if empty
        viewModelScope.launch {
            repository.allClients.collect { list ->
                if (list.isEmpty()) {
                    seedDatabase()
                }
            }
        }
    }

    private suspend fun seedDatabase() {
        // 1. Add Team Members
        val m1 = TeamMember(name = "Kamran Shah", phone = "+923001234567", role = "Photographer", experience = "6 Years", specialty = "Portraits & Fashion", availabilityStatus = "Available", eventsWorked = 8, totalEarnings = 120000.0, pendingPayments = 20000.0)
        val m2 = TeamMember(name = "Zahid Naeem", phone = "+923129876543", role = "Videographer", experience = "4 Years", specialty = "Cinematic Storytelling", availabilityStatus = "Available", eventsWorked = 5, totalEarnings = 95000.0, pendingPayments = 15000.0)
        val m3 = TeamMember(name = "Hamza Ahmed", phone = "+923334455667", role = "Assistant", experience = "2 Years", specialty = "Lighting & Setup", availabilityStatus = "Available", eventsWorked = 11, totalEarnings = 40000.0, pendingPayments = 0.0)
        val m4 = TeamMember(name = "Faraz Sheikh", phone = "+923455566778", role = "Drone Operator", experience = "3 Years", specialty = "Aerial Cinematography", availabilityStatus = "Available", eventsWorked = 3, totalEarnings = 60000.0, pendingPayments = 10000.0)

        val id1 = repository.insertTeamMember(m1)
        val id2 = repository.insertTeamMember(m2)
        val id3 = repository.insertTeamMember(m3)
        val id4 = repository.insertTeamMember(m4)

        // Date calculations
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 6) }

        // 2. Add Clients
        val c1 = Client(name = "Ayesha & Bilal", phone = "+923214567890", whatsapp = "+923214567890", email = "ayesha.bilal@gmail.com", address = "DHA Phase 6, Lahore", eventType = "Wedding", packageName = "Premium Signature Wedding Package", eventDate = tomorrow.timeInMillis, totalAmount = 180000.0, advanceReceived = 80000.0, remainingAmount = 100000.0)
        val c2 = Client(name = "Zainab & Hamza", phone = "+923015551234", whatsapp = "+923015551234", email = "zainab.h@yahoo.com", address = "Gulberg III, Lahore", eventType = "Mehndi", packageName = "Mehndi Special Cinematic Pack", eventDate = nextWeek.timeInMillis, totalAmount = 90000.0, advanceReceived = 30000.0, remainingAmount = 60000.0)
        val c3 = Client(name = "Nizam Tech Launch", phone = "+923158884422", whatsapp = "+923158884422", email = "media@nizamtech.com", address = "PC Hotel, Lahore", eventType = "Corporate Event", packageName = "Standard Corporate Media Package", eventDate = yesterday.timeInMillis, totalAmount = 150000.0, advanceReceived = 150000.0, remainingAmount = 0.0)

        val cid1 = repository.insertClient(c1)
        val cid2 = repository.insertClient(c2)
        val cid3 = repository.insertClient(c3)

        // 3. Add Bookings corresponding to client events
        val b1 = Booking(title = "Ayesha Wedding Day", clientName = "Ayesha & Bilal", clientId = cid1, eventType = "Wedding", eventDate = tomorrow.timeInMillis, eventTime = "18:00", eventLocation = "Royal Palm Golf & Country Club, Lahore", notes = "Capture couple portraits before guest arrivals", status = "Confirmed", assignedPhotographerId = id1, assignedVideographerId = id2, assignedAssistantId = id3, assignedDroneOperatorId = id4, photographerCount = 2, videographerCount = 2)
        val b2 = Booking(title = "Zainab Mehndi Night", clientName = "Zainab & Hamza", clientId = cid2, eventType = "Mehndi", eventDate = nextWeek.timeInMillis, eventTime = "19:30", eventLocation = "Shehnai Marquee, Canal Road", notes = "Traditional entrance and dance focus", status = "Confirmed", assignedPhotographerId = id1, assignedAssistantId = id3, photographerCount = 1, videographerCount = 0)
        val b3 = Booking(title = "Nizam Tech Corporate Launch", clientName = "Nizam Tech Launch", clientId = cid3, eventType = "Corporate Event", eventDate = yesterday.timeInMillis, eventTime = "10:00", eventLocation = "PC Hotel Shalimar Hall", notes = "Press conference, banner shots, keynote speakers", status = "Completed", assignedPhotographerId = id1, assignedVideographerId = id2, photographerCount = 1, videographerCount = 1)

        val bid1 = repository.insertBooking(b1)
        val bid2 = repository.insertBooking(b2)
        val bid3 = repository.insertBooking(b3)

        // 4. Add Expenses
        repository.insertExpense(Expense(type = "Fuel", description = "Travel fuel for Royal Palm location scouting", amount = 4500.0, date = yesterday.timeInMillis))
        repository.insertExpense(Expense(type = "Food", description = "Team lunch at PC Hotel during Nizam Tech launch", amount = 6800.0, date = yesterday.timeInMillis))
        repository.insertExpense(Expense(type = "Equipment Rental", description = "Movi Pro stabilizer rental for cinematic shots", amount = 15000.0, date = yesterday.timeInMillis))

        // 5. Add Team Payments
        repository.insertTeamPayment(TeamPayment(teamMemberId = id1, teamMemberName = "Kamran Shah", agreedAmount = 25000.0, paidAmount = 5000.0, remainingAmount = 20000.0, paymentDate = yesterday.timeInMillis))
        repository.insertTeamPayment(TeamPayment(teamMemberId = id2, teamMemberName = "Zahid Naeem", agreedAmount = 20000.0, paidAmount = 5000.0, remainingAmount = 15000.0, paymentDate = yesterday.timeInMillis))

        // 6. Add Data Deliveries
        repository.insertDataDelivery(DataDelivery(bookingId = bid1, clientName = "Ayesha & Bilal", deliveryStatus = "Editing", photosDelivered = false, videosDelivered = false, googleDriveLink = "https://drive.google.com/drive/folders/ayesha_bilal_lensora", dropboxLink = "", downloadLink = ""))
        repository.insertDataDelivery(DataDelivery(bookingId = bid3, clientName = "Nizam Tech Launch", deliveryStatus = "Delivered", photosDelivered = true, videosDelivered = true, albumDelivered = true, deliveryDate = yesterday.timeInMillis, googleDriveLink = "https://drive.google.com/drive/folders/nizam_tech_lensora", dropboxLink = "https://dropbox.com/sh/nizam_tech_deliver", downloadLink = "https://lensora.com/download/nizamtech", clientConfirmed = true))
    }

    // Database Actions
    fun addClient(client: Client) {
        viewModelScope.launch {
            repository.insertClient(client)
        }
    }

    fun modifyClient(client: Client) {
        viewModelScope.launch {
            repository.updateClient(client)
        }
    }

    fun removeClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    fun addTeamMember(member: TeamMember) {
        viewModelScope.launch {
            repository.insertTeamMember(member)
        }
    }

    fun modifyTeamMember(member: TeamMember) {
        viewModelScope.launch {
            repository.updateTeamMember(member)
        }
    }

    fun removeTeamMember(member: TeamMember) {
        viewModelScope.launch {
            repository.deleteTeamMember(member)
        }
    }

    fun addBooking(booking: Booking) {
        viewModelScope.launch {
            val bookingId = repository.insertBooking(booking)
            // Create data delivery entry instantly for the booking
            repository.insertDataDelivery(
                DataDelivery(
                    bookingId = bookingId,
                    clientName = booking.clientName,
                    deliveryStatus = "Not Started"
                )
            )
        }
    }

    fun modifyBooking(booking: Booking) {
        viewModelScope.launch {
            repository.updateBooking(booking)
        }
    }

    fun removeBooking(booking: Booking) {
        viewModelScope.launch {
            repository.deleteBooking(booking)
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addTeamPayment(payment: TeamPayment) {
        viewModelScope.launch {
            repository.insertTeamPayment(payment)
            // Also deduct from team member's pending payments and increase total earnings
            teamMembers.value.find { it.id == payment.teamMemberId }?.let { member ->
                val updated = member.copy(
                    totalEarnings = member.totalEarnings + payment.paidAmount,
                    pendingPayments = (member.pendingPayments - payment.paidAmount).coerceAtLeast(0.0)
                )
                repository.updateTeamMember(updated)
            }
        }
    }

    fun modifyDataDelivery(delivery: DataDelivery) {
        viewModelScope.launch {
            repository.updateDataDelivery(delivery)
        }
    }

    // Check for double-booking warnings relative to a Team Member
    fun isTeamMemberDoubleBooked(teamMemberId: Long, dateInMillis: Long, excludeBookingId: Long = 0): Boolean {
        if (teamMemberId == 0L) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = dateInMillis }
        return bookings.value.any { booking ->
            if (booking.id == excludeBookingId) return@any false
            if (booking.status == "Cancelled") return@any false
            val isAssigned = booking.assignedPhotographerId == teamMemberId ||
                    booking.assignedVideographerId == teamMemberId ||
                    booking.assignedAssistantId == teamMemberId ||
                    booking.assignedDroneOperatorId == teamMemberId
            if (isAssigned) {
                val cal2 = Calendar.getInstance().apply { timeInMillis = booking.eventDate }
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
            } else {
                false
            }
        }
    }

    // Perform dummy local backup
    fun triggerBackupAndRestore(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Seed more sample data as a restore action
            seedDatabase()
            onComplete()
        }
    }
}
