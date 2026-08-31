package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.viewmodel.UserRole
import com.example.util.CurrencyUtils

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Paid", "Completed", "Approved", "Accepted" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "Sent", "Viewed", "In Progress", "Development Started" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        "Partially Paid", "Pending", "Pending Admin Approval", "Requirement Received" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        "Unpaid", "Rejected", "Discount Rejected", "On Hold" -> Color(0xFFFFE4E6) to Color(0xFFBE123C)
        "Revision Requested" -> Color(0xFFF3E8FF) to Color(0xFF7E22CE)
        "Converted to Invoice" -> Color(0xFFCCFBF1) to Color(0xFF0F766E)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.testTag("status_badge_$status")
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    Surface(
        color = Color(role.badgeColor).copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(role.badgeColor).copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(role.badgeColor))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = role.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(role.badgeColor)
            )
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subValue: String? = null,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metric_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subValue != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MonthlySalesBarChart(
    data: List<Pair<String, Double>>, // Month label to Amount in ₹
    modifier: Modifier = Modifier
) {
    val maxVal = (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1000.0)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Sales & Quotation Revenue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recent months performance (in ₹ INR)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Sales Chart",
                    tint = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / (data.size * 2)
                    val bottomY = size.height - 24.dp.toPx()

                    // Horizontal gridlines
                    for (i in 1..3) {
                        val gridY = bottomY * (i / 4f)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.35f),
                            start = Offset(0f, gridY),
                            end = Offset(size.width, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    data.forEachIndexed { index, pair ->
                        val barHeight = ((pair.second / maxVal) * (bottomY - 10.dp.toPx())).toFloat()
                        val startX = (index * 2 + 0.5f) * barWidth
                        val barTop = bottomY - barHeight

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.65f)),
                                startY = barTop,
                                endY = bottomY
                            ),
                            topLeft = Offset(startX, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }

                // Month labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    data.forEach { pair ->
                        Text(
                            text = pair.first,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCategoryDonutChart(
    websiteCount: Int,
    appCount: Int,
    softwareCount: Int,
    aiCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (websiteCount + appCount + softwareCount + aiCount).coerceAtLeast(1)
    val webAngle = (websiteCount.toFloat() / total) * 360f
    val appAngle = (appCount.toFloat() / total) * 360f
    val softAngle = (softwareCount.toFloat() / total) * 360f
    val aiAngle = (aiCount.toFloat() / total) * 360f

    val colorWeb = Color(0xFF2563EB)
    val colorApp = Color(0xFF10B981)
    val colorSoft = Color(0xFFF59E0B)
    val colorAi = Color(0xFF8B5CF6)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Projects & Requirements by Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 18.dp.toPx()
                        var startAngle = -90f

                        if (websiteCount > 0) {
                            drawArc(colorWeb, startAngle, webAngle, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
                            startAngle += webAngle
                        }
                        if (appCount > 0) {
                            drawArc(colorApp, startAngle, appAngle, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
                            startAngle += appAngle
                        }
                        if (softwareCount > 0) {
                            drawArc(colorSoft, startAngle, softAngle, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
                            startAngle += softAngle
                        }
                        if (aiCount > 0) {
                            drawArc(colorAi, startAngle, aiAngle, false, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Projects",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LegendItem(color = colorWeb, label = "Websites ($websiteCount)")
                    LegendItem(color = colorApp, label = "Mobile Apps ($appCount)")
                    LegendItem(color = colorSoft, label = "Software ($softwareCount)")
                    LegendItem(color = colorAi, label = "AI Automation ($aiCount)")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Pune") }
    var state by remember { mutableStateOf("Maharashtra") }
    var pin by remember { mutableStateOf("411045") }
    var gstin by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Business") }

    val customerTypes = listOf(
        "Business", "Individual", "Company", "Startup", "E-Commerce",
        "Restaurant", "Hotel", "Real Estate", "Clinic", "Gym", "Retail Store", "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("cust_input_name")
                )
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business / Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("cust_input_business")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile / WhatsApp Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("cust_input_phone")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it.uppercase() },
                    label = { Text("GSTIN (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Billing Address & City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                        onConfirm(
                            CustomerEntity(
                                name = name,
                                businessName = businessName,
                                mobileNumber = phone,
                                whatsAppNumber = phone,
                                email = email,
                                billingAddress = address,
                                city = city,
                                state = state,
                                pinCode = pin,
                                gstin = gstin,
                                customerType = selectedType
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.testTag("cust_save_button")
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, type: String, method: String, ref: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(invoice.dueAmount.toLong().toString()) }
    var selectedType by remember { mutableStateOf("Milestone Payment") }
    var selectedMethod by remember { mutableStateOf("UPI") }
    var transRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val paymentTypes = listOf("Booking Advance (30%)", "Milestone Payment", "Full Payment", "Final Handover Balance")
    val paymentMethods = listOf("UPI", "Bank Transfer", "Cash", "Credit Card", "Cheque")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment for ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Balance Due:", fontWeight = FontWeight.Medium)
                        Text(
                            CurrencyUtils.formatInr(invoice.dueAmount),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                // Payment Type Chips
                Text("Payment Stage:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentTypes.take(2).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                // Payment Method Chips
                Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.take(3).forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = transRef,
                    onValueChange = { transRef = it },
                    label = { Text("UPI UTR / Bank Ref / Cheque No.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, selectedType, selectedMethod, transRef, notes)
                    }
                },
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PriceSummaryRow(
    label: String,
    value: String,
    isDiscount: Boolean = false,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isDiscount) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isDiscount) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface
        )
    }
}
