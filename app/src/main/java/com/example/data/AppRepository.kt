package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allClients: Flow<List<Client>> = appDao.getAllClients()
    val allTeamMembers: Flow<List<TeamMember>> = appDao.getAllTeamMembers()
    val allBookings: Flow<List<Booking>> = appDao.getAllBookings()
    val allExpenses: Flow<List<Expense>> = appDao.getAllExpenses()
    val allTeamPayments: Flow<List<TeamPayment>> = appDao.getAllTeamPayments()
    val allDataDeliveries: Flow<List<DataDelivery>> = appDao.getAllDataDeliveries()

    suspend fun insertClient(client: Client): Long = appDao.insertClient(client)
    suspend fun updateClient(client: Client) = appDao.updateClient(client)
    suspend fun deleteClient(client: Client) = appDao.deleteClient(client)

    suspend fun insertTeamMember(teamMember: TeamMember): Long = appDao.insertTeamMember(teamMember)
    suspend fun updateTeamMember(teamMember: TeamMember) = appDao.updateTeamMember(teamMember)
    suspend fun deleteTeamMember(teamMember: TeamMember) = appDao.deleteTeamMember(teamMember)

    suspend fun insertBooking(booking: Booking): Long = appDao.insertBooking(booking)
    suspend fun updateBooking(booking: Booking) = appDao.updateBooking(booking)
    suspend fun deleteBooking(booking: Booking) = appDao.deleteBooking(booking)

    suspend fun insertExpense(expense: Expense): Long = appDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = appDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = appDao.deleteExpense(expense)

    suspend fun insertTeamPayment(teamPayment: TeamPayment): Long = appDao.insertTeamPayment(teamPayment)
    suspend fun updateTeamPayment(teamPayment: TeamPayment) = appDao.updateTeamPayment(teamPayment)
    suspend fun deleteTeamPayment(teamPayment: TeamPayment) = appDao.deleteTeamPayment(teamPayment)

    suspend fun insertDataDelivery(dataDelivery: DataDelivery): Long = appDao.insertDataDelivery(dataDelivery)
    suspend fun updateDataDelivery(dataDelivery: DataDelivery) = appDao.updateDataDelivery(dataDelivery)
    suspend fun deleteDataDelivery(dataDelivery: DataDelivery) = appDao.deleteDataDelivery(dataDelivery)
}
