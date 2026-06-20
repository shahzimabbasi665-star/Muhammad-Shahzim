package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Clients
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    // Team Members
    @Query("SELECT * FROM team_members ORDER BY role, name ASC")
    fun getAllTeamMembers(): Flow<List<TeamMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMember(teamMember: TeamMember): Long

    @Update
    suspend fun updateTeamMember(teamMember: TeamMember)

    @Delete
    suspend fun deleteTeamMember(teamMember: TeamMember)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY eventDate ASC")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Team Payments
    @Query("SELECT * FROM team_payments ORDER BY paymentDate DESC")
    fun getAllTeamPayments(): Flow<List<TeamPayment>>

    @Query("SELECT * FROM team_payments WHERE teamMemberId = :memberId")
    fun getPaymentsForMember(memberId: Long): Flow<List<TeamPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamPayment(teamPayment: TeamPayment): Long

    @Update
    suspend fun updateTeamPayment(teamPayment: TeamPayment)

    @Delete
    suspend fun deleteTeamPayment(teamPayment: TeamPayment)

    // Data Deliveries
    @Query("SELECT * FROM data_deliveries ORDER BY deliveryDate DESC")
    fun getAllDataDeliveries(): Flow<List<DataDelivery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataDelivery(dataDelivery: DataDelivery): Long

    @Update
    suspend fun updateDataDelivery(dataDelivery: DataDelivery)

    @Delete
    suspend fun deleteDataDelivery(dataDelivery: DataDelivery)
}
