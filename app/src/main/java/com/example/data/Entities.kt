package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val address: String,
    val cnic: String = "",
    val eventType: String = "Other",
    val packageName: String = "",
    val bookingDate: Long = System.currentTimeMillis(),
    val eventDate: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val advanceReceived: Double = 0.0,
    val remainingAmount: Double = 0.0
)

@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val role: String, // "Photographer", "Videographer", "Assistant", "Drone Operator"
    val experience: String = "", // e.g., "5 Years"
    val specialty: String = "",  // e.g., "Portraits", "Cinematography"
    val availabilityStatus: String = "Available", // "Available", "Busy", "On Leave"
    val eventsWorked: Int = 0,
    val totalEarnings: Double = 0.0,
    val pendingPayments: Double = 0.0
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val clientName: String,
    val clientId: Long = 0, // Reference to Client table, or 0 if custom
    val eventType: String, // "Wedding", "Mehndi", "Barat", "Walima", "Birthday", "Corporate Event", "Other"
    val eventDate: Long, // timestamp
    val eventTime: String, // e.g. "18:00"
    val eventLocation: String,
    val notes: String = "",
    val status: String, // "Confirmed", "Tentative", "Completed", "Cancelled"
    
    // Team Assignment
    val assignedPhotographerId: Long = 0,
    val assignedVideographerId: Long = 0,
    val assignedAssistantId: Long = 0,
    val assignedDroneOperatorId: Long = 0,
    
    val photographerCount: Int = 1,
    val videographerCount: Int = 1
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "Fuel", "Food", "Travel", "Equipment Rental", "Other"
    val description: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "team_payments")
data class TeamPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamMemberId: Long,
    val teamMemberName: String,
    val agreedAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val paymentDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "data_deliveries")
data class DataDelivery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val clientName: String,
    val deliveryStatus: String, // "Not Started", "Editing", "Ready", "Delivered"
    val photosDelivered: Boolean = false,
    val videosDelivered: Boolean = false,
    val albumDelivered: Boolean = false,
    val deliveryDate: Long = 0,
    val googleDriveLink: String = "",
    val dropboxLink: String = "",
    val downloadLink: String = "",
    val clientConfirmed: Boolean = false
)
