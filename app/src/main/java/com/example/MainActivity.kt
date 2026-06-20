package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LensoraViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: LensoraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                LensoraApp(viewModel)
            }
        }
    }
}

// Side-bar routing destinations
enum class Screen {
    Dashboard,
    CalendarBookings,
    Clients,
    Photographers,
    Videographers,
    Payments,
    Reports,
    DataDelivery,
    Notifications,
    Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LensoraApp(viewModel: LensoraViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    var showMobileMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Observe DB lists
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val teamMembers by viewModel.teamMembers.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val teamPayments by viewModel.teamPayments.collectAsStateWithLifecycle()
    val deliveries by viewModel.dataDeliveries.collectAsStateWithLifecycle()

    // Scaffolding a full SaaS look
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = viewModel.getString("app_name"),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Studio SaaS Suite",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showMobileMenu = !showMobileMenu }) {
                        Icon(
                            imageVector = if (showMobileMenu) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = "Toggle Sidebar"
                        )
                    }
                },
                actions = {
                    // Profile Role / Fast Actions
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = when (viewModel.currentUserRole) {
                                    "Admin" -> Icons.Default.AdminPanelSettings
                                    "Manager" -> Icons.Default.ManageAccounts
                                    else -> Icons.Default.Person
                                },
                                contentDescription = "Role Icon",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = viewModel.currentUserRole,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.isDarkMode = !viewModel.isDarkMode }) {
                        Icon(
                            imageVector = if (viewModel.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme Toggle"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Adaptive mini Bottom Navigation Bar for quick access
            NavigationBar {
                val items = listOf(
                    Triple(Screen.Dashboard, Icons.Default.Dashboard, "dashboard"),
                    Triple(Screen.CalendarBookings, Icons.Default.Event, "calendar_bookings"),
                    Triple(Screen.Clients, Icons.Default.People, "clients"),
                    Triple(Screen.Payments, Icons.Default.AttachMoney, "payments"),
                    Triple(Screen.DataDelivery, Icons.Default.CloudQueue, "data_delivery")
                )
                items.forEach { (screen, icon, stringKey) ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(imageVector = icon, contentDescription = viewModel.getString(stringKey)) },
                        label = {
                            Text(
                                text = viewModel.getString(stringKey),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Main Active Content Pane
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        Screen.Dashboard -> DashboardScreen(viewModel, bookings, clients, teamMembers, expenses, deliveries) { currentScreen = it }
                        Screen.CalendarBookings -> CalendarScreen(viewModel, bookings, teamMembers, clients)
                        Screen.Clients -> ClientsScreen(viewModel, clients)
                        Screen.Photographers -> StaffScreen(viewModel, teamMembers, "Photographer")
                        Screen.Videographers -> StaffScreen(viewModel, teamMembers, "Videographer")
                        Screen.Payments -> FinanceScreen(viewModel, clients, teamMembers, expenses, teamPayments)
                        Screen.Reports -> ReportsScreen(viewModel, bookings, expenses, teamMembers)
                        Screen.DataDelivery -> DataDeliveryScreen(viewModel, deliveries, bookings)
                        Screen.Notifications -> NotificationsScreen(viewModel, bookings, clients, deliveries)
                        Screen.Settings -> SettingsScreen(viewModel)
                    }
                }
            }

            // Dark semi-transparent background overlay/scrim on mobile menu open
            AnimatedVisibility(
                visible = showMobileMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            showMobileMenu = false
                        }
                )
            }

            // Mobile sliding custom sidebar drawer
            AnimatedVisibility(
                visible = showMobileMenu,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Sidebar(
                    currentScreen = currentScreen,
                    onScreenSelected = {
                        currentScreen = it
                        showMobileMenu = false
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

// Left Sidebar Menu UI
@Composable
fun Sidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    viewModel: LensoraViewModel
) {
    val items = listOf(
        Pair(Screen.Dashboard, Icons.Default.Dashboard to "dashboard"),
        Pair(Screen.CalendarBookings, Icons.Default.CalendarToday to "calendar_bookings"),
        Pair(Screen.Clients, Icons.Default.People to "clients"),
        Pair(Screen.Photographers, Icons.Default.CameraAlt to "photographers"),
        Pair(Screen.Videographers, Icons.Default.Videocam to "videographers"),
        Pair(Screen.Payments, Icons.Default.AccountBalanceWallet to "payments"),
        Pair(Screen.DataDelivery, Icons.Default.CloudUpload to "data_delivery"),
        Pair(Screen.Reports, Icons.Default.Assessment to "reports"),
        Pair(Screen.Notifications, Icons.Default.Notifications to "notifications"),
        Pair(Screen.Settings, Icons.Default.Settings to "settings")
    )

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Sidebar Premium Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = "Lensora",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Minimalist Studio SaaS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // Scrollable Menu Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEach { (screen, info) ->
                    val (icon, key) = info
                    val isSelected = currentScreen == screen
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable { onScreenSelected(screen) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = viewModel.getString(key),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = viewModel.getString(key),
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            // Footer displaying user profile detail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = viewModel.currentUserRole.take(1),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Current Account",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = viewModel.currentUserRole + " Mode",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 1: DASHBOARD
// ----------------------------------------------------
@Composable
fun DashboardScreen(
    viewModel: LensoraViewModel,
    bookings: List<Booking>,
    clients: List<Client>,
    teamMembers: List<TeamMember>,
    expenses: List<Expense>,
    deliveries: List<DataDelivery>,
    onNavigate: (Screen) -> Unit
) {
    // Computations
    val totalBookings = bookings.size
    val activeBookings = bookings.filter { it.status == "Confirmed" || it.status == "Tentative" }
    
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())
    
    val todayEvents = bookings.filter {
        sdf.format(Date(it.eventDate)) == todayStr && it.status != "Cancelled"
    }.size

    val upcomingEvents = bookings.filter {
        it.eventDate > System.currentTimeMillis() && it.status != "Cancelled"
    }.size

    val pendingPaymentsCount = clients.filter { it.remainingAmount > 0.0 }.size
    val completedEvents = bookings.filter { it.status == "Completed" }.size

    val deliveryPendingCount = deliveries.filter { it.deliveryStatus != "Delivered" }.size
    val deliveredCount = deliveries.filter { it.deliveryStatus == "Delivered" }.size

    // Financial calculations
    val totalRevenue = clients.sumOf { it.totalAmount }
    val totalExpensesValue = expenses.sumOf { it.amount } + teamMembers.sumOf { it.totalEarnings }
    val monthlyProfit = totalRevenue - totalExpensesValue

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hero Welcome Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Assalam-o-Alaikum, Studio Team!",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Manage bookings, client delivery links, and cashflow in real-time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Studio Camera",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Key Operational Indicators",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Dashboard KPI Widgets (Grid Layout)
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cardsPerRow = if (maxWidth > 600.dp) 3 else 2
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val kpis = listOf(
                        Triple(viewModel.getString("total_bookings"), totalBookings.toString(), Icons.Default.Collections),
                        Triple(viewModel.getString("upcoming_events"), upcomingEvents.toString(), Icons.Default.Event),
                        Triple(viewModel.getString("todays_events"), todayEvents.toString(), Icons.Default.Today),
                        Triple(viewModel.getString("pending_payments"), pendingPaymentsCount.toString(), Icons.Default.Pending),
                        Triple(viewModel.getString("completed_events"), completedEvents.toString(), Icons.Default.DoneAll),
                        Triple(viewModel.getString("delivery_pending"), deliveryPendingCount.toString(), Icons.Default.HourglassEmpty)
                    )
                    
                    kpis.chunked(cardsPerRow).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { (title, valStr, icon) ->
                                DashboardKpiCard(
                                    title = title,
                                    value = valStr,
                                    icon = icon,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size < cardsPerRow) {
                                repeat(cardsPerRow - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Financial Snapshot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialMiniCard(
                    title = viewModel.getString("total_revenue"),
                    amount = "Rs. %,.0f".format(totalRevenue),
                    color = Color.Green,
                    modifier = Modifier.weight(1f)
                )
                FinancialMiniCard(
                    title = viewModel.getString("total_expenses"),
                    amount = "Rs. %,.0f".format(totalExpensesValue),
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
                FinancialMiniCard(
                    title = viewModel.getString("monthly_profit"),
                    amount = "Rs. %,.0f".format(monthlyProfit),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Recent Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Automatic dashboard alerts (Event Tomorrow, Event Today, Unpaid balance)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val alerts = mutableListOf<@Composable () -> Unit>()

                // Today/Tomorrow event alerts
                bookings.forEach { booking ->
                    val isToday = sdf.format(Date(booking.eventDate)) == todayStr
                    val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                    val isTomorrow = sdf.format(Date(booking.eventDate)) == sdf.format(calTomorrow.time)

                    if (isToday) {
                        alerts.add {
                            AlertNotificationItem(
                                title = "EVENT TODAY: " + booking.title,
                                desc = "At ${booking.eventLocation} (${booking.eventTime})",
                                isDanger = true,
                                icon = Icons.Default.NotificationImportant
                            )
                        }
                    } else if (isTomorrow) {
                        alerts.add {
                            AlertNotificationItem(
                                title = "Event Tomorrow: " + booking.title,
                                desc = "Assigned Team: " + (if(booking.assignedPhotographerId != 0L) "Photog " else "") + (if(booking.assignedVideographerId != 0L) "Video " else ""),
                                isDanger = false,
                                icon = Icons.Default.NotificationImportant
                            )
                        }
                    }
                }

                // Payment Alert
                clients.filter { it.remainingAmount > 0.0 }.forEach { client ->
                    alerts.add {
                        AlertNotificationItem(
                            title = "Payment Pending: " + client.name,
                            desc = "Balance Outstanding: Rs. %,.0f".format(client.remainingAmount),
                            isDanger = false,
                            icon = Icons.Default.MoneyOff
                        )
                    }
                }

                if (alerts.isEmpty()) {
                    Text(
                        text = "Everything looks clean! No urgent notifications.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                } else {
                    alerts.take(4).forEach { it() }
                }
            }
        }
    }
}

@Composable
fun DashboardKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            shape = RoundedCornerShape(24.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FinancialMiniCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
fun AlertNotificationItem(
    title: String,
    desc: String,
    isDanger: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = if (isDanger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Alert icon",
                tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDanger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDanger) MaterialTheme.colorScheme.onErrorContainer.copy(0.8f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(0.8f)
                )
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 2: CALENDAR & BOOKINGS MODULE
// ----------------------------------------------------
@Composable
fun CalendarScreen(
    viewModel: LensoraViewModel,
    bookings: List<Booking>,
    teamMembers: List<TeamMember>,
    clients: List<Client>
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Navigation for calendar months
    var displayedMonthCal by remember { mutableStateOf(Calendar.getInstance()) }

    // Booking Dialog Forms
    var eventTitle by remember { mutableStateOf("") }
    var selectedClientName by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf(0L) }
    var eventType by remember { mutableStateOf("Wedding") }
    var eventTime by remember { mutableStateOf("18:00") }
    var eventLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var bookingStatus by remember { mutableStateOf("Confirmed") }

    // assigned team member IDs
    var pId by remember { mutableStateOf(0L) }
    var vId by remember { mutableStateOf(0L) }
    var aId by remember { mutableStateOf(0L) }
    var dId by remember { mutableStateOf(0L) }

    var pCount by remember { mutableStateOf("1") }
    var vCount by remember { mutableStateOf("1") }

    // Helpers to draw dates
    val sdfMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthLabel = sdfMonthName.format(displayedMonthCal.time)

    // Calculate days configuration
    val calTemp = displayedMonthCal.clone() as Calendar
    calTemp.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeekIndex = calTemp.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday, 1=Monday...
    val maxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH)

    val listDays = mutableListOf<Date?>()
    // Fill pre-offset blanks
    for (i in 0 until firstDayOfWeekIndex) {
        listDays.add(null)
    }
    // Fill actual days
    for (day in 1..maxDays) {
        val calculated = displayedMonthCal.clone() as Calendar
        calculated.set(Calendar.DAY_OF_MONTH, day)
        listDays.add(calculated.time)
    }

    // Bookings filtered for selected date
    val selectedSdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val selectedDayString = selectedSdf.format(selectedDate.time)
    val bookingsForSelectedDay = bookings.filter { booking ->
        selectedSdf.format(Date(booking.eventDate)) == selectedDayString
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calendar Title Block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.getString("calendar_bookings"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Confirm team member availability and look up dates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }

            if (viewModel.currentUserRole != "Staff") {
                Button(
                    onClick = {
                        // Reset forms
                        eventTitle = ""
                        selectedClientName = ""
                        selectedClientId = 0L
                        eventType = "Wedding"
                        eventLocation = ""
                        notes = ""
                        bookingStatus = "Confirmed"
                        pId = 0L
                        vId = 0L
                        aId = 0L
                        dId = 0L
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Booking")
                }
            }
        }

        // Calendar Grid Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month Header Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val prev = displayedMonthCal.clone() as Calendar
                        prev.add(Calendar.MONTH, -1)
                        displayedMonthCal = prev
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev")
                    }

                    Text(
                        text = monthLabel,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(onClick = {
                        val next = displayedMonthCal.clone() as Calendar
                        next.add(Calendar.MONTH, 1)
                        displayedMonthCal = next
                    }) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days names row
                val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayHeaders.forEach { header ->
                        Text(
                            text = header,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days grid
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val rows = listDays.chunked(7)
                    rows.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                if (date == null) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val dateCal = Calendar.getInstance().apply { time = date }
                                    val dayNum = dateCal.get(Calendar.DAY_OF_MONTH)
                                    val isSelected = selectedSdf.format(date) == selectedSdf.format(selectedDate.time)
                                    
                                    val hasEvents = bookings.any { b ->
                                        selectedSdf.format(Date(b.eventDate)) == selectedSdf.format(date) && b.status != "Cancelled"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                selectedDate = dateCal
                                            }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayNum.toString(),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp
                                            )
                                            if (hasEvents) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                            else MaterialTheme.colorScheme.tertiary
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (week.size < 7) {
                                repeat(7 - week.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date and Bookings Detail Title
        val parsedDateSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        Text(
            text = "Schedule for: " + parsedDateSdf.format(selectedDate.time),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (bookingsForSelectedDay.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No bookings scheduled on this date.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                bookingsForSelectedDay.forEach { booking ->
                    BookingListItem(
                        booking = booking,
                        teamMembers = teamMembers,
                        viewModel = viewModel,
                        onDelete = { viewModel.removeBooking(booking) }
                    )
                }
            }
        }
    }

    // CREATE BOOKING DIALOG
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Create New Studio Booking",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text(viewModel.getString("event_title")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Client Selection Dropdown Sim
                    Text(
                        text = "Client Relationship",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    var expandedClient by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedClient = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = if (selectedClientName.isEmpty()) "Select Client Profile" else selectedClientName)
                        }
                        DropdownMenu(
                            expanded = expandedClient,
                            onDismissRequest = { expandedClient = false }
                        ) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedClientId = c.id
                                        selectedClientName = c.name
                                        expandedClient = false
                                    }
                                )
                            }
                            // Fallback to allow custom input if list empty
                            DropdownMenuItem(
                                text = { Text("(Custom client / Walk-In)") },
                                onClick = {
                                    selectedClientId = 0L
                                    selectedClientName = "Custom Wallet"
                                    expandedClient = false
                                }
                            )
                        }
                    }

                    if (selectedClientName.isEmpty()) {
                        OutlinedTextField(
                            value = selectedClientName,
                            onValueChange = { selectedClientName = it },
                            label = { Text("Or Type Client Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Event types dropdown
                    val eventTypes = listOf("Wedding", "Mehndi", "Barat", "Walima", "Birthday", "Corporate Event", "Other")
                    var expandedType by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedType = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Type: $eventType")
                        }
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            eventTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        eventType = type
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = eventTime,
                            onValueChange = { eventTime = it },
                            label = { Text("Start Time (e.g. 18:00)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pCount,
                            onValueChange = { pCount = it },
                            label = { Text("Photogs Target") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = eventLocation,
                        onValueChange = { eventLocation = it },
                        label = { Text(viewModel.getString("location")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(viewModel.getString("notes")) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Text(
                        text = "Staff Assignments & Availability Tracker",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Assign Photographer Dropdown Sim
                    AssignTeamDropdown(
                        label = "Assign Lead Photographer",
                        selectedId = pId,
                        onIdSelected = { pId = it },
                        teamMembers = teamMembers.filter { it.role == "Photographer" },
                        date = selectedDate.timeInMillis,
                        viewModel = viewModel
                    )

                    // Assign Videographer Dropdown Sim
                    AssignTeamDropdown(
                        label = "Assign Lead Videographer",
                        selectedId = vId,
                        onIdSelected = { vId = it },
                        teamMembers = teamMembers.filter { it.role == "Videographer" },
                        date = selectedDate.timeInMillis,
                        viewModel = viewModel
                    )

                    AssignTeamDropdown(
                        label = "Assign Lighting/setup Assistant",
                        selectedId = aId,
                        onIdSelected = { aId = it },
                        teamMembers = teamMembers.filter { it.role == "Assistant" },
                        date = selectedDate.timeInMillis,
                        viewModel = viewModel
                    )

                    AssignTeamDropdown(
                        label = "Assign Drone Operator",
                        selectedId = dId,
                        onIdSelected = { dId = it },
                        teamMembers = teamMembers.filter { it.role == "Drone Operator" },
                        date = selectedDate.timeInMillis,
                        viewModel = viewModel
                    )

                    var expandedStatus by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedStatus = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Status: $bookingStatus")
                        }
                        DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                            listOf("Confirmed", "Tentative", "Completed", "Cancelled").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        bookingStatus = s
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                if (eventTitle.isNotEmpty() && selectedClientName.isNotEmpty()) {
                                    viewModel.addBooking(
                                        Booking(
                                            title = eventTitle,
                                            clientName = selectedClientName,
                                            clientId = selectedClientId,
                                            eventType = eventType,
                                            eventDate = selectedDate.timeInMillis,
                                            eventTime = eventTime,
                                            eventLocation = eventLocation,
                                            notes = notes,
                                            status = bookingStatus,
                                            assignedPhotographerId = pId,
                                            assignedVideographerId = vId,
                                            assignedAssistantId = aId,
                                            assignedDroneOperatorId = dId,
                                            photographerCount = pCount.toIntOrNull() ?: 1,
                                            videographerCount = vCount.toIntOrNull() ?: 1
                                        )
                                    )
                                    // Make toast feedback
                                    Toast.makeText(context, "Booking recorded!", Toast.LENGTH_SHORT).show()
                                    showAddDialog = false
                                } else {
                                    Toast.makeText(context, "Title & client required!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignTeamDropdown(
    label: String,
    selectedId: Long,
    onIdSelected: (Long) -> Unit,
    teamMembers: List<TeamMember>,
    date: Long,
    viewModel: LensoraViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedMember = teamMembers.find { it.id == selectedId }
    val isDoubleBooked = viewModel.isTeamMemberDoubleBooked(selectedId, date)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isDoubleBooked) Color(0xFFFFECEF) else Color.Transparent
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedMember?.name ?: "- Unassigned -",
                        color = if (isDoubleBooked) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    if (isDoubleBooked) {
                        Text(text = "Double Booked!", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("- Unassigned -") },
                    onClick = {
                        onIdSelected(0L)
                        expanded = false
                    }
                )
                teamMembers.forEach { m ->
                    val bookedElsewhere = viewModel.isTeamMemberDoubleBooked(m.id, date)
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(m.name)
                                if (bookedElsewhere) {
                                    Text(
                                        "⚠️ Occupied",
                                        color = Color(0xFFE65100),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        onClick = {
                            onIdSelected(m.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingListItem(
    booking: Booking,
    teamMembers: List<TeamMember>,
    viewModel: LensoraViewModel,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val photog = teamMembers.find { it.id == booking.assignedPhotographerId }
    val video = teamMembers.find { it.id == booking.assignedVideographerId }
    val assistant = teamMembers.find { it.id == booking.assignedAssistantId }
    val drone = teamMembers.find { it.id == booking.assignedDroneOperatorId }

    val statusColor = when (booking.status) {
        "Confirmed" -> Color(0xFF00C853)
        "Tentative" -> Color(0xFFFFAB00)
        "Completed" -> Color(0xFF2979FF)
        else -> Color(0xFFD50000) // Cancelled
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(0.4f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = booking.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Client: ${booking.clientName} | Type: ${booking.eventType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }

                Surface(
                    color = statusColor.copy(0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(16.dp))
                Text(text = booking.eventTime, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(imageVector = Icons.Default.Place, contentDescription = "Place", modifier = Modifier.size(16.dp))
                Text(
                    text = booking.eventLocation,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Google Map Trigger
                IconButton(onClick = {
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(booking.eventLocation)))
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        // Fallback browser search
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(booking.eventLocation)))
                        context.startActivity(webIntent)
                    }
                }, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = "Open in Maps", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (booking.notes.isNotEmpty()) {
                Text(
                    text = "Notes: " + booking.notes,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Crew Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Crew:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                val crewList = mutableListOf<String>()
                photog?.let { crewList.add("📸 ${it.name}") }
                video?.let { crewList.add("🎥 ${it.name}") }
                assistant?.let { crewList.add("💡 ${it.name}") }
                drone?.let { crewList.add("🛸 ${it.name}") }

                if (crewList.isEmpty()) {
                    Text(text = "No crew assigned yet.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                } else {
                    Text(
                        text = crewList.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (viewModel.currentUserRole == "Admin") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 3: CLIENT MANAGEMENT MODULE
// ----------------------------------------------------
@Composable
fun ClientsScreen(viewModel: LensoraViewModel, clients: List<Client>) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("Wedding") }
    var pkgName by remember { mutableStateOf("") }
    var totalAmt by remember { mutableStateOf("") }
    var advanceReceivedAmt by remember { mutableStateOf("") }

    val filteredClients = clients.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.getString("clients") + " Directory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Track advance payments, package info, and WhatsApp directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }

            if (viewModel.currentUserRole != "Staff") {
                Button(
                    onClick = {
                        name = ""
                        phone = ""
                        whatsapp = ""
                        email = ""
                        address = ""
                        cnic = ""
                        pkgName = ""
                        totalAmt = ""
                        advanceReceivedAmt = ""
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Client")
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search clients by name, phone, locations...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredClients) { client ->
                ClientCardItem(client = client, viewModel = viewModel, onDelete = { viewModel.removeClient(client) })
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Register Video/Photo Client",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Couple Name / Client Name") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
                    OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Contact") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email ID") })
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Studio Billing Address") })
                    OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text("CNIC Number (Optional)") })
                    OutlinedTextField(value = pkgName, onValueChange = { pkgName = it }, label = { Text("Contract Package Name") })

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = totalAmt, onValueChange = { totalAmt = it }, label = { Text("Total Quote Amount") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = advanceReceivedAmt, onValueChange = { advanceReceivedAmt = it }, label = { Text("Advance Received") }, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAddDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && phone.isNotEmpty()) {
                                    val tot = totalAmt.toDoubleOrNull() ?: 0.0
                                    val adv = advanceReceivedAmt.toDoubleOrNull() ?: 0.0
                                    viewModel.addClient(
                                        Client(
                                            name = name,
                                            phone = phone,
                                            whatsapp = whatsapp,
                                            email = email,
                                            address = address,
                                            cnic = cnic,
                                            packageName = pkgName,
                                            totalAmount = tot,
                                            advanceReceived = adv,
                                            remainingAmount = tot - adv
                                        )
                                    )
                                    showAddDialog = false
                                } else {
                                    Toast.makeText(context, "Full Name & phone are mandatory!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Client")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCardItem(client: Client, viewModel: LensoraViewModel, onDelete: () -> Unit) {
    val context = LocalContext.current
    val balanceColor = if (client.remainingAmount > 0.0) Color(0xFFE65100) else Color(0xFF00C853)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = client.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (client.cnic.isNotEmpty()) {
                        Text(text = "CNIC: ${client.cnic}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // WhatsApp Trigger Link
                    IconButton(onClick = {
                        val trimmedContact = client.whatsapp.replace("+", "").replace("-", "")
                        val playUrl = "https://api.whatsapp.com/send?phone=$trimmedContact&text=Hello%20${client.name},%20this%20is%20Lensora%20Creative%20Studio%20regarding%20your%20events."
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playUrl))
                        context.startActivity(intent)
                    }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "whatsapp", tint = Color(0xFF25D366))
                    }

                    // Direct Phone dial
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                        context.startActivity(intent)
                    }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "call", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Contract details: ${client.packageName}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Contact Phone: ${client.phone}", style = MaterialTheme.typography.bodySmall)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Unpaid: Rs. %,.0f".format(client.remainingAmount), fontWeight = FontWeight.Bold, color = balanceColor, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Paid Adv: Rs. %,.0f / Rs. %,.0f".format(client.advanceReceived, client.totalAmount), style = MaterialTheme.typography.labelSmall)
                }
            }

            if (viewModel.currentUserRole == "Admin") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDelete) {
                        Text("Remove Record", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 4: TEAM MANAGEMENT (PHOTOGRAPHER / VIDEOGRAPHER)
// ----------------------------------------------------
@Composable
fun StaffScreen(
    viewModel: LensoraViewModel,
    teamMembers: List<TeamMember>,
    roleFilter: String
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var avail by remember { mutableStateOf("Available") }

    val members = teamMembers.filter { it.role == roleFilter }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (roleFilter == "Photographer") viewModel.getString("photographers") else viewModel.getString("videographers"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Track events assignments, specialties, and pending pay balance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }

            if (viewModel.currentUserRole == "Admin") {
                Button(
                    onClick = {
                        name = ""
                        phone = ""
                        experience = ""
                        specialty = ""
                        avail = "Available"
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Staff")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(members) { staff ->
                StaffCardItem(staff = staff, viewModel = viewModel, onDelete = { viewModel.removeTeamMember(staff) })
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add Studio Crew Profile ($roleFilter)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Contact") })
                    OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("Experience Year (e.g. 5 Years)") })
                    OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Specialty Style (e.g. Cinematic)") })

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAddDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                if (name.isNotEmpty() && phone.isNotEmpty()) {
                                    viewModel.addTeamMember(
                                        TeamMember(
                                            name = name,
                                            phone = phone,
                                            role = roleFilter,
                                            experience = experience,
                                            specialty = specialty,
                                            availabilityStatus = "Available"
                                        )
                                    )
                                    showAddDialog = false
                                } else {
                                    Toast.makeText(context, "Full Crew Name & phone required!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Crew")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffCardItem(staff: TeamMember, viewModel: LensoraViewModel, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = staff.name.firstOrNull()?.toString() ?: "?",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.wrapContentSize(Alignment.Center)
                        )
                    }

                    Column {
                        Text(text = staff.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(text = "${staff.experience} Exp | ${staff.specialty}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Surface(
                    color = if (staff.availabilityStatus == "Available") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = staff.availabilityStatus,
                        color = if (staff.availabilityStatus == "Available") Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Phone Line: ${staff.phone}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Total Events Worked: ${staff.eventsWorked}", style = MaterialTheme.typography.bodySmall)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Paid Earnings: Rs. %,.0f".format(staff.totalEarnings), style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Pending Payment: Rs. %,.0f".format(staff.pendingPayments),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (staff.pendingPayments > 0) Color.Red else Color.Green
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Change status toggle
                TextButton(onClick = {
                    val nextAvail = if (staff.availabilityStatus == "Available") "Busy" else "Available"
                    viewModel.modifyTeamMember(staff.copy(availabilityStatus = nextAvail))
                }) {
                    Text("Toggle Availability")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${staff.phone}"))
                        context.startActivity(intent)
                    }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "call", tint = MaterialTheme.colorScheme.primary)
                    }

                    if (viewModel.currentUserRole == "Admin") {
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 5: FINANCE & PAYMENTS
// ----------------------------------------------------
@Composable
fun FinanceScreen(
    viewModel: LensoraViewModel,
    clients: List<Client>,
    teamMembers: List<TeamMember>,
    expenses: List<Expense>,
    teamPayments: List<TeamPayment>
) {
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showPayDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Combined stats
    val totalRevenue = clients.sumOf { it.totalAmount }
    val totalCrewPayments = teamMembers.sumOf { it.totalEarnings }
    val totalGeneralExpenses = expenses.sumOf { it.amount }
    val totalExpensesValue = totalCrewPayments + totalGeneralExpenses
    val netProfit = totalRevenue - totalExpensesValue

    // Expense Forms
    var expenseType by remember { mutableStateOf("Fuel") }
    var expenseDesc by remember { mutableStateOf("") }
    var expenseAmt by remember { mutableStateOf("") }

    // Crew Pay Forms
    var selectedStaffId by remember { mutableStateOf(0L) }
    var selectedStaffName by remember { mutableStateOf("") }
    var payAmt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Financial Studio Ledger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "File expenses, reward photographers, and calculate dynamic net profits.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }

        // Executive Ledger cards
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LENSORA LEDGER NET PROFITS",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Rs. %,.0f".format(netProfit),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.2f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Total Studio Receipts", style = MaterialTheme.typography.labelMedium)
                        Text(text = "Rs. %,.0f".format(totalRevenue), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Aggregated Expenses", style = MaterialTheme.typography.labelMedium)
                        Text(text = "Rs. %,.0f".format(totalExpensesValue), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Red)
                    }
                }
            }
        }

        // Finance Actions
        if (viewModel.currentUserRole != "Staff") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        expenseDesc = ""
                        expenseAmt = ""
                        showExpenseDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Expense")
                }

                Button(
                    onClick = {
                        selectedStaffId = 0L
                        selectedStaffName = ""
                        payAmt = ""
                        showPayDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(imageVector = Icons.Default.Wallet, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay Team Member")
                }
            }
        }

        // Subtitled list for expenditures
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Expenditures",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "General Expenses", style = MaterialTheme.typography.labelMedium)
        }

        if (expenses.isEmpty()) {
            Text(text = "No recorded general expenses yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                expenses.take(5).forEach { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = when (exp.type) {
                                        "Fuel" -> Icons.Default.LocalGasStation
                                        "Food" -> Icons.Default.Restaurant
                                        "Travel" -> Icons.Default.FlightTakeoff
                                        "Equipment Rental" -> Icons.Default.Videocam
                                        else -> Icons.Default.Category
                                    },
                                    contentDescription = exp.type,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(text = exp.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = exp.type, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                            Text(text = "- Rs. %,.0f".format(exp.amount), color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Subtitled list for Crew Payments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Payments Ledger",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "Team payroll", style = MaterialTheme.typography.labelMedium)
        }

        if (teamPayments.isEmpty()) {
            Text(text = "No crew payments generated yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                teamPayments.take(5).forEach { pay ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = pay.teamMemberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Remaining balance: Rs. %,.0f".format(pay.remainingAmount), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Text(text = "Paid Rs. %,.0f".format(pay.paidAmount), color = Color.Green, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ADD EXPENSE DIALOG
    if (showExpenseDialog) {
        Dialog(onDismissRequest = { showExpenseDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Log Studio Expenditure", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    val types = listOf("Fuel", "Food", "Travel", "Equipment Rental", "Other")
                    var expandedType by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedType = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Category: $expenseType")
                        }
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            types.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = {
                                    expenseType = t
                                    expandedType = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(value = expenseDesc, onValueChange = { expenseDesc = it }, label = { Text("Expenditure Description / details") })
                    OutlinedTextField(value = expenseAmt, onValueChange = { expenseAmt = it }, label = { Text("Amount spent (Rs.)") })

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showExpenseDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                val amtStr = expenseAmt.toDoubleOrNull()
                                if (expenseDesc.isNotEmpty() && amtStr != null) {
                                    viewModel.addExpense(Expense(type = expenseType, description = expenseDesc, amount = amtStr))
                                    showExpenseDialog = false
                                } else {
                                    Toast.makeText(context, "Fill fields correctly!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Ledger")
                        }
                    }
                }
            }
        }
    }

    // PAY TEAM MEMBER DIALOG
    if (showPayDialog) {
        Dialog(onDismissRequest = { showPayDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Disburse Team Remuneration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    var expandedStaff by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedStaff = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = if (selectedStaffName.isEmpty()) "Choose Staff Crew member" else selectedStaffName)
                        }
                        DropdownMenu(expanded = expandedStaff, onDismissRequest = { expandedStaff = false }) {
                            teamMembers.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.name} (${m.role}) - Unpaid: Rs. ${m.pendingPayments}") },
                                    onClick = {
                                        selectedStaffId = m.id
                                        selectedStaffName = m.name
                                        expandedStaff = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(value = payAmt, onValueChange = { payAmt = it }, label = { Text("Transfer amount (Rs.)") })

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showPayDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                val amtStr = payAmt.toDoubleOrNull()
                                val selectedMember = teamMembers.find { it.id == selectedStaffId }
                                if (selectedMember != null && amtStr != null) {
                                    val remaining = (selectedMember.pendingPayments - amtStr).coerceAtLeast(0.0)
                                    viewModel.addTeamPayment(
                                        TeamPayment(
                                            teamMemberId = selectedMember.id,
                                            teamMemberName = selectedMember.name,
                                            agreedAmount = selectedMember.totalEarnings + selectedMember.pendingPayments,
                                            paidAmount = amtStr,
                                            remainingAmount = remaining
                                        )
                                    )
                                    showPayDialog = false
                                } else {
                                    Toast.makeText(context, "Select crew member & transfer amount!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Complete Pay")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 6: REPORTS & EXPORTS
// ----------------------------------------------------
@Composable
fun ReportsScreen(
    viewModel: LensoraViewModel,
    bookings: List<Booking>,
    expenses: List<Expense>,
    teamMembers: List<TeamMember>
) {
    var showExportOverlay by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("PDF") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Aggregated Management Reports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Export photography logs, monthly profit calculations and staff statistics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }

        // Performance grids
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Photographer Performance Index",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                teamMembers.filter { it.role == "Photographer" }.forEach { m ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = m.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${m.eventsWorked} Events | Earnings: Rs. %,.0f".format(m.totalEarnings), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Videographer Performance Index",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                teamMembers.filter { it.role == "Videographer" }.forEach { m ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = m.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${m.eventsWorked} Events | Earnings: Rs. %,.0f".format(m.totalEarnings), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Operational Statistics Reports",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Assigned Active Events & Bookings:")
                    Text(text = bookings.size.toString(), fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Total Expenditure general logs:")
                    Text(text = "Rs. %,.0f".format(expenses.sumOf { it.amount }), fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider()

        Text(
            text = "Generate and Export Documents",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    exportFormat = "PDF"
                    showExportOverlay = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF Report")
            }

            Button(
                onClick = {
                    exportFormat = "Excel"
                    showExportOverlay = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = "Excel")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Excel XLS")
            }
        }
    }

    if (showExportOverlay) {
        Dialog(onDismissRequest = { showExportOverlay = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (exportFormat == "PDF") Icons.Default.PictureAsPdf else Icons.Default.GridOn,
                        contentDescription = "Export file icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "Lensora report generated successfully!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Format: $exportFormat Document\nFile Name: lensora_studio_report_${System.currentTimeMillis() / 1000}.${exportFormat.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "$exportFormat Report shared successfully!", Toast.LENGTH_SHORT).show()
                            showExportOverlay = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share / Save File")
                    }

                    OutlinedButton(
                        onClick = { showExportOverlay = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 7: DATA DELIVERY TRACKER
// ----------------------------------------------------
@Composable
fun DataDeliveryScreen(
    viewModel: LensoraViewModel,
    deliveries: List<DataDelivery>,
    bookings: List<Booking>
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingDelivery by remember { mutableStateOf<DataDelivery?>(null) }
    val context = LocalContext.current

    // Fields for Edit Form
    var statusStr by remember { mutableStateOf("Not Started") }
    var photosDel by remember { mutableStateOf(false) }
    var videosDel by remember { mutableStateOf(false) }
    var albumDel by remember { mutableStateOf(false) }
    var gDriveLink by remember { mutableStateOf("") }
    var dropboxLink by remember { mutableStateOf("") }
    var downloadLink by remember { mutableStateOf("") }
    var clientConfirmedCheck by remember { mutableStateOf(false) }

    val filteredDeliveries = deliveries.filter {
        it.clientName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = "Client Data Delivery Tracker",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Dispatch Google Drive / Dropbox download repositories directly to clients.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search deliveries by client couples...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredDeliveries) { delivery ->
                val statusBgColor = when (delivery.deliveryStatus) {
                    "Delivered" -> Color(0xFFE8F5E9)
                    "Ready" -> Color(0xFFFFF3E0)
                    "Editing" -> Color(0xFFE3F2FD)
                    else -> Color(0xFFECEFF1)
                }

                val statusTextColor = when (delivery.deliveryStatus) {
                    "Delivered" -> Color(0xFF2E7D32)
                    "Ready" -> Color(0xFFE65100)
                    "Editing" -> Color(0xFF1565C0)
                    else -> Color(0xFF37474F)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = delivery.clientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Related Booking ID: #${delivery.bookingId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }

                            Surface(
                                color = statusBgColor,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = delivery.deliveryStatus,
                                    color = statusTextColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CheckboxWithLabel(label = "Photos Delivered", checked = delivery.photosDelivered, onCheckedChange = {})
                            CheckboxWithLabel(label = "Videos Delivered", checked = delivery.videosDelivered, onCheckedChange = {})
                            CheckboxWithLabel(label = "Album Delivered", checked = delivery.albumDelivered, onCheckedChange = {})
                        }

                        if (delivery.googleDriveLink.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Google Drive Repository URL:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(delivery.googleDriveLink))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Open Drive Folder", fontSize = 10.sp)
                                }
                            }
                        }

                        if (viewModel.currentUserRole != "Staff") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        editingDelivery = delivery
                                        statusStr = delivery.deliveryStatus
                                        photosDel = delivery.photosDelivered
                                        videosDel = delivery.videosDelivered
                                        albumDel = delivery.albumDelivered
                                        gDriveLink = delivery.googleDriveLink
                                        dropboxLink = delivery.dropboxLink
                                        downloadLink = delivery.downloadLink
                                        clientConfirmedCheck = delivery.clientConfirmed
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Delivery info", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Update Tracker")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingDelivery != null) {
        Dialog(onDismissRequest = { editingDelivery = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Update Delivery Status: " + (editingDelivery?.clientName ?: ""), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    // Status selection dropdown
                    val statuses = listOf("Not Started", "Editing", "Ready", "Delivered")
                    var expandedStatus by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expandedStatus = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Dispatch Stage: $statusStr")
                        }
                        DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                            statuses.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = {
                                    statusStr = s
                                    expandedStatus = false
                                })
                            }
                        }
                    }

                    // Checklist options
                    CheckboxWithLabel(label = "Photos editing finalized", checked = photosDel, onCheckedChange = { photosDel = it })
                    CheckboxWithLabel(label = "Videos grading/music layout finalized", checked = videosDel, onCheckedChange = { videosDel = it })
                    CheckboxWithLabel(label = "Physical album delivered to client", checked = albumDel, onCheckedChange = { albumDel = it })

                    HorizontalDivider()

                    OutlinedTextField(value = gDriveLink, onValueChange = { gDriveLink = it }, label = { Text("Google Drive Folder Link") })
                    OutlinedTextField(value = dropboxLink, onValueChange = { dropboxLink = it }, label = { Text("Dropbox Folder Link") })
                    OutlinedTextField(value = downloadLink, onValueChange = { downloadLink = it }, label = { Text("Universal Download link") })

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = clientConfirmedCheck, onCheckedChange = { clientConfirmedCheck = it })
                        Text(text = "☑ Client Received Data", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { editingDelivery = null }, modifier = Modifier.weight(1f)) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                editingDelivery?.let { entry ->
                                    viewModel.modifyDataDelivery(
                                        entry.copy(
                                            deliveryStatus = statusStr,
                                            photosDelivered = photosDel,
                                            videosDelivered = videosDel,
                                            albumDelivered = albumDel,
                                            googleDriveLink = gDriveLink,
                                            dropboxLink = dropboxLink,
                                            downloadLink = downloadLink,
                                            clientConfirmed = clientConfirmedCheck
                                        )
                                    )
                                    editingDelivery = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Commit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckboxWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.size(24.dp))
        Text(text = label, fontSize = 11.sp, maxLines = 1)
    }
}

// ----------------------------------------------------
// SCREEN 8: SYSTEM NOTIFICATIONS
// ----------------------------------------------------
@Composable
fun NotificationsScreen(
    viewModel: LensoraViewModel,
    bookings: List<Booking>,
    clients: List<Client>,
    deliveries: List<DataDelivery>
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    val alerts = mutableListOf<Triple<String, String, Boolean>>()

    bookings.forEach { booking ->
        val isToday = sdf.format(Date(booking.eventDate)) == todayStr
        val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = sdf.format(Date(booking.eventDate)) == sdf.format(calTomorrow.time)

        if (isToday) {
            alerts.add(Triple("EVENT TODAY: ${booking.title}", "Schedule location: ${booking.eventLocation} at ${booking.eventTime}", true))
        } else if (isTomorrow) {
            alerts.add(Triple("Event Tomorrow: ${booking.title}", "Please secure lenses, lighting equipment and clear storage memory cards.", false))
        }
    }

    clients.filter { it.remainingAmount > 0.0 }.forEach { client ->
        alerts.add(Triple("Outstanding Client Balance: ${client.name}", "Remaining outstanding amount: Rs. %,.0f. Contact client via Whatsapp link.".format(client.remainingAmount), false))
    }

    deliveries.filter { it.deliveryStatus != "Delivered" }.forEach { del ->
        alerts.add(Triple("Delivery Tracker outstanding: ${del.clientName}", "Processing Stage: ${del.deliveryStatus}. Drive links upload needed.", false))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = "Notifications & Alerts Room",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Actionable checklist items derived from dynamic studio state.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }

        if (alerts.isEmpty()) {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text(text = "Studio is completely clear! No alerts.", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alerts) { (title, desc, isDanger) ->
                    AlertNotificationItem(title = title, desc = desc, isDanger = isDanger, icon = Icons.Default.CircleNotifications)
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 9: SETTINGS
// ----------------------------------------------------
@Composable
fun SettingsScreen(viewModel: LensoraViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Lensora Configurations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bilingual configuration, user access controls, and cloud database operations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }

        // Locale Language Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Localization (Language / زبان)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select active dashboard localization dialect. Interactive language switching.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.currentLanguage = "en" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.currentLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (viewModel.currentLanguage == "en") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("English Language")
                    }

                    Button(
                        onClick = { viewModel.currentLanguage = "ur" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.currentLanguage == "ur") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (viewModel.currentLanguage == "ur") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("اردو زبان")
                    }
                }
            }
        }

        // Access Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Operational User Access Roles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Switch active profile credentials to simulate distinct workspace clearance levels.",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roles = listOf("Admin", "Manager", "Staff")
                    roles.forEach { role ->
                        Button(
                            onClick = { viewModel.currentUserRole = role },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.currentUserRole == role) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (viewModel.currentUserRole == role) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(role, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Cloud Operations Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Backup, Restore & Cloud Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Re-seed professional demo portfolio data instantly inside the local Room persistence cache.",
                    style = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = {
                        viewModel.triggerBackupAndRestore {
                            Toast.makeText(context, "Cloud sync finalized! Demo portfolio loaded.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Cloud Restore & Seed Data")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Lensora Studio v1.0.0", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(text = "Secure local state persistence | Built with Jetpack Compose & M3", style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
            }
        }
    }
}
