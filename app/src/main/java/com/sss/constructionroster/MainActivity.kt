package com.sss.constructionroster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sss.constructionroster.ui.theme.ConstructionRosterTheme

import kotlinx.datetime.*
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.ui.text.style.TextAlign
import com.sss.constructionroster.data.PaymentDataManager


class MainActivity : ComponentActivity() {
    private lateinit var paymentDataManager: PaymentDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentDataManager = PaymentDataManager(this)
        
        enableEdgeToEdge()
        setContent {
            ConstructionRosterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var selectedImage by remember { mutableStateOf<ImageItem?>(null) }
                    var selectedMonthImage by remember { 
                        mutableStateOf(paymentDataManager.loadMonthImageSelection()) 
                    }
                    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        when {
                            selectedDate != null -> {
                                DateDetailsScreen(
                                    date = selectedDate!!,
                                    amountPerDay = selectedMonthImage?.amountPerDay ?: "",
                                    monthImage = selectedMonthImage!!,
                                    onDismiss = { 
                                        selectedMonthImage?.let { paymentDataManager.saveMonthImageSelection(it) }
                                        selectedDate = null 
                                    },
                                    onSubmit = { dateDetails ->
                                        selectedMonthImage = selectedMonthImage?.copy(
                                            dayEntries = selectedMonthImage?.dayEntries?.apply {
                                                if (dateDetails.isAbsent) {
                                                    // If marked as absent, store with empty amount paid
                                                    put(dateDetails.date.dayOfMonth, DayEntry(
                                                        date = dateDetails.date,
                                                        amountPaid = "",
                                                        isAbsent = true
                                                    ))
                                                } else if (dateDetails.amountPaid.isNotBlank()) {
                                                    // If amount paid is provided, store the entry
                                                    put(dateDetails.date.dayOfMonth, DayEntry(
                                                        date = dateDetails.date,
                                                        amountPaid = dateDetails.amountPaid,
                                                        isAbsent = false
                                                    ))
                                                } else {
                                                    // If no amount and not absent, remove the entry
                                                    remove(dateDetails.date.dayOfMonth)
                                                }
                                            } ?: mutableMapOf()
                                        )
                                        selectedMonthImage?.let { paymentDataManager.saveMonthImageSelection(it) }
                                        selectedDate = null
                                    }
                                )
                            }
                            selectedMonthImage != null -> {
                                MonthImageScreen(
                                    monthImage = selectedMonthImage!!,
                                    onDismiss = { 
                                        selectedMonthImage?.let { paymentDataManager.saveMonthImageSelection(it) }
                                        selectedMonthImage = null 
                                    },
                                    onDateSelected = { date -> selectedDate = date },
                                    onAmountPerDayChange = { amount ->
                                        selectedMonthImage = selectedMonthImage?.copy(
                                            amountPerDay = amount
                                        )
                                        selectedMonthImage?.let { paymentDataManager.saveMonthImageSelection(it) }
                                    }
                                )
                            }
                            else -> {
                                ImageGrid(
                                    modifier = Modifier.weight(1f),
                                    onImageSelected = { imageItem ->
                                        selectedImage = imageItem
                                    }
                                )

                                selectedImage?.let { image ->
                                    SelectedImageDisplay(
                                        imageRes = image.imageRes,
                                        title = image.title,
                                        onDismiss = { 
                                            selectedMonthImage?.let { paymentDataManager.saveMonthImageSelection(it) }
                                            selectedImage = null 
                                        }
                                    )
                                    MonthCalendar(
                                        onMonthSelected = { month ->
                                            val existingData = paymentDataManager.loadMonthImageSelection(
                                                imageRes = image.imageRes,
                                                month = month
                                            )
                                            selectedMonthImage = existingData ?: MonthImageSelection(
                                                month = month,
                                                imageItem = image,
                                                amountPerDay = ""
                                            )
                                            if (existingData == null) {
                                                paymentDataManager.saveMonthImageSelection(selectedMonthImage!!)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ImageItem(
    val imageRes: Int,
    val title: String
)

val sampleImages = listOf(
    ImageItem(R.drawable.sample_image1, "Sample 1"),
    ImageItem(R.drawable.sample_image2, "Sample 2"),
    ImageItem(R.drawable.sample_image3, "Sample 3"),
    ImageItem(R.drawable.sample_image4, "Sample 4"),
    ImageItem(R.drawable.sample_image5, "Sample 5"),
    ImageItem(R.drawable.sample_image6, "Sample 6")
)

data class SelectedImage(
    val imageRes: Int,
    val title: String,
    val isVisible: Boolean
)

data class MonthImageSelection(
    val month: Month,
    val imageItem: ImageItem,
    val amountPerDay: String = "",
    val dayEntries: MutableMap<Int, DayEntry> = mutableMapOf()
)

@Composable
fun ImageGrid(
    modifier: Modifier = Modifier,
    onImageSelected: (ImageItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(sampleImages) { item ->
            ImageCard(
                item = item,
                onImageClick = { onImageSelected(item) }
            )
        }
    }
}

@Composable
fun ImageCard(
    item: ImageItem,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onImageClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SelectedImageDisplay(
    imageRes: Int,
    title: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Add back button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun MonthCalendar(
    onMonthSelected: (Month) -> Unit
) {
    var selectedMonth by remember { mutableStateOf<Month?>(null) }
    val currentYear = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .year

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Color(0xFF000000), // Light green background
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Calendar $currentYear",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32) // Darker green for title
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Month.values().toList()) { month ->
                MonthCard(
                    month = month,
                    year = currentYear,
                    isSelected = month == selectedMonth,
                    onMonthClick = { 
                        selectedMonth = month
                        onMonthSelected(month)
                    }
                )
            }
        }
    }
}

@Composable
fun MonthCard(
    month: Month,
    year: Int,
    isSelected: Boolean,
    onMonthClick: () -> Unit
) {
    val date = LocalDate(year, month.number, 1)
    val backgroundColor = if (isSelected) {
        Color(0xFF81C784) // Selected green
    } else {
        Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onMonthClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFAED581),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = month.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Color(0xFF33691E)
                )
            )
            Text(
                text = "${month.number}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = if (isSelected) Color.White else Color(0xFF33691E)
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "Starts ${date.dayOfWeek.name.take(3)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF558B2F)
                )
            )
        }
    }
}

@Composable
fun MonthImageScreen(
    monthImage: MonthImageSelection,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAmountPerDayChange: (String) -> Unit
) {
    var amountPerDay by remember(monthImage.amountPerDay) { 
        mutableStateOf(monthImage.amountPerDay) 
    }
    val currentYear = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .year

    // Calculate totals
    val daysInMonth = when (monthImage.month.number) {
        2 -> if (isLeapYear(currentYear)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    val totalPossibleAmount = amountPerDay.toIntOrNull()?.times(daysInMonth) ?: 0
    val totalPaidAmount = monthImage.dayEntries.values.sumOf { it.amountPaid.toIntOrNull() ?: 0 }
    val absentDaysAmount = monthImage.dayEntries.values.count { it.isAbsent } * (amountPerDay.toIntOrNull() ?: 0)
    val remainingAmount = totalPossibleAmount - totalPaidAmount - absentDaysAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "${monthImage.month.name} $currentYear",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Profile Image Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Image(
                painter = painterResource(id = monthImage.imageItem.imageRes),
                contentDescription = monthImage.imageItem.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Amount per day and Month Total Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Amount per day input (smaller)
                OutlinedTextField(
                    value = amountPerDay,
                    onValueChange = { 
                        amountPerDay = it
                        onAmountPerDayChange(it)
                    },
                    modifier = Modifier.weight(0.4f),
                    label = { Text("Per Day") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                // Totals Column
                Column(
                    modifier = Modifier.weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Month Total
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Month Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹$totalPossibleAmount",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                    
                    // Paid Amount
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Paid Amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹$totalPaidAmount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                    
                    // Remaining Amount
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹$remainingAmount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
        }

        // Calendar Grid
        MonthDaysGrid(
            month = monthImage.month,
            year = currentYear,
            monthImage = monthImage,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun MonthDaysGrid(
    month: Month,
    year: Int,
    monthImage: MonthImageSelection,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = LocalDate(year, month.number, 1)
    val daysInMonth = when (month.number) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Month and Year header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${month.name} (${month.number}) $year",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }

        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid with fixed height calculation
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp) // Fixed height to ensure all weeks are visible
        ) {
            // Empty cells before first day
            items(firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            // Days of the month
            items(daysInMonth) { day ->
                val dayOfMonth = day + 1
                val dayEntry = monthImage.dayEntries[dayOfMonth]
                DayCell(
                    day = dayOfMonth,
                    isToday = isToday(year, month.number, dayOfMonth),
                    dayEntry = dayEntry,
                    onClick = { onDateSelected(LocalDate(year, month.number, dayOfMonth)) }
                )
            }

            // Add empty cells to complete the grid if needed
            val totalCells = firstDayOfWeek + daysInMonth
            val remainingCells = (7 - (totalCells % 7)) % 7
            items(remainingCells) {
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    dayEntry: DayEntry?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                dayEntry?.isAbsent == true -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                dayEntry != null -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isToday)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                ),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (dayEntry != null) {
                if (dayEntry.isAbsent) {
                    Text(
                        text = "Absent",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (dayEntry.amountPaid.isNotBlank()) {
                    Text(
                        text = "₹${dayEntry.amountPaid}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun isToday(year: Int, month: Int, day: Int): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return today.year == year && today.monthNumber == month && today.dayOfMonth == day
}

// Add this helper function to check for leap years
private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}

// Add this data class for date details
data class DateDetails(
    val date: LocalDate,
    val amountPerDay: String,
    val amountPaid: String = "",
    val isAbsent: Boolean = false
)

// Add this data class to track daily entries
data class DayEntry(
    val date: LocalDate,
    val amountPaid: String,
    val isAbsent: Boolean
)

// Update DateDetailsScreen to show total calculations
@Composable
fun DateDetailsScreen(
    date: LocalDate,
    amountPerDay: String,
    monthImage: MonthImageSelection,
    onDismiss: () -> Unit,
    onSubmit: (DateDetails) -> Unit
) {
    var amountPaid by remember { mutableStateOf("") }
    var isAbsent by remember { mutableStateOf(false) }

    // Calculate totals
    val daysInMonth = when (date.monthNumber) {
        2 -> if (isLeapYear(date.year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    
    val totalPossibleAmount = amountPerDay.toIntOrNull()?.times(daysInMonth) ?: 0
    val totalPaidAmount = monthImage.dayEntries.values.sumOf { it.amountPaid.toIntOrNull() ?: 0 }
    val absentDaysAmount = monthImage.dayEntries.values.count { it.isAbsent } * (amountPerDay.toIntOrNull() ?: 0)
    val remainingAmount = totalPossibleAmount - totalPaidAmount - absentDaysAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "${date.month.name} ${date.dayOfMonth}, ${date.year}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Profile Image Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Image(
                painter = painterResource(id = monthImage.imageItem.imageRes),
                contentDescription = monthImage.imageItem.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Amount per day with Total
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Amount per day and Month Total in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountPerDay,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        label = { Text("Amount per day") },
                        readOnly = true,
                        enabled = false,
                        singleLine = true
                    )
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Month Total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹$totalPossibleAmount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Payment details in a more compact layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paid:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "₹$totalPaidAmount",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Absent:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${monthImage.dayEntries.values.count { it.isAbsent }}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remaining:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "₹$remainingAmount",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Absent Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAbsent,
                onCheckedChange = { isChecked -> 
                    isAbsent = isChecked
                    if (isChecked) {
                        // Clear amount paid when marked as absent
                        amountPaid = ""
                    } else {
                        // When unchecking absent, initialize with default amount per day
                        amountPaid = amountPerDay
                    }
                }
            )
            Text(
                text = "Absent",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Amount Paid - disabled when absent
        OutlinedTextField(
            value = amountPaid,
            onValueChange = { if (!isAbsent) amountPaid = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            label = { Text("Amount Paid") },
            enabled = !isAbsent,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        )

        // Submit Button
        Button(
            onClick = {
                onSubmit(
                    DateDetails(
                        date = date,
                        amountPerDay = amountPerDay,
                        amountPaid = amountPaid,
                        isAbsent = isAbsent
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Submit")
        }
    }
}