package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.ConsumerEntity
import com.example.data.model.BillingStatus
import com.example.data.model.ConnectionStatus
import com.example.data.model.ConsumerCategory
import com.example.data.model.TariffSettings
import com.example.data.repository.SaveConsumerResult
import com.example.ui.i18n.BijliLabels
import com.example.ui.theme.MonoFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerFormScreen(
    existingConsumer: ConsumerEntity?,
    tariffSettings: TariffSettings,
    labels: BijliLabels,
    onGetSuggestedNumbers: suspend () -> Pair<String, Pair<String, String>>,
    onSave: suspend (ConsumerEntity) -> SaveConsumerResult,
    onBack: () -> Unit,
    onSavedSuccess: (Int, Boolean) -> Unit
) {
    val isEditing = existingConsumer != null
    val scope = rememberCoroutineScope()

    var accountNumber by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.accountNumber ?: "")
    }
    var kNumber by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.kNumber ?: "")
    }
    var name by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.name ?: "")
    }
    var category by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.category ?: ConsumerCategory.DOMESTIC)
    }
    var meterNumber by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.meterNumber ?: "")
    }
    var mobileNumber by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.mobileNumber ?: "")
    }
    var addressArea by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.addressArea ?: "")
    }
    var connectionStatus by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.connectionStatus ?: ConnectionStatus.ACTIVE)
    }
    var previousReading by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.previousReading?.toString() ?: "0")
    }
    var currentReading by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.currentReading?.toString() ?: "0")
    }
    var tariffRate by remember(existingConsumer) {
        mutableStateOf(
            existingConsumer?.tariffRate?.toString()
                ?: tariffSettings.rateFor(ConsumerCategory.DOMESTIC).toString()
        )
    }
    var billingStatus by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.billingStatus ?: BillingStatus.UNPAID)
    }
    var lastBillAmount by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.lastBillAmount?.roundToInt()?.toString() ?: "0")
    }
    var paidAmount by remember(existingConsumer) {
        mutableStateOf(existingConsumer?.paidAmount?.roundToInt()?.toString() ?: "0")
    }

    var accountError by remember { mutableStateOf<String?>(null) }
    var kNumberError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var meterError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var readingError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(existingConsumer) {
        if (existingConsumer == null && accountNumber.isEmpty()) {
            val (acc, pair) = onGetSuggestedNumbers()
            accountNumber = acc
            kNumber = pair.first
            meterNumber = pair.second
        }
    }

    val prevInt = previousReading.toIntOrNull() ?: 0
    val currInt = currentReading.toIntOrNull() ?: 0
    val unitsConsumed = (currInt - prevInt).coerceAtLeast(0)
    val rateDouble = tariffRate.toDoubleOrNull() ?: category.defaultTariff

    fun recalculateBill() {
        val bill = (unitsConsumed * rateDouble).roundToInt()
        lastBillAmount = bill.toString()
        if (billingStatus == BillingStatus.PAID) {
            paidAmount = bill.toString()
        } else if (billingStatus == BillingStatus.UNPAID) {
            paidAmount = "0"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) labels.editConsumer else labels.addConsumer)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("form_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("${labels.fullName} *") },
                        isError = nameError != null,
                        supportingText = nameError?.let { err -> { Text(err) } },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_name_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = {
                                accountNumber = it
                                accountError = null
                            },
                            label = { Text("${labels.accountNumber} *") },
                            isError = accountError != null,
                            supportingText = accountError?.let { err -> { Text(err) } },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_account_input")
                        )
                        OutlinedTextField(
                            value = kNumber,
                            onValueChange = {
                                kNumber = it
                                kNumberError = null
                            },
                            label = { Text("${labels.kNumber} *") },
                            isError = kNumberError != null,
                            supportingText = kNumberError?.let { err -> { Text(err) } },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_k_number_input")
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = labels.category,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ConsumerCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = {
                                        category = cat
                                        tariffRate = tariffSettings.rateFor(cat).toString()
                                    },
                                    label = { Text(cat.code) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = meterNumber,
                            onValueChange = {
                                meterNumber = it
                                meterError = null
                            },
                            label = { Text("${labels.meterNumber} *") },
                            isError = meterError != null,
                            supportingText = meterError?.let { err -> { Text(err) } },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_meter_input")
                        )
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = {
                                if (it.length <= 10 && it.all { ch -> ch.isDigit() }) {
                                    mobileNumber = it
                                    mobileError = null
                                }
                            },
                            label = { Text("${labels.mobileNumber} *") },
                            isError = mobileError != null,
                            supportingText = mobileError?.let { err -> { Text(err) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_mobile_input")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = addressArea,
                        onValueChange = { addressArea = it },
                        label = { Text(labels.addressArea) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_address_input")
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = labels.connectionStatus,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ConnectionStatus.entries.forEach { status ->
                                FilterChip(
                                    selected = connectionStatus == status,
                                    onClick = { connectionStatus = status },
                                    label = { Text(status.englishLabel) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = previousReading,
                            onValueChange = {
                                if (it.all { ch -> ch.isDigit() }) {
                                    previousReading = it
                                    readingError = null
                                }
                            },
                            label = { Text(labels.previousReading) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_prev_reading_input")
                        )
                        OutlinedTextField(
                            value = currentReading,
                            onValueChange = {
                                if (it.all { ch -> ch.isDigit() }) {
                                    currentReading = it
                                    readingError = null
                                }
                            },
                            label = { Text(labels.currentReading) },
                            isError = readingError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_curr_reading_input")
                        )
                    }
                    if (readingError != null) {
                        Text(
                            text = readingError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Units Consumed: $unitsConsumed kWh",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { recalculateBill() }) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = "Auto-calculate bill",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Auto-Calc Bill")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = tariffRate,
                            onValueChange = { tariffRate = it },
                            label = { Text(labels.tariffRate) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lastBillAmount,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) lastBillAmount = it },
                            label = { Text(labels.lastBillAmount) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_bill_amount_input")
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = labels.billingStatus,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BillingStatus.entries.forEach { status ->
                                FilterChip(
                                    selected = billingStatus == status,
                                    onClick = {
                                        billingStatus = status
                                        val billVal = lastBillAmount.toIntOrNull() ?: 0
                                        if (status == BillingStatus.PAID) {
                                            paidAmount = billVal.toString()
                                        } else if (status == BillingStatus.UNPAID) {
                                            paidAmount = "0"
                                        }
                                    },
                                    label = { Text(status.englishLabel) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                if (billingStatus == BillingStatus.PARTIAL) {
                    item {
                        OutlinedTextField(
                            value = paidAmount,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) paidAmount = it },
                            label = { Text(labels.paidAmount) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            var valid = true
                            if (name.isBlank()) {
                                nameError = "Consumer name is required"
                                valid = false
                            }
                            if (accountNumber.isBlank()) {
                                accountError = "Account Number is required"
                                valid = false
                            }
                            if (kNumber.isBlank()) {
                                kNumberError = "K. Number is required"
                                valid = false
                            }
                            if (meterNumber.isBlank()) {
                                meterError = "Meter Number is required"
                                valid = false
                            }
                            if (mobileNumber.length != 10) {
                                mobileError = "Enter a valid 10-digit mobile number"
                                valid = false
                            }
                            if (currInt < prevInt) {
                                readingError = "Current reading cannot be less than previous reading"
                                valid = false
                            }
                            if (!valid || isSaving) return@Button

                            val finalBill = lastBillAmount.toDoubleOrNull()
                                ?: (unitsConsumed * rateDouble).roundToInt().toDouble()
                            val finalPaid = when (billingStatus) {
                                BillingStatus.PAID -> finalBill
                                BillingStatus.UNPAID -> 0.0
                                BillingStatus.PARTIAL -> (paidAmount.toDoubleOrNull() ?: 0.0).coerceIn(0.0, finalBill)
                            }

                            val entity = ConsumerEntity(
                                id = existingConsumer?.id ?: 0,
                                accountNumber = accountNumber,
                                kNumber = kNumber,
                                name = name,
                                category = category,
                                meterNumber = meterNumber,
                                mobileNumber = mobileNumber,
                                addressArea = addressArea.ifBlank { "Main Zone • Feeder F-01" },
                                connectionStatus = connectionStatus,
                                previousReading = prevInt,
                                currentReading = currInt,
                                tariffRate = rateDouble,
                                lastBillAmount = finalBill,
                                paidAmount = finalPaid,
                                billingStatus = billingStatus
                            )

                            isSaving = true
                            scope.launch {
                                when (val result = onSave(entity)) {
                                    is SaveConsumerResult.Success -> {
                                        isSaving = false
                                        onSavedSuccess(result.consumerId, isEditing)
                                    }
                                    is SaveConsumerResult.ValidationError -> {
                                        isSaving = false
                                        if (result.field == "accountNumber") {
                                            accountError = result.message
                                        } else if (result.field == "kNumber") {
                                            kNumberError = result.message
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("form_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "Update Consumer Record" else labels.saveChanges,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
