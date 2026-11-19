package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.ConsumerEntity
import com.example.data.model.BillingStatus
import com.example.ui.components.BillingStatusBadge
import com.example.ui.components.CategoryPill
import com.example.ui.components.ConnectionStatusChip
import com.example.ui.components.ConsumerAvatar
import com.example.ui.i18n.BijliLabels
import com.example.ui.theme.LocalBijliColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.Formatters
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerDetailScreen(
    consumer: ConsumerEntity?,
    labels: BijliLabels,
    onBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onCallClick: (ConsumerEntity) -> Unit,
    onCopyText: (String, String) -> Unit,
    onSaveMeterReading: (ConsumerEntity, Int, Double) -> Unit,
    onTogglePaid: (ConsumerEntity) -> Unit,
    onDeleteConsumer: (Int) -> Unit,
    onShareWhatsApp: (ConsumerEntity) -> Unit = {},
    onSendDirectWhatsApp: (ConsumerEntity) -> Unit = {},
    onCopyFullData: (ConsumerEntity) -> Unit = {},
    onExportSingleCsv: (ConsumerEntity) -> Unit = {}
) {
    val sem = LocalBijliColors.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(labels.consumerDetails) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (consumer != null) {
                        IconButton(
                            onClick = { onShareWhatsApp(consumer) },
                            modifier = Modifier.testTag("detail_share_whatsapp_topbar")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Single Consumer Data on WhatsApp"
                            )
                        }
                        IconButton(
                            onClick = { onEditClick(consumer.id) },
                            modifier = Modifier.testTag("detail_edit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Consumer"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (consumer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Consumer record not found.")
            }
            return@Scaffold
        }

        var currentReadingText by remember(consumer.id, consumer.currentReading) {
            mutableStateOf(consumer.currentReading.toString())
        }
        var tariffRateText by remember(consumer.id, consumer.tariffRate) {
            mutableStateOf(consumer.tariffRate.toString())
        }

        val parsedReading = currentReadingText.toIntOrNull()
        val parsedRate = tariffRateText.toDoubleOrNull() ?: consumer.tariffRate
        val readingInvalid = parsedReading == null || parsedReading < consumer.previousReading
        val calcUnits = if (!readingInvalid && parsedReading != null) {
            parsedReading - consumer.previousReading
        } else 0
        val estimatedBill = (calcUnits * parsedRate.coerceAtLeast(0.0)).roundToInt().toDouble()

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete ${consumer.name}?") },
                text = { Text("Account ${consumer.accountNumber} (${consumer.kNumber}) will be permanently removed.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteConsumer(consumer.id)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Profile Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, sem.cardBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ConsumerAvatar(
                                    initials = consumer.initials,
                                    category = consumer.category,
                                    size = 54.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = consumer.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CategoryPill(category = consumer.category)
                                        ConnectionStatusChip(status = consumer.connectionStatus)
                                    }
                                }
                                BillingStatusBadge(status = consumer.billingStatus)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            DetailKeyValueRow("ID", "#${consumer.id}")
                            DetailKeyValueRow("Account Number", consumer.accountNumber) {
                                IconButton(
                                    onClick = { onCopyText("Account Number", consumer.accountNumber) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Account Number",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DetailKeyValueRow("K. Number", consumer.kNumber) {
                                IconButton(
                                    onClick = { onCopyText("K. Number", consumer.kNumber) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy K Number",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DetailKeyValueRow("Meter Number", consumer.meterNumber)
                            DetailKeyValueRow("Mobile", consumer.mobileNumber) {
                                FilledTonalButton(
                                    onClick = { onCallClick(consumer) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = "Call",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            DetailKeyValueRow("Area / Feeder", consumer.addressArea)
                            DetailKeyValueRow("Updated", Formatters.formatTimestamp(consumer.lastUpdatedMillis))
                        }
                    }
                }

                // Quick Meter Reading & Bill Calculator
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ElectricMeter,
                                    contentDescription = "Meter Reading",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = labels.meterCalculator,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Previous Reading: ${consumer.previousReading} kWh",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentReadingText,
                                    onValueChange = { if (it.all { ch -> ch.isDigit() }) currentReadingText = it },
                                    label = { Text("New Current (kWh)") },
                                    isError = readingInvalid,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_new_reading_input")
                                )
                                OutlinedTextField(
                                    value = tariffRateText,
                                    onValueChange = { tariffRateText = it },
                                    label = { Text("Rate (₹/kWh)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_tariff_rate_input")
                                )
                            }

                            if (readingInvalid) {
                                Text(
                                    text = "Current reading must be >= previous reading (${consumer.previousReading} kWh).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Units Consumed",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$calcUnits kWh",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Estimated Bill",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Formatters.formatRupees(estimatedBill),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (!readingInvalid && parsedReading != null) {
                                        onSaveMeterReading(consumer, parsedReading, parsedRate.coerceAtLeast(0.0))
                                    }
                                },
                                enabled = !readingInvalid,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detail_save_reading_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Reading & Update Bill")
                            }
                        }
                    }
                }

                // Single Consumer Full Data & WhatsApp Share Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, sem.paid.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share Single Consumer Data",
                                    tint = sem.paid
                                )
                                Column {
                                    Text(
                                        text = "Single Consumer Full Data • पूरा डेटा शेयर",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Send complete meter, reading & bill slip on WhatsApp or export CSV",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onSendDirectWhatsApp(consumer) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = sem.paid,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_whatsapp_consumer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WhatsApp Consumer", maxLines = 1)
                                }

                                FilledTonalButton(
                                    onClick = { onShareWhatsApp(consumer) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_share_whatsapp_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share WhatsApp", maxLines = 1)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onCopyFullData(consumer) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_copy_full_data_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Full Data", maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = { onExportSingleCsv(consumer) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_export_single_csv_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SaveAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Single CSV", maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Billing Summary & Actions
                item {
                    val isPaid = consumer.billingStatus == BillingStatus.PAID
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, sem.cardBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Billing Summary • बिल सारांश",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            DetailKeyValueRow("Last Bill Amount", Formatters.formatRupees(consumer.lastBillAmount))
                            DetailKeyValueRow("Paid Amount", Formatters.formatRupees(consumer.paidAmount))
                            DetailKeyValueRow("Pending Due", Formatters.formatRupees(consumer.dueAmount))

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { onTogglePaid(consumer) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isPaid) sem.unpaidContainer else sem.paidContainer,
                                        contentColor = if (isPaid) sem.unpaid else sem.paid
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_toggle_paid_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPaid) Icons.Filled.Payments else Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isPaid) "Mark Unpaid" else "Mark Paid")
                                }

                                OutlinedButton(
                                    onClick = { onEditClick(consumer.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Full Record")
                                }
                            }

                            TextButton(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = sem.unpaid),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete Consumer Record", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailKeyValueRow(
    label: String,
    value: String,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (trailingAction != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingAction()
        }
    }
}
