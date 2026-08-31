package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BusinessSettingsEntity
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.ui.viewmodel.UserRole

@Composable
fun SettingsScreen(
    viewModel: SmartQuoteViewModel,
    modifier: Modifier = Modifier
) {
    val currentSettings by viewModel.businessSettings.collectAsStateWithLifecycle()
    val userRole by viewModel.currentRole.collectAsStateWithLifecycle()

    var businessName by remember(currentSettings) { mutableStateOf(currentSettings?.businessName ?: "Rishi_dev") }
    var tagline by remember(currentSettings) { mutableStateOf(currentSettings?.tagline ?: "Web & Mobile App Development • AI Automations") }
    var gstin by remember(currentSettings) { mutableStateOf(currentSettings?.gstin ?: "27ALDPI8191C1Z5") }
    var pan by remember(currentSettings) { mutableStateOf(currentSettings?.pan ?: "ALDPI8191C") }
    var email by remember(currentSettings) { mutableStateOf(currentSettings?.email ?: "contact@rishidev.com") }
    var phone by remember(currentSettings) { mutableStateOf(currentSettings?.phone ?: "+91 9876543210") }
    var address by remember(currentSettings) { mutableStateOf(currentSettings?.address ?: "Pune IT Park, Maharashtra, India 411045") }

    var bankName by remember(currentSettings) { mutableStateOf(currentSettings?.bankName ?: "HDFC Bank") }
    var accountNo by remember(currentSettings) { mutableStateOf(currentSettings?.accountNumber ?: "50200087654321") }
    var ifsc by remember(currentSettings) { mutableStateOf(currentSettings?.ifscCode ?: "HDFC0001234") }
    var holder by remember(currentSettings) { mutableStateOf(currentSettings?.accountHolder ?: "Rishi_dev Technologies") }
    var upiId by remember(currentSettings) { mutableStateOf(currentSettings?.upiId ?: "rishidev@hdfcbank") }

    var defaultGstRateText by remember(currentSettings) { mutableStateOf(currentSettings?.defaultGstRate?.toInt()?.toString() ?: "18") }
    var salesMaxDiscountText by remember(currentSettings) { mutableStateOf(currentSettings?.salesMaxAllowedDiscountPercent?.toInt()?.toString() ?: "10") }
    var terms by remember(currentSettings) { mutableStateOf(currentSettings?.termsAndConditions ?: "") }

    var saveMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
    ) {
        // Business Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🏢 Business Details & GST Compliance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business / Enterprise Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_business_name")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Business Tagline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it.uppercase() },
                            label = { Text("GSTIN *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("settings_gstin")
                        )
                        OutlinedTextField(
                            value = pan,
                            onValueChange = { pan = it.uppercase() },
                            label = { Text("PAN") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone / WhatsApp") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Official Email") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Registered Business Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Bank & UPI QR Payment Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🏦 Bank Transfer & UPI Details (Printed on Invoices)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = accountNo,
                            onValueChange = { accountNo = it },
                            label = { Text("Account Number") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ifsc,
                            onValueChange = { ifsc = it.uppercase() },
                            label = { Text("IFSC Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI Virtual Payment Address (VPA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Sales Discount Threshold & GST Rules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚙️ GST & Sales Threshold Rules", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = defaultGstRateText,
                            onValueChange = { defaultGstRateText = it },
                            label = { Text("Default GST Rate (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = salesMaxDiscountText,
                            onValueChange = { salesMaxDiscountText = it },
                            label = { Text("Sales Max Allowed Discount (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Terms & Conditions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📄 Standard Quotation Terms & Conditions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = terms,
                        onValueChange = { terms = it },
                        label = { Text("Terms & Conditions") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val gst = defaultGstRateText.toDoubleOrNull() ?: 18.0
                    val maxDisc = salesMaxDiscountText.toDoubleOrNull() ?: 10.0
                    viewModel.saveSettings(
                        BusinessSettingsEntity(
                            id = 1,
                            businessName = businessName,
                            tagline = tagline,
                            gstin = gstin,
                            pan = pan,
                            email = email,
                            phone = phone,
                            address = address,
                            bankName = bankName,
                            accountNumber = accountNo,
                            ifscCode = ifsc,
                            accountHolder = holder,
                            upiId = upiId,
                            defaultGstRate = gst,
                            salesMaxAllowedDiscountPercent = maxDisc,
                            termsAndConditions = terms
                        )
                    )
                    saveMessage = "Business settings saved successfully!"
                },
                modifier = Modifier.fillMaxWidth().testTag("save_settings_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save All Settings", fontWeight = FontWeight.Bold)
            }

            if (saveMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = saveMessage!!,
                    color = Color(0xFF15803D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
